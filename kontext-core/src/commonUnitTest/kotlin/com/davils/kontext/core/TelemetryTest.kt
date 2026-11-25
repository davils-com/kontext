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

import com.davils.kollect.core.meterRegistry
import com.davils.konfetti.testing.EventRecorder
import com.davils.konfetti.testing.testEventBus
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.fixture.BROKEN
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.HELLO
import com.davils.kontext.core.fixture.RecordingTracer
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.fixture.gauge
import com.davils.kontext.core.fixture.sum
import com.davils.kontext.core.fixture.withScope
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.testing.FailingContainerSink
import com.davils.kontext.testing.RecordingContainerSink
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.awaitCancellation

private val app: Module = module("app") {
    single<Greeter> { EnglishGreeter() }
    factory { Service(get()) }
    scope(SESSION) { factory { "session" } }
}

private const val ROOT_DEFINITIONS: Int = 3
private const val REJECTED_EVENTS: Double = 6.0

class TelemetryTest : FunSpec({
    context("sink") {
        test("hears every event in order") {
            val sink = RecordingContainerSink()
            val container = containerOf(app) { this.sink = sink }

            container.get<Service>()
            container.openScope(SESSION).close()
            shouldThrow<ResolutionException> { container.get<Int>() }
            container.child { }
            container.close()

            sink.events.map { event -> event::class.simpleName } shouldBe listOf(
                "Started",
                "InstanceCreated",
                "InstanceCreated",
                "ScopeOpened",
                "ScopeClosed",
                "ResolutionFailed",
                "Started",
                "ChildOpened",
                "Closed",
                "Closed",
            )
            val started = sink.of<ContainerEvent.Started>().first()
            val closed = sink.of<ContainerEvent.ScopeClosed>().single()
            val failed = sink.of<ContainerEvent.ResolutionFailed>().single()
            started.definitions shouldBe ROOT_DEFINITIONS
            closed.failures shouldBe 0
            failed.failure.reason shouldBe "missing"
        }

        test("a failing sink loses its events but not the container") {
            val container = containerOf(app) { sink = FailingContainerSink() }

            container.get<Greeter>().greet() shouldBe HELLO

            container.rejectedEvents shouldBe 2L
        }
    }

    context("event bus") {
        test("reaches the topic when it has consumers") {
            withScope { scope ->
                val bus = testEventBus(scope)
                val recorder = EventRecorder<ContainerEvent>()
                bus.declare(ContainerEvent.TOPIC)
                bus[ContainerEvent.TOPIC].consume("recorder") { handle(recorder::record) }
                val container = containerOf(app) { events = bus }

                container.close()

                recorder.awaitCount(2)
                recorder.payloads.first().shouldBeInstanceOf<ContainerEvent.Started>()
                container.rejectedEvents shouldBe 0L
            }
        }

        test("counts the events a full topic refuses") {
            withScope { scope ->
                val bus = testEventBus(scope)
                bus.declare(ContainerEvent.TOPIC) { bufferCapacity = 1 }
                bus[ContainerEvent.TOPIC].consume("stuck") { handle { awaitCancellation() } }
                val container = containerOf(app) { events = bus }

                repeat(times = 8) { container.get<Service>() }

                container.rejectedEvents shouldBeGreaterThan 0L
            }
        }

        test("skips a topic without consumers") {
            withScope { scope ->
                val container = containerOf(app) { events = testEventBus(scope) }

                container.get<Greeter>()

                container.rejectedEvents shouldBe 0L
            }
        }
    }

    context("meters") {
        test("count instances, failures, scopes, children and rejected events") {
            val registry = meterRegistry()
            val container = containerOf(app) {
                meters = registry
                sink = FailingContainerSink()
            }

            container.get<Service>()
            val scope = container.openScope(SESSION)
            shouldThrow<ResolutionException> { container.get<Int>() }
            val child = container.child { name = ContainerName("child") }

            registry.sum("kontext.instances", "root", "single") shouldBe 1.0
            registry.sum("kontext.instances", "root", "factory") shouldBe 1.0
            registry.sum("kontext.resolution.failures", "root", "missing") shouldBe 1.0
            registry.gauge("kontext.scopes.open", "root") shouldBe 1.0
            registry.gauge("kontext.children.open", "root") shouldBe 1.0
            registry.sum("kontext.events.rejected", "root") shouldBe REJECTED_EVENTS

            scope.close()
            child.close()

            registry.gauge("kontext.scopes.open", "root") shouldBe 0.0
            registry.gauge("kontext.children.open", "root") shouldBe 0.0
            container.close()
        }
    }

    context("spans") {
        test("cover the start and the close") {
            val tracer = RecordingTracer()
            val container = containerOf(app) { this.tracer = tracer }

            container.close()

            tracer.spans shouldBe listOf(
                "kontext.start Ok [kontext.container=root, kontext.profile=default]",
                "kontext.close Ok [kontext.container=root, kontext.profile=default]",
            )
        }

        test("mark a failed start") {
            val tracer = RecordingTracer()

            shouldThrow<ResolutionException> {
                containerOf(module("broken") { single<Greeter>(eager = true) { error(BROKEN) } }) {
                    this.tracer = tracer
                }
            }

            tracer.spans shouldBe listOf("kontext.start Error [kontext.container=root, kontext.profile=default]")
        }
    }
})
