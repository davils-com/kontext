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
import com.davils.kontext.core.fixture.containerOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.yield

private const val BROKEN: String = "broken"
private val heavy: LazyModule = lazyModule("heavy") { single<Greeter> { EnglishGreeter() } }

class LazyModulesTest : FunSpec({
    test("lazy modules load after the start and resolution waits for them") {
        val context = currentCoroutineContext()
        val container = containerOf {
            coroutineContext = context
            lazyModules(heavy)
        }

        container.isReady shouldBe false
        val failure = shouldThrow<ResolutionException> { container.get<Greeter>() }.failure
        val greeter = container.await<Greeter>()

        failure.shouldBeInstanceOf<ResolutionFailure.NotReady>()
        failure.reason shouldBe "not-ready"
        failure.toString() shouldBe "NotReady(${failure.key}, path=[])"
        greeter.greet() shouldBe "hello"
        container.isReady shouldBe true
        container.awaitReady()
        container.awaitAll<Greeter>().size shouldBe 1
        heavy.toString() shouldBe "LazyModule(heavy)"
        container.close()
    }

    test("a failing lazy module fails the wait for readiness") {
        val context = currentCoroutineContext()
        val container = containerOf {
            coroutineContext = context
            lazyModules(lazyModule(BROKEN) { error(BROKEN) })
        }

        shouldThrow<IllegalStateException> { container.awaitReady() }.message shouldBe BROKEN
        container.close()
    }

    test("a container without lazy modules is ready at once") {
        val container = containerOf()

        yield()

        container.isReady shouldBe true
        container.awaitReady()
    }
})
