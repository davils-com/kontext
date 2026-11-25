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
import com.davils.knot.shutdown.shutdown
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.container
import com.davils.kontext.core.module
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.ConcurrentLinkedQueue

class OrderlyShutdownTest : FunSpec({
    test("an orderly shutdown closes each container in its phase after the work it depends on") {
        val journal = ConcurrentLinkedQueue<String>()
        val shutdown = shutdown(this) {
            drain { task("requests") { action { journal += "requests drained" } } }
        }
        val application = container {
            name = ContainerName("application")
            modules(module("app") { single(close = { _: String -> journal += "application closed" }) { "app" } })
        }
        val plugin = application.child {
            name = ContainerName("plugin")
            modules(module("plugin") { single(close = { _: Int -> journal += "plugin closed" }) { 1 } })
        }
        application.get<String>()
        plugin.get<Int>()
        shutdown.adopt(plugin, ShutdownPhase.Stop)
        shutdown.adopt(application, ShutdownPhase.Release)

        val report = shutdown.initiate()

        report.isClean shouldBe true
        journal.toList() shouldBe listOf("requests drained", "plugin closed", "application closed")
        report.tasks.map { task -> task.name.value } shouldBe listOf(
            "requests",
            "container:plugin",
            "container:application",
        )
    }
})
