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
import com.davils.kontext.core.definition.Definition
import com.davils.kontext.core.definition.bind
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.fixture.ENGLISH
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.LIVE
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.testing.RecordingContainerSink
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import kotlin.reflect.typeOf

class ValuesTest : FunSpec({
    context("names") {
        test("accept dotted names in lower case") {
            ContainerName("app.root").toString() shouldBe "app.root"
            ModuleName("app-http").value shouldBe "app-http"
            ScopeKey("a.b2").toString() shouldBe "a.b2"
            Qualifier("db.primary").toString() shouldBe "db.primary"
            Profile("live").toString() shouldBe "live"
            Profile.DEFAULT.value shouldBe "default"
        }

        test("refuse other names without showing them raw") {
            listOf(
                "",
                "App",
                "1app",
                "app.",
                "a..b",
                "a b",
                "a\u0000b",
                "a".repeat(ContainerLimits.MAX_NAME_LENGTH + 1),
            ).forEach { name ->
                shouldThrow<IllegalArgumentException> { Qualifier(name) }
                shouldThrow<IllegalArgumentException> { ContainerName(name) }
                shouldThrow<IllegalArgumentException> { ModuleName(name) }
                shouldThrow<IllegalArgumentException> { ScopeKey(name) }
                shouldThrow<IllegalArgumentException> { Profile(name) }
            }
            shouldThrow<IllegalArgumentException> { Qualifier("a\u0000b") }.message shouldContain "\\u{0000}"
        }
    }

    context("keys") {
        test("are equal by type and qualifier") {
            val plain = DefinitionKey(typeOf<Greeter>(), null)
            val qualified = DefinitionKey(typeOf<Greeter>(), ENGLISH)

            plain shouldBe DefinitionKey(typeOf<Greeter>(), null)
            plain.hashCode() shouldBe DefinitionKey(typeOf<Greeter>(), null).hashCode()
            plain shouldNotBe qualified
            plain.equals("other") shouldBe false
            plain.toString() shouldBe typeOf<Greeter>().toString()
            qualified.toString() shouldBe "${typeOf<Greeter>()}@english"
        }
    }

    context("declarations") {
        test("refuse a binding after the build, a second binding of one key and a binding to itself") {
            val declared = mutableListOf<Definition<EnglishGreeter>>()
            module("app") {
                val definition = single { EnglishGreeter() }
                definition.bind<Greeter>()
                shouldThrow<IllegalArgumentException> { definition.bind<Greeter>() }
                shouldThrow<IllegalArgumentException> { definition.bind<EnglishGreeter>() }
                definition.toString() shouldBe "Definition(${definition.key})"
                declared += definition
            }

            shouldThrow<IllegalStateException> { declared.single().bind<Any>() }
        }

        test("refuse an empty and a nested variant") {
            module("app") {
                shouldThrow<IllegalArgumentException> { variant(emptySet()) {} }
                variant(LIVE) { shouldThrow<IllegalStateException> { variant(LIVE) {} } }
            }
        }

        test("describe their builders and modules") {
            val included = module("included") {}
            val app = module("app") {
                include(included)
                this.toString() shouldBe "ModuleBuilder(app)"
                name shouldBe ModuleName("app")
                scope(REQUEST, within = SESSION) {
                    toString() shouldBe "ScopeBuilder(request, within=session)"
                    key shouldBe REQUEST
                    within shouldBe SESSION
                }
                scope(SESSION) { toString() shouldBe "ScopeBuilder(session, within=none)" }
            }

            app.toString() shouldBe "Module(app)"
            app.includes shouldBe listOf(included)
        }

        test("describe a seed") {
            val seed = ScopeSeed()
            seed.provide("value")

            seed.toString() shouldBe "ScopeSeed([${typeOf<String>()}])"
        }
    }

    context("reports") {
        test("describe themselves") {
            CloseReport.AlreadyClosed.isClean shouldBe true
            CloseReport.AlreadyClosed.toString() shouldBe "AlreadyClosed"
            val container = containerOf(
                module("app") { single<Greeter>(close = { _: Greeter -> error("x") }) { EnglishGreeter() } },
            )
            container.get<Greeter>()

            val report = container.close()

            report.toString() shouldBe "Closed(failures=[DisposalFailure(${DefinitionKey(typeOf<Greeter>(), null)}, " +
                "IllegalStateException)])"
        }

        test("failures describe themselves and their reason") {
            val sink = RecordingContainerSink()
            val container = containerOf(module("app") { single<Greeter> { error("x") } }) { this.sink = sink }

            val failed = shouldThrow<ResolutionException> { container.get<Greeter>() }.failure
            val missing = shouldThrow<ResolutionException> { container.get<Service>() }.failure

            failed.reason shouldBe "failed"
            failed.toString() shouldStartWith "Failed("
            failed.toString() shouldContain "cause=IllegalStateException"
            missing.toString() shouldStartWith "Missing("
            ResolutionFailure.Circular(failed.key, emptyList()).toString() shouldStartWith "Circular("
            ResolutionFailure.Circular(failed.key, emptyList()).reason shouldBe "circular"
            ResolutionFailure.Ambiguous(failed.key, emptyList(), emptyList()).toString() shouldStartWith "Ambiguous("
            ResolutionFailure.Unprovided(failed.key, emptyList()).toString() shouldStartWith "Unprovided("
            ResolutionFailure.Closed(failed.key, emptyList()).toString() shouldStartWith "Closed("
            ResolutionFailure.Closed(failed.key, emptyList()).reason shouldBe "closed"
            ResolutionFailure.TooDeep(failed.key, emptyList()).toString() shouldBe "TooDeep(${failed.key}, depth=0)"
            sink.of<ContainerEvent.ResolutionFailed>().size shouldBe 2
        }

        test("events describe themselves") {
            val sink = RecordingContainerSink()
            val container = containerOf(
                module("app") {
                    single<Greeter> { EnglishGreeter() }
                    scope(SESSION) {}
                },
            ) { this.sink = sink }
            container.get<Greeter>()
            container.openScope(SESSION).close()
            shouldThrow<ResolutionException> { container.get<Service>() }
            container.child { }
            container.close()

            sink.events.map { event -> event.toString().substringBefore("(") } shouldBe listOf(
                "Started",
                "InstanceCreated",
                "ScopeOpened",
                "ScopeClosed",
                "ResolutionFailed",
                "Started",
                "ChildOpened",
                "Closed",
                "Closed",
            )
            sink.events.first().toString() shouldBe "Started(root, default, definitions=2)"
            val created = sink.of<ContainerEvent.InstanceCreated>().single()
            val opened = sink.of<ContainerEvent.ChildOpened>().single()
            val closed = sink.of<ContainerEvent.Closed>().last()
            created.lifetime.name shouldBe "Single"
            opened.toString() shouldBe "ChildOpened(root, child)"
            closed.toString() shouldBe "Closed(root, failures=0)"
            sink.toString() shouldBe "RecordingContainerSink(9 events)"
        }
    }
})
