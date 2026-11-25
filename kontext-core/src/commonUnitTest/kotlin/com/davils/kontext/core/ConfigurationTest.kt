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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.core

import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.definition.bind
import com.davils.kontext.core.error.ContainerViolationCodes
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.GermanGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.HALLO
import com.davils.kontext.core.fixture.HELLO
import com.davils.kontext.core.fixture.LIVE
import com.davils.kontext.core.fixture.MOCK
import com.davils.kontext.core.fixture.PAGE
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.containerOf
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.validation.report.ValidationException
import com.davils.kore.core.validation.violation.ViolationCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

private suspend fun violationsOf(block: ContainerBuilder.() -> Unit): List<ViolationCode> {
    val exception = shouldThrow<ValidationException> { container(block = block) }
    return exception.report.errors.map { violation -> violation.code }
}

private val greeter: Module = module("greeter") { single<Greeter> { EnglishGreeter() } }

private const val VARIANTS: String = "variants"
private const val SCOPES: String = "scopes"
private const val REQUESTED: String = "request"

class ConfigurationTest : FunSpec({
    context("definitions") {
        test("refuses a key declared twice in one profile") {
            violationsOf {
                modules(greeter, module("other") { single<Greeter> { GermanGreeter() } })
            } shouldContainExactly listOf(ContainerViolationCodes.DUPLICATE_DEFINITION)
        }

        test("accepts a key declared in different profiles") {
            val variants = module(VARIANTS) {
                variant(LIVE) { single<Greeter> { EnglishGreeter() } }
                variant(MOCK) { single<Greeter> { GermanGreeter() } }
            }

            val greeter = containerOf(variants, profile = MOCK).get<Greeter>()

            greeter.greet() shouldBe HALLO
        }

        test("refuses a key of all profiles next to one of a variant") {
            val variants = module(VARIANTS) { variant(LIVE) { single<Greeter> { GermanGreeter() } } }

            violationsOf { modules(greeter, variants) } shouldContainExactly
                listOf(ContainerViolationCodes.DUPLICATE_DEFINITION)
        }

        test("refuses a key declared twice in overlapping variants or in one scope") {
            val variants = module(VARIANTS) {
                variant(LIVE) { single<Greeter> { GermanGreeter() } }
                variant(setOf(MOCK, LIVE)) { single<Greeter> { EnglishGreeter() } }
            }
            val scoped = module("scoped") {
                scope(SESSION) {
                    factory { "a" }
                    factory { "b" }
                }
            }

            violationsOf { modules(variants, scoped) } shouldContainExactly listOf(
                ContainerViolationCodes.DUPLICATE_DEFINITION,
                ContainerViolationCodes.DUPLICATE_DEFINITION,
            )
            violationsOf { modules(variants, greeter) }.size shouldBe 1
        }

        test("refuses the key of the profile") {
            violationsOf { modules(module("app") { single { Profile("x") } }) } shouldContainExactly
                listOf(ContainerViolationCodes.RESERVED_DEFINITION)
        }

        test("refuses two modules of one name and includes an included module once") {
            violationsOf { modules(greeter, module("greeter") {}) } shouldContainExactly
                listOf(ContainerViolationCodes.DUPLICATE_MODULE)

            val included = module("all") { include(greeter, greeter) }
            val container = containerOf(included, greeter)

            container.get<Greeter>().greet() shouldBe HELLO
        }
    }

    context(VARIANTS) {
        test("refuses an include within a variant") {
            val exception = shouldThrow<IllegalStateException> {
                module("app") { variant(MOCK) { include(greeter) } }
            }

            exception.message shouldContain "within a variant"
        }

        test("declares a scope of a variant only in the profiles of the variant") {
            val variants = module(VARIANTS) {
                scope(SESSION) {}
                variant(LIVE) { scope(REQUEST) { factory { REQUESTED } } }
                variant(MOCK) { scope(REQUEST, within = SESSION) { factory { REQUESTED } } }
            }

            val live = containerOf(variants, profile = LIVE)
            val other = containerOf(variants)

            live.scopes shouldBe setOf(SESSION, REQUEST)
            live.openScope(REQUEST).get<String>() shouldBe REQUESTED
            other.scopes shouldBe setOf(SESSION)
        }
    }

    context(SCOPES) {
        test("refuses a scope declared within different scopes") {
            violationsOf {
                modules(
                    module("a") {
                        scope(SESSION) {}
                        scope(PAGE) {}
                        scope(REQUEST, within = SESSION) {}
                    },
                    module("b") { scope(REQUEST, within = PAGE) {} },
                )
            } shouldContainExactly listOf(ContainerViolationCodes.CONFLICTING_SCOPE_NESTING)
        }

        test("refuses a scope within an undeclared scope") {
            violationsOf { modules(module("a") { scope(REQUEST, within = SESSION) {} }) } shouldContainExactly
                listOf(ContainerViolationCodes.UNKNOWN_SCOPE)
        }

        test("refuses scopes within each other") {
            violationsOf {
                modules(
                    module("a") {
                        scope(REQUEST, within = SESSION) {}
                        scope(SESSION, within = REQUEST) {}
                    },
                )
            } shouldContainExactly listOf(
                ContainerViolationCodes.CYCLIC_SCOPE_NESTING,
                ContainerViolationCodes.CYCLIC_SCOPE_NESTING,
            )
        }

        test("refuses a scoped definition that hides one of the container or of an enclosing scope") {
            violationsOf {
                modules(
                    greeter,
                    module(SCOPES) {
                        scope(SESSION) {
                            scoped<Greeter> { EnglishGreeter() }
                            factory { "session" }
                        }
                        scope(REQUEST, within = SESSION) { factory { REQUESTED } }
                    },
                )
            } shouldContainExactly listOf(
                ContainerViolationCodes.SHADOWED_DEFINITION,
                ContainerViolationCodes.SHADOWED_DEFINITION,
            )
        }

        test("accepts a scope within a scope without definitions") {
            val container = containerOf(
                module(SCOPES) {
                    scope(SESSION) {}
                    scope(REQUEST, within = SESSION) { factory { REQUESTED } }
                },
            )

            val session = container.openScope(SESSION)

            session.openScope(REQUEST).get<String>() shouldBe REQUESTED
        }

        test("refuses a definition of a child that hides one of its parent") {
            val parent = containerOf(greeter)

            val exception = shouldThrow<ValidationException> { parent.child { modules(greeter) } }

            exception.report.errors
                .single()
                .code shouldBe ContainerViolationCodes.SHADOWED_DEFINITION
            exception.message shouldContain "the container root"
        }
    }

    context("aliases") {
        test("refuses an alias that collides with a definition of the same place") {
            violationsOf {
                modules(greeter, module("alias") { single { GermanGreeter() }.bind<Greeter>() })
            } shouldContainExactly listOf(ContainerViolationCodes.DUPLICATE_DEFINITION)
            violationsOf {
                modules(
                    module(SCOPES) {
                        scope(SESSION) {
                            scoped<Greeter> { EnglishGreeter() }
                            scoped { GermanGreeter() }.bind<Greeter>()
                        }
                    },
                )
            } shouldContainExactly listOf(ContainerViolationCodes.DUPLICATE_DEFINITION)
        }

        test("refuses a replacement whose alias collides with a kept definition") {
            val german = module("german") { single { GermanGreeter() } }

            violationsOf {
                modules(greeter, german)
                replace(module("fake") { single { GermanGreeter() }.bind<Greeter>() })
            } shouldContainExactly listOf(ContainerViolationCodes.DUPLICATE_DEFINITION)
        }

        test("refuses a scoped alias that hides a definition of the container") {
            val exception = shouldThrow<ValidationException> {
                containerOf(
                    greeter,
                    module(SCOPES) { scope(SESSION) { scoped { GermanGreeter() }.bind<Greeter>() } },
                )
            }

            exception.report.errors
                .single()
                .code shouldBe ContainerViolationCodes.SHADOWED_DEFINITION
            exception.message shouldContain "the container"
        }

        test("refuses an alias of a child that hides a definition of its parent") {
            val parent = containerOf(greeter)

            val exception = shouldThrow<ValidationException> {
                parent.child { modules(module("evil") { single { GermanGreeter() }.bind<Greeter>() }) }
            }

            exception.report.errors
                .single()
                .code shouldBe ContainerViolationCodes.SHADOWED_DEFINITION
            parent.get<Greeter>().greet() shouldBe HELLO
        }

        test("refuses a definition of a child that hides an alias of its parent") {
            val parent = containerOf(module("alias") { single { EnglishGreeter() }.bind<Greeter>() })

            val exception = shouldThrow<ValidationException> { parent.child { modules(greeter) } }

            exception.report.errors
                .single()
                .code shouldBe ContainerViolationCodes.SHADOWED_DEFINITION
        }
    }

    context("replacements") {
        test("replace a declared definition") {
            val container = containerOf(greeter) {
                replace(module("fake") { single<Greeter> { GermanGreeter() } })
            }

            container.get<Greeter>().greet() shouldBe HALLO
        }

        test("replace a definition only in the profiles of the replacement") {
            val fake = module("fake") { variant(MOCK) { single<Greeter> { GermanGreeter() } } }

            val live = containerOf(greeter, profile = LIVE) { replace(fake) }
            val mocked = containerOf(greeter, profile = MOCK) { replace(fake) }

            live.get<Greeter>().greet() shouldBe HELLO
            mocked.get<Greeter>().greet() shouldBe HALLO
        }

        test("refuse a replacement of nothing and duplicates among replacements") {
            violationsOf {
                modules(greeter)
                replace(
                    module("fake") {
                        single<Greeter> { GermanGreeter() }
                        single { "unknown" }
                    },
                    module("other") { single<Greeter> { GermanGreeter() } },
                )
            } shouldContainExactly listOf(
                ContainerViolationCodes.DUPLICATE_DEFINITION,
                ContainerViolationCodes.UNKNOWN_REPLACEMENT,
            )
        }
    }

    context("builder") {
        test("validates without throwing and refuses changes after its evaluation") {
            val builder = ContainerBuilder(Profile.DEFAULT, null)
            builder.modules(greeter, greeter)

            builder.validate().isValid shouldBe true
            shouldThrow<IllegalStateException> { builder.modules(greeter) }
            shouldThrow<IllegalStateException> { builder.replace(greeter) }
            builder.toString() shouldBe "ContainerBuilder(root, default)"
        }

        test("keeps the declared settings in its configuration") {
            val builder = ContainerBuilder(MOCK, null)
            builder.name = ContainerName("app")
            builder.modules(greeter)

            val configuration = builder.build()

            configuration.name shouldBe ContainerName("app")
            configuration.profile shouldBe MOCK
            configuration.modules shouldBe listOf(greeter)
            configuration.replacements shouldBe emptyList()
            configuration.meters shouldBe null
            configuration.events shouldBe null
            configuration.sink shouldBe null
            configuration.toString() shouldBe "ContainerConfiguration(app, mock, modules=1)"
        }
    }
})
