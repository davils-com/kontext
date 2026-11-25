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

import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.error.ContainerClosedException
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.extension.Retention
import com.davils.kontext.core.fixture.BrokenTracer
import com.davils.kontext.core.fixture.Chicken
import com.davils.kontext.core.fixture.Egg
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.Nameless
import com.davils.kontext.core.fixture.ParentCloser
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.testing.RecordingContainerSink
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.milliseconds

private const val FIRST: String = "first"
private const val SECOND: String = "second"
private const val CLOSED_FIRST: String = "closed $FIRST"
private const val TIMEOUT_MILLIS: Long = 50L

class HardeningTest : FunSpec({
    context("resolution depth") {
        test("a fallback that resolves its own key fails as circular instead of overflowing the stack") {
            val container = containerOf(module("app") {}) {
                fallbacks({ _, resolver -> resolver.getOrNull<Service>() })
            }

            val failure = shouldThrow<ResolutionException> { container.getOrNull<Service>() }

            failure.failure.shouldBeInstanceOf<ResolutionFailure.Circular>()
        }

        test("a single forcing a lazy dependency on itself fails as circular") {
            val container = containerOf(
                module("app") {
                    single { Chicken(get()) }
                    single { Egg(getLazy()).also { egg -> egg.chicken.value } }
                },
            )

            val failure = shouldThrow<ResolutionException> { container.get<Chicken>() }

            failure.failure.shouldBeInstanceOf<ResolutionFailure.Circular>()
        }

        test("factories forcing lazy dependencies on each other stop at the depth limit") {
            val container = containerOf(
                module("app") {
                    factory { Chicken(get()) }
                    factory { Egg(getLazy()).also { egg -> egg.chicken.value } }
                },
            )

            val failure = shouldThrow<ResolutionException> { container.get<Chicken>() }

            failure.failure.shouldBeInstanceOf<ResolutionFailure.TooDeep>()
        }

        test("a sink that resolves what failed does not recurse") {
            var self: Container? = null
            val container = containerOf(module("app") {}) {
                sink = { event -> if (event is ContainerEvent.ResolutionFailed) self?.get<Service>() }
            }
            self = container

            shouldThrow<ResolutionException> { container.get<Service>() }

            container.rejectedEvents shouldBeGreaterThan 0L
        }
    }

    context("closing") {
        test("a cancelled close still runs every close function and frees the scope id") {
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    scope(SESSION) {
                        scoped(qualifier = Qualifier(FIRST), close = { resource: Resource -> resource.close() }) {
                            Resource(FIRST, journal)
                        }
                        scoped(
                            qualifier = Qualifier(SECOND),
                            close = { resource: Resource ->
                                yield()
                                resource.close()
                            },
                        ) { Resource(SECOND, journal) }
                    }
                },
            )
            val scope = container.openScope(SESSION, ScopeId(FIRST))
            scope.get<Resource>(Qualifier(FIRST))
            scope.get<Resource>(Qualifier(SECOND))

            coroutineScope {
                val closing = launch(start = CoroutineStart.UNDISPATCHED) { scope.close() }
                closing.cancel()
            }

            journal.entries shouldContain CLOSED_FIRST
            journal.entries shouldContain "closed $SECOND"
            scope.isOpen.shouldBeFalse()
            container.openScope(SESSION, ScopeId(FIRST)).isOpen shouldBe true
            container.close()
        }

        test("a second close waits until the first one has closed everything") {
            val gate = CompletableDeferred<Unit>()
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    single(
                        eager = true,
                        close = { resource: Resource ->
                            gate.await()
                            resource.close()
                        },
                    ) { Resource(FIRST, journal) }
                },
            )

            coroutineScope {
                val first = async(start = CoroutineStart.UNDISPATCHED) { container.close() }
                val second = async(start = CoroutineStart.UNDISPATCHED) { container.close() }
                second.isCompleted.shouldBeFalse()
                gate.complete(Unit)

                second.await() shouldBe CloseReport.AlreadyClosed
                first.await().isClean shouldBe true
            }
            journal.entries shouldContain CLOSED_FIRST
        }

        test("a close function closing its own container does not wait for itself") {
            var self: Container? = null
            var nested: CloseReport? = null
            val container = containerOf(
                module("app") {
                    single(eager = true, close = { _: Journal -> nested = self?.close() }) { Journal() }
                },
            )
            self = container

            container.close().isClean shouldBe true
            nested shouldBe CloseReport.AlreadyClosed
        }

        test("a broken tracer and a broken extension leave start and close intact") {
            val sink = RecordingContainerSink()
            val journal = Journal()
            val container = containerOf(
                module(
                    "app",
                ) {
                    single(
                        eager = true,
                        close = { resource: Resource -> resource.close() },
                    ) { Resource(FIRST, journal) }
                },
            ) {
                tracer = BrokenTracer()
                this.sink = sink
                extensions(Nameless())
            }

            container.close().isClean shouldBe true

            journal.entries shouldContain CLOSED_FIRST
            sink.of<ContainerEvent.ExtensionFailed>().map { event -> event.extension } shouldContainExactly
                listOf("unnamed", "unnamed")
        }

        test("a cancelled start closes what it created") {
            val journal = Journal()

            shouldThrow<TimeoutCancellationException> {
                withTimeout(TIMEOUT_MILLIS.milliseconds) {
                    containerOf(
                        module("app") {
                            single(eager = true, close = { resource: Resource -> resource.close() }) {
                                Resource(FIRST, journal)
                            }
                            singleAsync(eager = true) { awaitCancellation() }
                        },
                    )
                }
            }

            journal.entries shouldContain CLOSED_FIRST
        }

        test("lazy modules of a container whose job is cancelled report it instead of hanging") {
            val job = Job()
            job.cancel()
            val container = containerOf(module("app") {}) {
                coroutineContext = job
                lazyModules(lazyModule("late") { single { Journal() } })
            }

            shouldThrow<ContainerClosedException> { container.awaitReady() }
        }
    }

    context("closed hosts") {
        test("a close function resolving a single nobody created yet fails instead of leaking it") {
            var self: Container? = null
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    single(eager = true, close = { _: Journal -> self?.get<Resource>() }) { Journal() }
                    single(close = { resource: Resource -> resource.close() }) { Resource(FIRST, journal) }
                },
            )
            self = container

            val report = container.close()

            report.failures.size shouldBe 1
            journal.entries shouldBe emptyList()
        }

        test("a factory a close function resolves after the seal fails as closed") {
            var self: Container? = null
            val container = containerOf(
                module("app") {
                    single(eager = true, close = { _: Journal -> self?.get<Request>() }) { Journal() }
                    factory { Request(FIRST) }
                },
            )
            self = container

            container.close().failures.size shouldBe 1
        }

        test("a suspending single finished after the seal is closed again instead of leaking") {
            val gate = CompletableDeferred<Unit>()
            val journal = Journal()
            val context = currentCoroutineContext()
            val container = containerOf(
                module("app") {
                    singleAsync(close = { resource: Resource -> resource.close() }) {
                        withContext(NonCancellable) { gate.await() }
                        Resource(FIRST, journal)
                    }
                },
            ) { coroutineContext = context }

            coroutineScope {
                val awaiting = async(start = CoroutineStart.UNDISPATCHED) {
                    shouldThrow<ResolutionException> { container.await<Resource>() }
                }
                yield()
                container.close()
                gate.complete(Unit)
                awaiting.await().failure.shouldBeInstanceOf<ResolutionFailure.Closed>()
            }
            yield()

            journal.entries shouldContainExactly listOf("opened $FIRST", CLOSED_FIRST)
        }

        test("a suspending single a close function awaits after the seal fails as closed") {
            var self: Container? = null
            val container = containerOf(
                module("app") {
                    single(eager = true, close = { _: Journal -> self?.await<Request>() }) { Journal() }
                    singleAsync { Request(FIRST) }
                },
            )
            self = container

            container.close().failures.size shouldBe 1
        }

        test("a child whose parent closes while it starts is closed and refused") {
            val parent = containerOf(module("app") {})

            shouldThrow<ContainerClosedException> { parent.child { extensions(ParentCloser(parent)) } }

            parent.state.value shouldBe ContainerState.Closed
        }

        test("a closed scope opens no scope and takes no link") {
            val container = containerOf(
                module("app") {
                    scope(SESSION) {}
                    scope(REQUEST, within = SESSION) {}
                },
            )
            val closed = container.openScope(SESSION)
            val open = container.openScope(SESSION)
            closed.close()

            shouldThrow<IllegalStateException> { closed.openScope(REQUEST) }
            shouldThrow<IllegalStateException> { open.link(closed) }
            shouldThrow<IllegalStateException> { closed.link(open) }
            container.close()
        }

        test("a retention that fails expires the instance") {
            val container = containerOf(
                module(
                    "app",
                ) { single<Greeter>(retention = Retention { error("The retention is broken") }) { EnglishGreeter() } },
            )

            container.get<Greeter>() shouldNotBe container.get<Greeter>()
            container.close()
        }

        test("a module loaded again after it was unloaded creates its singles anew") {
            val counted = Journal()
            val late = module("late") { single { Resource(FIRST, counted) } }
            val container = containerOf(module("app") {})

            val first = container.load(late)
            container.get<Resource>()
            first.unload()
            container.load(late)
            container.get<Resource>()

            counted.entries.size shouldBeGreaterThan 1
            container.close()
        }
    }
})
