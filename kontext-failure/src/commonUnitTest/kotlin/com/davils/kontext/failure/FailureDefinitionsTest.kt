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

package com.davils.kontext.failure

import com.davils.knot.failure.FailureHandler
import com.davils.knot.failure.FailureOrigin
import com.davils.knot.failure.FailureReport
import com.davils.knot.shutdown.Shutdown
import com.davils.knot.shutdown.shutdown
import com.davils.kollect.core.meterRegistry
import com.davils.konfetti.core.eventBus
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.container
import com.davils.kontext.core.module
import com.davils.kontext.failure.fixture.QuietTracer
import com.davils.kontext.failure.fixture.RecordingFailureSink
import com.davils.kontext.failure.fixture.StuckFailureSink
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlin.time.Duration.Companion.milliseconds

private const val MODULE: String = "failures"
private const val MEMORY: String = "memory"
private val ORIGIN: FailureOrigin = FailureOrigin.Coroutine("test")
private val CRASHES: Qualifier = Qualifier("crashes")

class FailureDefinitionsTest : FunSpec({
    context("failures") {
        test("reports to the tracer, the meters and the event bus of the container") {
            val scope = CoroutineScope(currentCoroutineContext() + Job())
            val registry = meterRegistry()
            val bus = eventBus(scope)
            val quiet = QuietTracer()
            val container = container {
                tracer = quiet
                meters = registry
                events = bus
                modules(module(MODULE) { failures(qualifier = CRASHES) })
            }

            val configuration = container.get<FailureHandler>(CRASHES).configuration

            configuration.tracer shouldBe quiet
            configuration.meters shouldBe registry
            configuration.events shouldBe bus
            container.close().isClean shouldBe true
            scope.cancel()
        }

        test("leaves out what the container lacks") {
            val container = container { modules(module(MODULE) { failures() }) }

            val handler = container.get<FailureHandler>()

            handler.configuration.meters.shouldBeNull()
            handler.configuration.events.shouldBeNull()
            container.close()
            handler.isClosed shouldBe true
        }

        test("initiates the shutdown the container declares on a fatal failure") {
            val target = shutdown(this)
            val sink = RecordingFailureSink()
            val container = container {
                modules(
                    module(MODULE) {
                        single<Shutdown> { target }
                        failures {
                            classify { fatal<IllegalStateException>() }
                            sinks { sink(MEMORY, sink) }
                        }
                    },
                )
            }

            val report = container.get<FailureHandler>().report(IllegalStateException("broken"), ORIGIN)

            report.shouldBeInstanceOf<FailureReport.Recorded>()
            target.isStarted shouldBe true
            val written = sink.reports.single()
            written.failure.cause.message shouldBe "broken"
            container.close()
        }

        test("writes the reports it took in before the container closes it") {
            val sink = RecordingFailureSink()
            val container = container {
                modules(module(MODULE) { failures { sinks { sink(MEMORY, sink) } } })
            }
            val handler = container.get<FailureHandler>()

            handler.submit(IllegalArgumentException("late"), ORIGIN)
            val closed = container.close()

            closed.isClean shouldBe true
            handler.isClosed shouldBe true
            val written = sink.reports.single()
            written.failure.cause.message shouldBe "late"
        }

        test("closes the handler after the drain timeout when a sink hangs") {
            val container = container {
                modules(
                    module(MODULE) {
                        failures(drainTimeout = 20.milliseconds) { sinks { sink("stuck", StuckFailureSink()) } }
                    },
                )
            }
            val handler = container.get<FailureHandler>()
            handler.submit(IllegalArgumentException("stuck"), ORIGIN)

            container.close().isClean shouldBe true

            handler.isClosed shouldBe true
        }
    }
})
