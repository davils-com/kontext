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

package com.davils.kontext.krypt

import com.davils.kollect.core.meterRegistry
import com.davils.kollect.core.trace.Tracer
import com.davils.konfetti.core.eventBus
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.container
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.module
import com.davils.kontext.krypt.fixture.QuietTracer
import com.davils.kontext.krypt.fixture.withScope
import com.davils.krypt.core.Environment
import com.davils.krypt.core.policy.CryptoPolicy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

private const val MODULE: String = "crypto"
private val STRICT: Qualifier = Qualifier("strict")

class CryptoDefinitionsTest : FunSpec({
    context("cryptoEnvironment") {
        test("builds the environment from the tracer, the meters and the event bus of the container") {
            withScope { scope ->
                val registry = meterRegistry()
                val bus = eventBus(scope)
                val quiet = QuietTracer()
                val container = container {
                    tracer = quiet
                    meters = registry
                    events = bus
                    modules(module(MODULE) { cryptoEnvironment() })
                }

                val environment = container.get<Environment>()

                environment.tracer shouldBe quiet
                environment.meters shouldBe registry
                environment.events shouldBe bus
                environment.policy shouldBe CryptoPolicy.Modern
                container.get<Environment>() shouldBeSameInstanceAs environment
                container.close().isClean shouldBe true
            }
        }

        test("leaves out what the container lacks and applies the configuration last") {
            val container = container {
                modules(
                    module(MODULE) {
                        cryptoEnvironment(qualifier = STRICT) {
                            policy = CryptoPolicy.Cnsa2
                            eventCapacity = 1
                        }
                    },
                )
            }

            val environment = container.get<Environment>(STRICT)

            environment.policy shouldBe CryptoPolicy.Cnsa2
            environment.eventCapacity shouldBe 1
            environment.tracer shouldBe Tracer.None
            environment.meters.shouldBeNull()
            environment.events.shouldBeNull()
            container.close()
        }

        test("fails the resolution of an environment krypt refuses") {
            val container = container { modules(module(MODULE) { cryptoEnvironment { eventCapacity = 0 } }) }

            shouldThrow<ResolutionException> { container.get<Environment>() }

            container.close()
        }
    }
})
