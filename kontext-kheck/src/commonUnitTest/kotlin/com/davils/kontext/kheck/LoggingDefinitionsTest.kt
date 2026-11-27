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

package com.davils.kontext.kheck

import com.davils.kheck.api.info
import com.davils.kheck.core.Logging
import com.davils.kheck.core.sink.custom.sink
import com.davils.konfetti.core.eventBus
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.container
import com.davils.kontext.core.module
import com.davils.kontext.kheck.fixture.Payments
import com.davils.kontext.kheck.fixture.RecordingLogSink
import com.davils.kontext.kheck.fixture.StuckLogSink
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TestTimeSource

private const val MODULE: String = "logging"
private const val AUDIT_NAME: String = "audit"
private const val PAYMENTS: String = "app.payments"
private const val RECORDING: String = "recording"
private val AUDIT: Qualifier = Qualifier(AUDIT_NAME)

class LoggingDefinitionsTest : FunSpec({
    context("logging") {
        test("writes every record and closes its sinks when the container closes") {
            val sink = RecordingLogSink()
            val container = container { modules(module(MODULE) { logging { sink(RECORDING, sink) } }) }

            container.logger(PAYMENTS).info { "charged" }
            container.logger<Payments>().info { "refunded" }
            val logging = container.get<Logging>()
            val report = container.close()

            report.isClean shouldBe true
            logging.isClosed shouldBe true
            sink.isClosed shouldBe true
            sink.lines.map { line -> line.record.message } shouldBe listOf("charged", "refunded")
            sink.lines.map { line -> line.record.source.logger.value }.first() shouldBe PAYMENTS
        }

        test("measures on the time source and publishes on the event bus of the container") {
            val scope = CoroutineScope(currentCoroutineContext() + Job())
            val bus = eventBus(scope)
            val time = TestTimeSource()
            val container = container {
                timeSource = time
                events = bus
                modules(module(MODULE) { logging(qualifier = AUDIT) })
            }

            val logging = container.get<Logging>(AUDIT)

            logging.configuration.timeSource shouldBe time
            logging.configuration.events shouldBe bus
            container.logger(name = AUDIT_NAME, qualifier = AUDIT) shouldBeSameInstanceAs logging.logger(AUDIT_NAME)
            container.close()
            scope.cancel()
        }

        test("leaves out an event bus the container lacks") {
            val container = container { modules(module(MODULE) { logging() }) }
            val configuration = container.get<Logging>().configuration

            configuration.events.shouldBeNull()

            container.close().isClean shouldBe true
        }

        test("fails the close when the logging does not close in time") {
            val container = container {
                modules(module(MODULE) { logging(closeTimeout = 50.milliseconds) { sink("stuck", StuckLogSink()) } })
            }
            container.logger("app").info { "stuck" }

            val report = container.close()

            val failure = report.failures.single()
            failure.cause.message.orEmpty() shouldContain "did not close within 50ms"
        }
    }
})
