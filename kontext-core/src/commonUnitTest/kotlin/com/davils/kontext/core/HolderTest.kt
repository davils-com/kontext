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

@file:OptIn(GlobalContainerApi::class)

package com.davils.kontext.core

import com.davils.kontext.core.component.get
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.error.ContainerAlreadyStartedException
import com.davils.kontext.core.error.ContainerNotStartedException
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.HandlerComponent
import com.davils.kontext.core.fixture.LegacyComponent
import com.davils.kontext.core.fixture.PageComponent
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.holder.ContainerHolder
import com.davils.kontext.core.holder.GlobalContainer
import com.davils.kontext.core.holder.GlobalContainerApi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

private val app: Module = module("app") {
    single<Greeter> { EnglishGreeter() }
    scope(REQUEST) { scoped { Request("scoped") } }
}

class HolderTest : FunSpec({
    test("a holder starts, holds, loads into and stops one container") {
        val holder = ContainerHolder()

        holder.currentOrNull.shouldBeNull()
        shouldThrow<ContainerNotStartedException> { holder.current }
        val started = holder.start { modules(app) }

        holder.current shouldBeSameInstanceAs started
        shouldThrow<ContainerAlreadyStartedException> { holder.start {} }.container shouldBe ContainerName("root")
        holder.load(module("late") { single { 1 } }).modules.size shouldBe 1
        holder.current.get<Int>() shouldBe 1
        holder.toString() shouldBe "ContainerHolder(root)"
        holder.stop().isClean shouldBe true
        holder.stop() shouldBe CloseReport.AlreadyClosed
        holder.currentOrNull.shouldBeNull()
    }

    test("components inject from their container, scope or the global container") {
        val holder = ContainerHolder()
        val container = holder.start { modules(app) }
        val page = PageComponent(container)
        val handler = HandlerComponent(container.openScope(REQUEST))

        page.greeter.greet() shouldBe "hello"
        page.missing.shouldBeNull()
        page.get<Greeter>() shouldBeSameInstanceAs page.greeter
        handler.request.id shouldBe "scoped"
        handler.resolver shouldBeSameInstanceAs handler.scope

        GlobalContainer.start { modules(app) }
        LegacyComponent().greeter.greet() shouldBe "hello"
        GlobalContainer.toString() shouldBe "GlobalContainer(root)"
        GlobalContainer.stop()
        GlobalContainer.toString() shouldBe "GlobalContainer(none)"
        holder.stop()
    }
})
