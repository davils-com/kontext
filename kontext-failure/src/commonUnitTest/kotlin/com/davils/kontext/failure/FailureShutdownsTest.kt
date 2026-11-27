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
import com.davils.knot.shutdown.ShutdownPhase
import com.davils.knot.shutdown.ShutdownRegistration
import com.davils.knot.shutdown.shutdown
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.container
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.module
import com.davils.kontext.failure.fixture.RecordingFailureSink
import com.davils.kontext.shutdown.adopt
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private const val MODULE: String = "failures"
private const val MEMORY: String = "memory"

class FailureShutdownsTest : FunSpec({
    context("adoptFailures") {
        test("drains the handler in the drain phase and closes it with the container") {
            val sink = RecordingFailureSink()
            val shutdown = shutdown(this)
            val container = container {
                name = ContainerName("app")
                modules(module(MODULE) { failures { sinks { sink(MEMORY, sink) } } })
            }
            val handler = container.get<FailureHandler>()

            val registration = shutdown.adoptFailures(container)
            shutdown.adopt(container)
            handler.submit(IllegalArgumentException("late"), FailureOrigin.Coroutine("test"))
            val report = shutdown.initiate()

            registration.shouldBeInstanceOf<ShutdownRegistration.Registered>()
            report.isClean shouldBe true
            report.tasks.map { task -> task.name.value to task.phase } shouldBe listOf(
                "failures:app-drain" to ShutdownPhase.Drain,
                "failures:app-close" to ShutdownPhase.Release,
                "container:app" to ShutdownPhase.Release,
            )
            val written = sink.reports.single()
            written.failure.cause.message shouldBe "late"
            handler.isClosed shouldBe true
        }

        test("unregisters the tasks once the container is closed in another way") {
            val shutdown = shutdown(this)
            val container = container { modules(module(MODULE) { failures() }) }
            shutdown.adoptFailures(container, name = "crashes")

            container.close()

            shutdown.metrics().registeredTasks shouldBe 0
        }

        test("is refused by a shutdown that already ran") {
            val shutdown = shutdown(this)
            shutdown.initiate()
            val container = container { modules(module(MODULE) { failures() }) }

            shutdown.adoptFailures(container).shouldBeInstanceOf<ShutdownRegistration.Refused>()

            container.close().isClean shouldBe true
        }

        test("needs a container that declares a handler") {
            val shutdown = shutdown(this)
            val container = container { }

            shouldThrow<ResolutionException> { shutdown.adoptFailures(container) }

            container.close()
        }
    }
})
