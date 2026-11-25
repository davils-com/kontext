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

package com.davils.kontext.testing

import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.Profile
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.module
import com.davils.kontext.core.observe.ContainerEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val SESSION: ScopeKey = ScopeKey("session")

private val app = module("app") {
    single { "real" }
    scope(SESSION) {}
}

class TestContainersTest : FunSpec({
    test("a test container runs in the mock profile with replaced definitions and counted scope ids") {
        val container = testContainer {
            modules(app)
            replace(module("fake") { single { "fake" } })
            configure { name = ContainerName("test") }
        }

        container.profile shouldBe TestProfiles.MOCK
        container.name shouldBe ContainerName("test")
        container.get<String>() shouldBe "fake"
        container.openScope(SESSION).id.toString() shouldBe "00000000-0000-7000-8000-000000000000"
        container.close()
    }

    test("a test container takes another profile") {
        val container = testContainer(TestProfiles.LIVE) { }

        container.get<Profile>() shouldBe TestProfiles.LIVE
        TestProfiles.LIVE.value shouldBe "live"
    }

    test("describes its builder") {
        testContainer {
            modules(app)
            toString() shouldBe "TestContainerBuilder(modules=1, replaced=0)"
        }
    }

    test("a recording sink keeps, filters and forgets events") {
        val sink = RecordingContainerSink()
        val container = testContainer { configure { this.sink = sink } }

        container.close()

        sink.of<ContainerEvent.Started>().size shouldBe 1
        sink.events.size shouldBe 2
        sink.toString() shouldBe "RecordingContainerSink(2 events)"
        sink.clear()
        sink.events shouldBe emptyList()
    }

    test("a failing sink refuses every event") {
        val sink = FailingContainerSink()
        val recording = RecordingContainerSink()
        val container = testContainer { configure { this.sink = sink } }
        testContainer { configure { this.sink = recording } }

        container.rejectedEvents shouldBe 1L
        sink.toString() shouldBe "FailingContainerSink"
        shouldThrow<IllegalStateException> { sink.record(recording.events.single()) }
    }
})
