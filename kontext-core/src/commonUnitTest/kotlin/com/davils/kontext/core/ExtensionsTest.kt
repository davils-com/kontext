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
import com.davils.kontext.core.extension.Retention
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Failing
import com.davils.kontext.core.fixture.GermanGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.Quiet
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Recording
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.testing.RecordingContainerSink
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

private val GERMAN_NAME: Qualifier = Qualifier("german")
private val WRONG: Qualifier = Qualifier("wrong")
private val FAILING: Qualifier = Qualifier("failing")

class ExtensionsTest : FunSpec({
    context("extensions") {
        test("hear the life of the container and its children in order") {
            val journal = Journal()
            val container = containerOf { extensions(Recording("a", journal), Recording("b", journal), Quiet()) }
            val handle = container.load(module("late") {})
            handle.unload()
            container.child { name = ContainerName("kid") }

            container.close()

            journal.entries shouldBe listOf(
                "a started root",
                "b started root",
                "a loaded 1",
                "b loaded 1",
                "a unloaded 1",
                "b unloaded 1",
                "a started kid",
                "b started kid",
                "b closing",
                "a closing",
                "b closing",
                "a closing",
            )
        }

        test("that fail are reported and never stop the container") {
            val sink = RecordingContainerSink()
            val container = containerOf {
                this.sink = sink
                extensions(Failing())
            }
            container.close().isClean shouldBe true

            sink.of<ContainerEvent.ExtensionFailed>().map { event -> event.phase } shouldBe listOf("started", "closing")
            val failure = sink.of<ContainerEvent.ExtensionFailed>().first()
            failure.toString() shouldBe
                "ExtensionFailed(root, \"failing\", started)"
        }
    }

    context("fallbacks") {
        test("supply what the container does not declare, checked against the key") {
            val container = containerOf(module("app") { single<Greeter> { EnglishGreeter() } }) {
                fallbacks(
                    { key, _ -> GermanGreeter().takeIf { key.qualifier == GERMAN_NAME } },
                    { key, _ -> "wrong".takeIf { key.qualifier == WRONG } },
                    { key, _ -> key.qualifier?.takeIf { it == FAILING }?.let { error("x") } },
                )
            }

            container.get<Greeter>(GERMAN_NAME).greet() shouldBe "hallo"
            container.get<Greeter>(
                GERMAN_NAME,
            ) shouldNotBeSameInstanceAs container.get<Greeter>(GERMAN_NAME)
            container.getOrNull<Greeter>(Qualifier("none")).shouldBeNull()
            val mismatched = shouldThrow<ResolutionException> { container.get<Greeter>(WRONG) }.failure
            mismatched.shouldBeInstanceOf<ResolutionFailure.Mismatched>()
            mismatched.reason shouldBe "mismatched"
            mismatched.toString() shouldBe "Mismatched(${mismatched.key}, path=[])"
            shouldThrow<ResolutionException> { container.get<Greeter>(FAILING) }
                .failure
                .shouldBeInstanceOf<ResolutionFailure.Failed>()
            container.await<Greeter>(GERMAN_NAME).greet() shouldBe "hallo"
        }
    }

    context("retention") {
        test("replaces an expired instance and closes the old one") {
            val time = TestTimeSource()
            val journal = Journal()
            val context = currentCoroutineContext()
            var count = 0
            val container = containerOf(
                module("app") {
                    single(
                        retention = Retention.expireAfter(1.minutes),
                        close = { resource: Resource -> resource.close() },
                    ) {
                        count++
                        Resource("r$count", journal)
                    }
                    scope(REQUEST) { scoped(retention = Retention.FOREVER) { 1 } }
                },
            ) {
                timeSource = time
                coroutineContext = context
            }
            val first = container.get<Resource>()

            time += 30.seconds
            container.get<Resource>() shouldBeSameInstanceAs first
            time += 1.minutes
            val second = container.get<Resource>()
            yield()

            second.name shouldBe "r2"
            journal.entries shouldBe listOf("opened r1", "opened r2", "closed r1")
            container.openScope(REQUEST).get<Int>() shouldBe 1
            container.close()
        }

        test("a failing close of an expired instance is reported and a throwing retention keeps") {
            val time = TestTimeSource()
            val sink = RecordingContainerSink()
            val context = currentCoroutineContext()
            val container = containerOf(
                module("app") {
                    single<Greeter>(retention = Retention.expireAfter(1.seconds), close = { error("x") }) {
                        EnglishGreeter()
                    }
                    single(retention = { error("x") }) { 1 }
                },
            ) {
                timeSource = time
                coroutineContext = context
                this.sink = sink
            }
            container.get<Greeter>()
            val number = container.get<Int>()

            time += 2.seconds
            container.get<Greeter>()
            yield()

            container.get<Int>() shouldBe number
            val failure = sink.of<ContainerEvent.DisposalFailed>().single()
            failure.toString() shouldBe
                "DisposalFailed(root, ${DefinitionKey.of<Greeter>()})"
            shouldThrow<IllegalArgumentException> { Retention.expireAfter(0.seconds) }
            container.close()
        }
    }
})
