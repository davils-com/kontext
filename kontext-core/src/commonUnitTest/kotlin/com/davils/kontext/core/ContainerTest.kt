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

import com.davils.kollect.core.health.Health
import com.davils.kollect.core.health.HealthStatus
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.definition.bind
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.fixture.BROKEN
import com.davils.kontext.core.fixture.Chicken
import com.davils.kontext.core.fixture.ENGLISH
import com.davils.kontext.core.fixture.Egg
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.GERMAN
import com.davils.kontext.core.fixture.GermanGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.HALLO
import com.davils.kontext.core.fixture.HELLO
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.LIVE
import com.davils.kontext.core.fixture.MOCK
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.UNCLOSABLE
import com.davils.kontext.core.fixture.containerOf
import com.davils.kore.core.annotation.DavilsInternal
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlin.reflect.typeOf

private const val FIRST: String = "first"

class ContainerTest : FunSpec({
    context("lifetimes") {
        test("a single is created once and a factory on every resolution") {
            val container = containerOf(
                module("app") {
                    single<Greeter> { EnglishGreeter() }
                    factory { Service(get()) }
                },
            )

            container.get<Greeter>() shouldBeSameInstanceAs container.get<Greeter>()
            container.get<Service>() shouldNotBeSameInstanceAs container.get<Service>()
            container.get<Service>().greeter shouldBeSameInstanceAs container.get<Greeter>()
        }

        test("qualifiers tell apart definitions of one type") {
            val container = containerOf(
                module("app") {
                    single<Greeter>(ENGLISH) { EnglishGreeter() }
                    single<Greeter>(GERMAN) { GermanGreeter() }
                },
            )

            container.get<Greeter>(ENGLISH).greet() shouldBe HELLO
            container.get<Greeter>(GERMAN).greet() shouldBe HALLO
            container.getOrNull<Greeter>().shouldBeNull()
        }

        test("generic types are told apart by their arguments") {
            val container = containerOf(
                module("app") {
                    single { listOf("a") }
                    single { listOf(1) }
                },
            )

            container.get<List<String>>() shouldBe listOf("a")
            container.get<List<Int>>() shouldBe listOf(1)
        }

        test("an eager single is created on start") {
            val journal = Journal()
            containerOf(module("app") { single(eager = true) { Resource("eager", journal) } })

            journal.entries shouldBe listOf("opened eager")
        }
    }

    context("bindings") {
        test("an implementation is found under its interface") {
            val container = containerOf(module("app") { single { EnglishGreeter() }.bind<Greeter>() })

            container.get<Greeter>() shouldBeSameInstanceAs container.get<EnglishGreeter>()
        }

        test("several bindings of one key are ambiguous but all found by getAll") {
            val container = containerOf(
                module("app") {
                    single { EnglishGreeter() }.bind<Greeter>()
                    single { GermanGreeter() }.bind<Greeter>()
                },
            )

            val failure = shouldThrow<ResolutionException> { container.get<Greeter>() }.failure
            failure.shouldBeInstanceOf<ResolutionFailure.Ambiguous>()
            failure.candidates.map { key -> key.type } shouldContainExactlyInAnyOrder listOf(
                typeOf<EnglishGreeter>(),
                typeOf<GermanGreeter>(),
            )
            container.getAll<Greeter>().map { greeter -> greeter.greet() } shouldContainExactly listOf(HELLO, HALLO)
        }

        test("getAll finds qualified definitions and is empty for an unknown type") {
            val container = containerOf(
                module("app") {
                    single<Greeter>(ENGLISH) { EnglishGreeter() }
                    single<Greeter> { GermanGreeter() }
                },
            )

            container.getAll<Greeter>().size shouldBe 2
            container.getAll<Request>() shouldBe emptyList()
        }
    }

    context("profiles") {
        test("the container provides its profile") {
            containerOf(profile = LIVE).get<Profile>() shouldBe LIVE
        }

        test("variants exist only in their profiles") {
            val greeters = module("greeters") {
                variant(LIVE) { single<Greeter> { EnglishGreeter() } }
                variant(setOf(MOCK, Profile.DEFAULT)) { single<Greeter> { GermanGreeter() } }
            }

            val live = containerOf(greeters, profile = LIVE)
            val mock = containerOf(greeters, profile = MOCK)
            val unnamed = containerOf(greeters)
            val other = containerOf(greeters, profile = Profile("other"))

            live.get<Greeter>().greet() shouldBe HELLO
            mock.get<Greeter>().greet() shouldBe HALLO
            unnamed.get<Greeter>().greet() shouldBe HALLO
            other.getOrNull<Greeter>().shouldBeNull()
        }
    }

    context("assisted") {
        test("an assisted factory takes its argument at the call") {
            val container = containerOf(
                module("app") {
                    single<Greeter> { EnglishGreeter() }
                    assisted<String, Request> { id -> Request("$id-${get<Greeter>().greet()}") }
                },
            )

            val create = container.get<(String) -> Request>()

            create("a").id shouldBe "a-hello"
            create shouldBeSameInstanceAs container.get<(String) -> Request>()
        }
    }

    context("failures") {
        test("a missing definition names the path to it") {
            val container = containerOf(module("app") { factory { Service(get()) } })

            val exception = shouldThrow<ResolutionException> { container.get<Service>() }

            val failure = exception.failure.shouldBeInstanceOf<ResolutionFailure.Missing>()
            failure.key.type shouldBe typeOf<Greeter>()
            failure.path.map { key -> key.type } shouldBe listOf(typeOf<Service>())
            exception.message shouldBe "Cannot resolve ${failure.key} (missing) along ${failure.path.first()} -> " +
                "${failure.key}"
        }

        test("a failing factory keeps its cause") {
            val broken = module("broken") { single<Greeter> { error(BROKEN) } }
            val container = containerOf(broken)

            val exception = shouldThrow<ResolutionException> { container.get<Greeter>() }

            exception.failure.shouldBeInstanceOf<ResolutionFailure.Failed>()
            exception.cause?.message shouldBe BROKEN
        }

        test("a cycle is reported instead of recursing") {
            val container = containerOf(
                module("app") {
                    single { Chicken(get()) }
                    single { Egg(lazyOf(get<Chicken>())) }
                },
            )

            val failure = shouldThrow<ResolutionException> { container.get<Chicken>() }.failure

            failure.shouldBeInstanceOf<ResolutionFailure.Circular>()
            failure.path.map { key -> key.type } shouldBe listOf(typeOf<Chicken>(), typeOf<Egg>())
        }

        test("a lazy resolution breaks a cycle") {
            val container = containerOf(
                module("app") {
                    single { Chicken(get()) }
                    single { Egg(getLazy()) }
                },
            )

            val chicken = container.get<Chicken>()

            chicken.egg.chicken.value shouldBeSameInstanceAs chicken
            container.getLazy<Chicken>().value shouldBeSameInstanceAs chicken
        }

        test("a chain beyond the deepest resolution is refused") {
            val depth = ContainerLimits.MAX_RESOLUTION_DEPTH + 1
            val chain = module("chain") {
                for (index in 0 until depth) {
                    factory(Qualifier("link.l$index")) {
                        "$index" + getOrNull<String>(Qualifier("link.l${index + 1}"))
                    }
                }
            }
            val container = containerOf(chain)

            val failure = shouldThrow<ResolutionException> { container.get<String>(Qualifier("link.l0")) }.failure

            failure.shouldBeInstanceOf<ResolutionFailure.TooDeep>()
            failure.reason shouldBe "too-deep"
        }

        test("an eager failure closes what was created and keeps the close failures") {
            val journal = Journal()
            val broken = module("app") {
                single(eager = true, close = { resource: Resource -> resource.close() }) {
                    Resource(FIRST, journal)
                }
                single(eager = true, close = { _: Request -> error(UNCLOSABLE) }) { Request("second") }
                single<Greeter>(eager = true) { error(BROKEN) }
            }

            val exception = shouldThrow<ResolutionException> { containerOf(broken) }

            journal.entries shouldBe listOf("opened $FIRST", "closed $FIRST")
            exception.suppressedExceptions.map { suppressed -> suppressed.message } shouldBe listOf("unclosable")
        }
    }

    context("closing") {
        test("closes instances in the reverse order of their creation") {
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    single(ENGLISH, close = { resource: Resource -> resource.close() }) { Resource("a", journal) }
                    single(GERMAN, close = { resource: Resource -> resource.close() }) {
                        get<Resource>(ENGLISH)
                        Resource("b", journal)
                    }
                },
            )
            container.get<Resource>(GERMAN)

            val report = container.close()

            journal.entries shouldBe listOf("opened a", "opened b", "closed b", "closed a")
            report.isClean shouldBe true
            container.state.value shouldBe ContainerState.Closed
            container.close() shouldBe CloseReport.AlreadyClosed
        }

        test("a failing close function does not stop the others") {
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    single(close = { resource: Resource -> resource.close() }) { Resource("a", journal) }
                    single(close = { _: Request -> error(UNCLOSABLE) }) { Request("b") }
                },
            )
            container.get<Resource>()
            container.get<Request>()

            val report = container.close().shouldBeInstanceOf<CloseReport.Closed>()

            report.isClean shouldBe false
            val failure = report.failures.single()
            failure.key.type shouldBe typeOf<Request>()
            failure.cause.message shouldBe UNCLOSABLE
            journal.entries shouldBe listOf("opened a", "closed a")
        }

        test("a closed container resolves nothing") {
            val container = containerOf(module("app") { single<Greeter> { EnglishGreeter() } })
            container.close()

            val failure = shouldThrow<ResolutionException> { container.get<Greeter>() }.failure

            failure.shouldBeInstanceOf<ResolutionFailure.Closed>()
        }

        test("runs close handlers once closed and at once when closed already") {
            val journal = Journal()
            val container = containerOf()
            container.onClosed { journal.write(FIRST) }
            container.onClosed { error("ignored") }

            container.close()
            container.onClosed { journal.write("late") }

            journal.entries shouldBe listOf(FIRST, "late")
        }
    }

    context("state") {
        test("tells its name, profile, definitions, scopes and health") {
            val container = containerOf(
                module("app") {
                    single<Greeter> { EnglishGreeter() }
                    scope(ScopeKey("request")) { factory { Request("r") } }
                },
                profile = MOCK,
            ) { name = ContainerName("app") }

            container.name shouldBe ContainerName("app")
            container.profile shouldBe MOCK
            container.definitions.map { key -> key.type } shouldContainExactlyInAnyOrder listOf(
                typeOf<Profile>(),
                typeOf<Greeter>(),
            )
            container.scopes shouldBe setOf(ScopeKey("request"))
            container.check() shouldBe Health.Up
            container.toString() shouldBe "Container(app, mock, Running)"

            container.close()

            container.check().status shouldBe HealthStatus.Down
        }
    }
})
