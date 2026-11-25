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

package com.davils.kontext.shutdown

import com.davils.knot.shutdown.ShutdownPhase
import com.davils.knot.shutdown.ShutdownRegistration
import com.davils.knot.shutdown.report.TaskOutcome
import com.davils.knot.shutdown.shutdown
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.container
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.module
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private const val UNCLOSABLE: String = "unclosable"

private val unclosable = module("unclosable") { single(close = { _: String -> error(UNCLOSABLE) }) { "value" } }

class ShutdownBindingsTest : FunSpec({
    test("closes the container in its phase of the shutdown") {
        val shutdown = shutdown(this)
        val container = container { name = ContainerName("app") }

        val registration = shutdown.adopt(container, ShutdownPhase.Stop)
        val report = shutdown.initiate()

        registration.shouldBeInstanceOf<ShutdownRegistration.Registered>()
        val task = report.tasks.single()
        task.name.value shouldBe "container:app"
        task.phase shouldBe ShutdownPhase.Stop
        task.isCompleted shouldBe true
        container.state.value shouldBe ContainerState.Closed
    }

    test("fails the task when instances fail to close") {
        val shutdown = shutdown(this)
        val container = container { modules(unclosable) }
        container.get<String>()
        shutdown.adopt(container)

        val outcome = shutdown
            .initiate()
            .tasks
            .single()
            .outcome

        val failed = outcome.shouldBeInstanceOf<TaskOutcome.Failed>()
        val cause = failed.cause.shouldBeInstanceOf<ContainerCloseException>()
        cause.container shouldBe ContainerName("root")
        val failure = cause.failures.single()
        failure.cause.message shouldBe UNCLOSABLE
        cause.message shouldBe "1 instances of the container root failed to close"
    }

    test("unregisters the task once the container is closed in another way") {
        val shutdown = shutdown(this)
        val container = container { }
        shutdown.adopt(container)

        container.close()

        shutdown.metrics().registeredTasks shouldBe 0
        shutdown.initiate().tasks shouldBe emptyList()
    }

    test("is refused by a shutdown that already ran") {
        val shutdown = shutdown(this)
        shutdown.initiate()
        val container = container { }

        shutdown.adopt(container).shouldBeInstanceOf<ShutdownRegistration.Refused>()

        container.close().isClean shouldBe true
    }
})
