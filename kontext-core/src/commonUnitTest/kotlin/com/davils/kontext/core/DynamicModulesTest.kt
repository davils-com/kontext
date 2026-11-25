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
import com.davils.kontext.core.error.ContainerViolationCodes
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.GermanGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.testing.RecordingContainerSink
import com.davils.kore.core.validation.report.ValidationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private val greeter: Module = module("greeter") { single<Greeter> { EnglishGreeter() } }

class DynamicModulesTest : FunSpec({
    test("loads modules into a running container and unloads them with their instances and scopes") {
        val journal = Journal()
        val sink = RecordingContainerSink()
        val container = containerOf { this.sink = sink }
        val loaded = module("loaded") {
            single(close = { resource: Resource -> resource.close() }) { Resource("r", journal) }
            scope(SESSION) {
                scoped(Qualifier("s"), close = { resource: Resource -> resource.close() }) { Resource("s", journal) }
            }
        }

        val handle = container.load(loaded, eager = true)
        val session = container.openScope(SESSION)
        session.get<Resource>(Qualifier("s"))

        journal.entries shouldBe listOf("opened r", "opened s")
        handle.modules shouldBe listOf(loaded)
        handle.toString() shouldBe "ModuleHandle([loaded])"
        handle.unload().isClean shouldBe true
        journal.entries shouldBe listOf("opened r", "opened s", "closed s", "closed r")
        session.isOpen shouldBe false
        container.getOrNull<Resource>().shouldBeNull()
        handle.unload() shouldBe CloseReport.AlreadyClosed
        val loadedEvent = sink.of<ContainerEvent.ModulesLoaded>().single()
        loadedEvent.toString() shouldBe "ModulesLoaded(root, [loaded])"
        sink.of<ContainerEvent.ModulesUnloaded>().single().modules shouldBe listOf(ModuleName("loaded"))
    }

    test("refuses a load that conflicts with the container and loads nothing") {
        val container = containerOf(greeter)

        val exception = shouldThrow<ValidationException> {
            container.load(module("other") { single<Greeter> { GermanGreeter() } })
        }

        exception.report.errors
            .single()
            .code shouldBe ContainerViolationCodes.DUPLICATE_DEFINITION
        container.get<Greeter>().greet() shouldBe "hello"
    }

    test("refuses a load into a parent that a child already declares") {
        val parent = containerOf()
        parent.child { modules(greeter) }

        val exception = shouldThrow<ValidationException> {
            parent.load(
                module("late") { single<Greeter> { GermanGreeter() } },
            )
        }

        exception.report.errors
            .single()
            .code shouldBe ContainerViolationCodes.SHADOWED_DEFINITION
    }

    test("unloads again when an eager single of the load fails") {
        val container = containerOf()

        shouldThrow<ResolutionException> {
            container.load(
                module("broken", eager = true) { single<Greeter> { error("x") } },
            )
        }

        container.getOrNull<Greeter>().shouldBeNull()
        container.load(greeter).modules shouldBe listOf(greeter)
    }

    test("a module loaded before is not loaded twice") {
        val container = containerOf(greeter)

        container.load(greeter).modules shouldBe emptyList()
    }

    test("declares single instances and removes them again") {
        val journal = Journal()
        val container = containerOf()

        val first = container.declare<Greeter>(EnglishGreeter())
        val second = container.declare(Resource("d", journal), qualifier = Qualifier("d"), close = { it.close() })

        container.get<Greeter>().greet() shouldBe "hello"
        container.get<Resource>(Qualifier("d")).name shouldBe "d"
        second.unload().isClean shouldBe true
        val declared = first.modules.single()
        declared.name.value shouldBe "kontext.declared.d1"
        journal.entries shouldBe listOf("opened d", "closed d")
    }

    test("a closed container loads nothing") {
        val container = containerOf()
        container.close()

        shouldThrow<ContainerClosedException> { container.load(greeter) }
    }

    test("modules combine with plus") {
        val combined = greeter + module("b") {} + module("c") {}

        combined.map { module -> module.name.value } shouldBe listOf("greeter", "b", "c")
        val container = containerOf { modules(combined) }
        container.get<Greeter>().shouldBeInstanceOf<EnglishGreeter>()
    }
})
