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

import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.error.ContainerClosedException
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.LIVE
import com.davils.kontext.core.fixture.MOCK
import com.davils.kontext.core.fixture.Resource
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.testing.RecordingContainerSink
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs

private val OWN: Qualifier = Qualifier("own")

class ChildContainerTest : FunSpec({
    test("a child resolves from its parent and the parent never from the child") {
        val parent = containerOf(module("parent") { single<Greeter> { EnglishGreeter() } })
        val services = module("services") { single { Service(get()) } }
        val child = parent.child { modules(services) }

        child.get<Service>().greeter shouldBeSameInstanceAs parent.get<Greeter>()
        parent.getOrNull<Service>() shouldBe null
        child.getAll<Greeter>().single() shouldBeSameInstanceAs parent.get<Greeter>()
        child.name shouldBe ContainerBuilder.CHILD
        child.profile shouldBe parent.profile
    }

    test("a child may run in its own profile and inherits the settings of its parent") {
        val sink = RecordingContainerSink()
        val parent = containerOf(profile = LIVE) { this.sink = sink }
        val child = parent.child(MOCK) { name = ContainerName("plugin") }

        sink.events.map { event -> event.container.value } shouldBe listOf("root", "plugin", "root")
        child.get<Profile>() shouldBe MOCK
        parent.get<Profile>() shouldBe LIVE
    }

    test("a child of the same profile resolves the profile from its parent") {
        val parent = containerOf(profile = LIVE)

        parent.child { }.get<Profile>() shouldBe LIVE
    }

    test("closing the parent closes its children first") {
        val journal = Journal()
        val parent = containerOf(
            module("parent") { single(close = { resource: Resource -> resource.close() }) { Resource("p", journal) } },
        )
        val child = parent.child {
            modules(
                module("child") {
                    single(
                        OWN,
                        close = { resource: Resource -> resource.close() },
                    ) { Resource("c", journal) }
                },
            )
        }
        parent.get<Resource>()
        child.get<Resource>(OWN)

        parent.close()

        journal.entries shouldBe listOf("opened p", "opened c", "closed c", "closed p")
    }

    test("a child closed on its own leaves its parent running") {
        val parent = containerOf()
        val child = parent.child { }

        child.close().isClean shouldBe true

        parent.close().isClean shouldBe true
    }

    test("a closed container opens no child") {
        val parent = containerOf()
        parent.close()

        shouldThrow<ContainerClosedException> { parent.child { } }
    }
})
