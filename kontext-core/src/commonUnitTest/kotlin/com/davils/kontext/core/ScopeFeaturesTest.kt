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

import com.davils.kontext.core.error.ScopeAlreadyOpenException
import com.davils.kontext.core.error.ScopeLimitException
import com.davils.kontext.core.error.ScopeNotOpenException
import com.davils.kontext.core.error.ScopeSeedException
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.PAGE
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.containerOf
import com.davils.kore.core.validation.report.ValidationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.isActive

private val USER: ScopeId = ScopeId("user-1")

private val scopes: Module = module("scopes") {
    scope(SESSION) {
        provided<Request>()
        scoped { Journal() }
    }
    scope(PAGE) { scoped<Greeter> { EnglishGreeter() } }
    scope(REQUEST, within = SESSION) { factory { get<Request>().id } }
}

class ScopeFeaturesTest : FunSpec({
    context("ids") {
        test("find an open scope again and free its id when it closes") {
            val container = containerOf(scopes)
            val session = container.openScope(SESSION, USER)

            container.scope(USER) shouldBe session
            container.scopeOrNull(USER) shouldBe session
            session.scopeId shouldBe USER
            session.hashCode() shouldBe container.scope(USER).hashCode()
            session.close()

            container.scopeOrNull(USER).shouldBeNull()
            shouldThrow<ScopeNotOpenException> { container.scope(USER) }.id shouldBe USER
        }

        test("are unique and opened or found atomically") {
            val container = containerOf(scopes)
            val first = container.openOrGetScope(SESSION, USER) { provide(Request("1")) }

            container.openOrGetScope(SESSION, USER) shouldBe first
            shouldThrow<ScopeAlreadyOpenException> { container.openScope(SESSION, USER) }.id shouldBe USER
            shouldThrow<IllegalArgumentException> { container.openOrGetScope(PAGE, USER) }
            first.openScope(REQUEST, ScopeId("request-1")).get<String>() shouldBe "1"
        }
    }

    test("a container refuses scopes beyond its limit") {
        val container = containerOf(scopes) { scopeLimit = 1 }
        container.openScope(PAGE)

        shouldThrow<ScopeLimitException> { container.openScope(PAGE) }.limit shouldBe 1
        shouldThrow<ValidationException> { containerOf { scopeLimit = 0 } }
    }

    context("links") {
        test("let a scope resolve from another until either closes") {
            val container = containerOf(scopes)
            val session = container.openScope(SESSION) { provide(Request("1")) }
            val page = container.openScope(PAGE)

            session.link(page)
            session.link(page)

            session.get<Greeter>() shouldBeSameInstanceAs page.get<Greeter>()
            session.getAll<Greeter>().size shouldBe 1
            page.close()
            session.getOrNull<Greeter>().shouldBeNull()
        }

        test("end on unlink and refuse foreign scopes") {
            val container = containerOf(scopes)
            val session = container.openScope(SESSION)
            val page = container.openScope(PAGE)
            val foreign = containerOf(scopes).openScope(PAGE)
            session.link(page)

            session.unlink(page) shouldBe true
            session.unlink(page) shouldBe false
            session.getOrNull<Greeter>().shouldBeNull()
            shouldThrow<IllegalArgumentException> { session.link(session) }
            shouldThrow<IllegalArgumentException> { session.link(foreign) }
        }

        test("are bounded") {
            val container = containerOf(scopes)
            val session = container.openScope(SESSION)
            repeat(ContainerLimits.MAX_LINKS) { session.link(container.openScope(PAGE)) }

            shouldThrow<IllegalArgumentException> { session.link(container.openScope(PAGE)) }
        }

        test("closing the linking scope releases the link") {
            val container = containerOf(scopes)
            val session = container.openScope(SESSION)
            val page = container.openScope(PAGE)
            session.link(page)

            session.close()

            page.close().isClean shouldBe true
        }
    }

    context("declarations") {
        test("provide a declared value once after opening") {
            val container = containerOf(scopes)
            val session = container.openScope(SESSION)

            session.declare(Request("late"))

            session.openScope(REQUEST).get<String>() shouldBe "late"
            shouldThrow<ScopeSeedException> { session.declare(Request("again")) }
            shouldThrow<ScopeSeedException> { session.declare("undeclared") }
        }
    }

    context("life") {
        test("tells close handlers and cancels its coroutine scope") {
            val journal = Journal()
            val container = containerOf(scopes)
            val page = container.openScope(PAGE)
            page.onClosed { journal.write("closed") }
            page.onClosed { error("ignored") }

            page.coroutineScope.isActive shouldBe true
            page.close()
            page.onClosed { journal.write("late") }

            page.coroutineScope.isActive shouldBe false
            journal.entries shouldBe listOf("closed", "late")
            page shouldNotBe container.openScope(PAGE)
            page.equals("page") shouldBe false
        }
    }
})
