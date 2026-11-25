/*
 * Copyright 2026 Davils
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.davils.kontext.core

import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.parameter.parametersOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield

private const val SCOPED: String = "scoped"
private const val CALLERS: Int = 16

class AsyncTest : FunSpec({
    test("a suspending single is created once for every concurrent caller") {
        val gate = CompletableDeferred<Unit>()
        val journal = Journal()
        val container = containerOf(
            module("app") {
                singleAsync<Greeter> {
                    journal.write("created")
                    gate.await()
                    EnglishGreeter()
                }
            },
        )

        val callers = (1..CALLERS).map { async(start = CoroutineStart.UNDISPATCHED) { container.await<Greeter>() } }
        val pending = shouldThrow<ResolutionException> { container.get<Greeter>() }.failure
        gate.complete(Unit)
        val greeters = callers.awaitAll()

        greeters.distinct().size shouldBe 1
        journal.entries shouldBe listOf("created")
        pending.shouldBeInstanceOf<ResolutionFailure.Suspending>()
        pending.reason shouldBe "suspending"
        pending.toString() shouldBe "Suspending(${pending.key}, path=[])"
        container.get<Greeter>() shouldBeSameInstanceAs greeters.first()
    }

    test("a cancelled caller does not cancel the creation for the others") {
        val gate = CompletableDeferred<Unit>()
        val container = containerOf(
            module("app") {
                singleAsync<Greeter> {
                    gate.await()
                    EnglishGreeter()
                }
            },
        )
        val first = async(start = CoroutineStart.UNDISPATCHED) { container.await<Greeter>() }
        val second = async(start = CoroutineStart.UNDISPATCHED) { container.await<Greeter>() }

        first.cancel()
        yield()
        gate.complete(Unit)

        second.await().greet() shouldBe "hello"
    }

    test("suspending factories, scoped definitions and parameters") {
        val container = containerOf(
            module("app") {
                single<Greeter> { EnglishGreeter() }
                factoryAsync {
                    yield()
                    Service(await())
                }
                factoryAsync { Request(parameters.get()) }
                singleAsync { 1 }
                scope(REQUEST) { scopedAsync(Qualifier(SCOPED)) { Request(SCOPED) } }
            },
        )

        container.await<Service>() shouldNotBeSameInstanceAs container.await<Service>()
        container.await<Request>(parameters = parametersOf("id")).id shouldBe "id"
        shouldThrow<ResolutionException> { container.get<Service>() }
            .failure
            .shouldBeInstanceOf<ResolutionFailure.Suspending>()
        shouldThrow<ResolutionException> { container.await<Int>(parameters = parametersOf("x")) }
            .failure
            .shouldBeInstanceOf<ResolutionFailure.ParametersRefused>()
        container.await<Greeter>().greet() shouldBe "hello"
        container.awaitOrNull<Long>().shouldBeNull()
        container.awaitAll<Greeter>().size shouldBe 1
        val scope = container.openScope(REQUEST)
        scope.await<Request>(Qualifier(SCOPED)) shouldBeSameInstanceAs scope.await<Request>(Qualifier(SCOPED))
        scope.awaitOrNull<Request>(Qualifier(SCOPED)).shouldBeInstanceOf<Request>()
        scope.awaitAll<Greeter>().size shouldBe 1
        shouldThrow<ResolutionException> { container.await<Long>() }
            .failure
            .shouldBeInstanceOf<ResolutionFailure.Missing>()
    }

    test("eager suspending singles are created on start and a failure rolls the start back") {
        val journal = Journal()
        containerOf(
            module("app") {
                singleAsync(eager = true) { "a".also(journal::write) }
                singleAsync(eager = true) { 1.also { journal.write("b") } }
            },
        )
        val broken = module("broken") {
            singleAsync(eager = true, close = { resource: Resource -> resource.close() }) { Resource("r", journal) }
            singleAsync<Greeter>(eager = true) {
                yield()
                error("broken")
            }
        }

        shouldThrow<ResolutionException> { containerOf(broken) }.failure.shouldBeInstanceOf<ResolutionFailure.Failed>()

        journal.entries shouldBe listOf("a", "b", "opened r", "closed r")
    }

    test("closing cancels the coroutine scope and creations in flight") {
        val container = containerOf(module("app") { singleAsync<Greeter> { awaitCancellation() } })
        val creation = async(start = CoroutineStart.UNDISPATCHED) {
            try {
                container.await<Greeter>()
                null
            } catch (refusal: ResolutionException) {
                refusal
            }
        }

        container.coroutineScope.isActive shouldBe true
        container.close()

        container.coroutineScope.isActive shouldBe false
        creation.await().shouldBeInstanceOf<ResolutionException>()
    }

    test("a factory sees the coroutine scope of its holder") {
        val container = containerOf(
            module("app") {
                single { coroutineScope }
                scope(REQUEST) { scoped { Request(coroutineScope.toString()) } }
            },
        )

        container.get<CoroutineScope>() shouldBeSameInstanceAs container.coroutineScope
        val scope = container.openScope(REQUEST)
        scope.get<Request>().id shouldBe scope.coroutineScope.toString()
    }
})
