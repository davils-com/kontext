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
import com.davils.kontext.core.error.ContainerClosedException
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.error.ScopeNestingException
import com.davils.kontext.core.error.ScopeNotDeclaredException
import com.davils.kontext.core.error.ScopeSeedException
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Handler
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.PAGE
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlin.reflect.typeOf

private val web: Module = module("web") {
    single<Greeter> { EnglishGreeter() }
    single { Service(get()) }
    scope(SESSION) {
        scoped { Journal() }
    }
    scope(REQUEST, within = SESSION) {
        provided<Request>()
        scoped { Handler(get(), get()) }
        factory { Resource("request", get()) }
        assisted<String, Resource> { name -> Resource(name, get()) }
    }
}

class ScopeTest : FunSpec({
    context("resolution") {
        test("a scoped instance lives once per scope and sees its enclosing scopes and the container") {
            val container = containerOf(web)
            val session = container.openScope(SESSION)
            val first = session.openScope(REQUEST) { provide(Request("1")) }
            val second = session.openScope(REQUEST) { provide(Request("2")) }

            first.get<Handler>() shouldBeSameInstanceAs first.get<Handler>()
            first.get<Handler>() shouldNotBeSameInstanceAs second.get<Handler>()
            first.get<Handler>().request.id shouldBe "1"
            first.get<Handler>().service shouldBeSameInstanceAs container.get<Service>()
            first.get<Journal>() shouldBeSameInstanceAs second.get<Journal>()
        }

        test("factories and assisted factories of a scope resolve within it") {
            val container = containerOf(web)
            val request = container.openScope(SESSION).openScope(REQUEST) { provide(Request("1")) }

            request.get<Resource>().name shouldBe "request"
            request.get<(String) -> Resource>()("named").name shouldBe "named"
            request.get<Journal>().entries shouldBe listOf("opened request", "opened named")
        }

        test("the container does not see the definitions of a scope") {
            val container = containerOf(web)

            container.getOrNull<Journal>() shouldBe null
        }

        test("getAll collects from the scope and the container") {
            val container = containerOf(
                module("app") {
                    single<Greeter> { EnglishGreeter() }
                    scope(SESSION) { scoped<Greeter>(Qualifier("scoped")) { EnglishGreeter() } }
                },
            )

            container.openScope(SESSION).getAll<Greeter>().size shouldBe 2
        }

        test("resolves optionally and lazily") {
            val session = containerOf(web).openScope(SESSION)
            val request = session.openScope(REQUEST) { provide(Request("1")) }
            val handler = request.getOrNull<Handler>()

            handler.shouldNotBeNull().request.id shouldBe "1"
            request.getLazy<Handler>().value shouldBeSameInstanceAs request.get<Handler>()
        }

        test("an unprovided value fails") {
            val session = containerOf(web).openScope(SESSION)
            val request = session.openScope(REQUEST)

            val failure = shouldThrow<ResolutionException> { request.get<Handler>() }.failure

            failure.shouldBeInstanceOf<ResolutionFailure.Unprovided>()
            failure.reason shouldBe "unprovided"
        }
    }

    context("opening") {
        test("refuses an undeclared scope, a wrong place and an undeclared value") {
            val container = containerOf(web)

            shouldThrow<ScopeNotDeclaredException> { container.openScope(PAGE) }.scope shouldBe PAGE
            val misplaced = shouldThrow<ScopeNestingException> { container.openScope(REQUEST) }
            misplaced.expected shouldBe SESSION
            misplaced.actual shouldBe null
            misplaced.message shouldBe "The scope request is opened within the scope session, not within the container"
            val seeded = shouldThrow<ScopeSeedException> { container.openScope(SESSION) { provide(Request("1")) } }
            seeded.scope shouldBe SESSION
            val session = container.openScope(SESSION)
            val nested = shouldThrow<ScopeNestingException> { session.openScope(SESSION) }
            nested.message shouldBe "The scope session is opened within the container, not within the scope session"
            val handler = Handler(Request("1"), Service(EnglishGreeter()))
            val unseeded = shouldThrow<ScopeSeedException> { session.openScope(REQUEST) { provide(handler) } }
            unseeded.definition.type shouldBe typeOf<Handler>()
        }

        test("refuses a value provided twice") {
            val container = containerOf(web)
            val session = container.openScope(SESSION)

            shouldThrow<IllegalArgumentException> {
                session.openScope(REQUEST) {
                    provide(Request("1"))
                    provide(Request("2"))
                }
            }
        }

        test("a closed container opens no scope") {
            val container = containerOf(web)
            container.close()

            shouldThrow<ContainerClosedException> { container.openScope(SESSION) }.container shouldBe container.name
        }

        test("tells its key, id and whether it is open") {
            val scope = containerOf(web).openScope(SESSION)

            scope.key shouldBe SESSION
            scope.isOpen shouldBe true
            scope.toString() shouldStartWith "Scope(session, "
        }
    }

    context("closing") {
        test("closes its scoped instances and the scopes opened within it") {
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    scope(
                        SESSION,
                    ) { scoped(close = { resource: Resource -> resource.close() }) { Resource("s", journal) } }
                    scope(REQUEST, within = SESSION) {
                        scoped(
                            Qualifier("r"),
                            close = { resource: Resource -> resource.close() },
                        ) { Resource("r", journal) }
                    }
                },
            )
            val session = container.openScope(SESSION)
            val request = session.openScope(REQUEST)
            session.get<Resource>()
            request.get<Resource>(Qualifier("r"))

            session.close().isClean shouldBe true

            request.isOpen shouldBe false
            journal.entries shouldBe listOf("opened s", "opened r", "closed r", "closed s")
            session.close() shouldBe CloseReport.AlreadyClosed
            shouldThrow<ResolutionException> { session.get<Resource>() }
                .failure
                .shouldBeInstanceOf<ResolutionFailure.Closed>()
        }

        test("a nested scope closed on its own leaves its parent open") {
            val container = containerOf(web)
            val session = container.openScope(SESSION)
            val request = session.openScope(REQUEST) { provide(Request("1")) }

            request.close()

            session.isOpen shouldBe true
            session.close().isClean shouldBe true
        }

        test("closing the container closes its open scopes") {
            val journal = Journal()
            val container = containerOf(
                module("app") {
                    scope(
                        SESSION,
                    ) { scoped(close = { resource: Resource -> resource.close() }) { Resource("s", journal) } }
                },
            )
            val session = container.openScope(SESSION)
            session.get<Resource>()

            container.close()

            session.isOpen shouldBe false
            journal.entries shouldBe listOf("opened s", "closed s")
        }
    }
})
