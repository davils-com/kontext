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

import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.observe.ContainerEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first

private val scoped: Module = module("scoped") { scope(SESSION) { scoped { 1 } } }

class UsesTest : FunSpec({
    test("use closes a container and a scope also when the block fails") {
        val container = containerOf(scoped)
        val scope = container.openScope(SESSION)

        scope.use { opened -> opened.get<Int>() } shouldBe 1
        shouldThrow<IllegalStateException> { container.use { error("x") } }

        scope.isOpen shouldBe false
        container.state.value shouldBe ContainerState.Closed
    }

    test("withScope opens and closes a scope around a block") {
        val container = containerOf(scoped)

        container.withScope(SESSION, ScopeId("s")) { scope -> scope.get<Int>() } shouldBe 1

        container.scopeOrNull(ScopeId("s")) shouldBe null
        container.use { opened -> opened.name } shouldBe ContainerName("root")
    }

    test("events flow to a collector") {
        val container = containerOf(scoped)
        val opened = async(start = CoroutineStart.UNDISPATCHED) { container.events.first() }

        container.openScope(SESSION)

        opened.await().shouldBeInstanceOf<ContainerEvent.ScopeOpened>()
        container.close()
    }

    test("setups configure several containers and modules may be eager") {
        val counter = mutableListOf<String>()
        val setup = containerSetup { modules(module("eager", eager = true) { single { "x".also(counter::add) } }) }

        containerOf { include(setup) }
        containerOf { modules(listOf(scoped)) }

        counter shouldBe listOf("x")
        setup.toString() shouldBe "ContainerSetup"
    }
})
