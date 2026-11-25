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

import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Hostile
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.observe.TextLevel
import com.davils.kontext.core.observe.asContainerSink
import com.davils.kontext.testing.RecordingTextSink
import com.davils.kore.testing.isSafeToShow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith

private const val WARNINGS: Int = 3

class TextSinkTest : FunSpec({
    test("writes every event as a safe line of its level") {
        val lines = RecordingTextSink()
        val container = containerOf(
            module("app") {
                single<Greeter>(close = { error("x") }) { EnglishGreeter() }
                scope(SESSION) {}
            },
        ) {
            sink = lines.asContainerSink(TextLevel.Debug)
            extensions(Hostile())
        }
        container.get<Greeter>()
        container.openScope(SESSION).close()
        shouldThrow<ResolutionException> { container.get<Int>() }
        container.load(module("late") {}).unload()
        container.child { }
        container.close()

        lines.at(TextLevel.Info).map { line -> line.substringBefore(" in ") } shouldContainExactly listOf(
            "container root started",
            "container root loaded [late]",
            "container root unloaded [late]",
            "container child started",
            "container root opened the child child",
            "container child closed with 0 failures",
            "container root closed with 1 failures",
        )
        lines.at(TextLevel.Warning).size shouldBe WARNINGS
        lines.at(TextLevel.Warning) shouldContain "container root failed to resolve kotlin.Int (missing)"
        lines.at(TextLevel.Debug).first() shouldStartWith "container root created"
        lines.lines.all { line -> isSafeToShow(line) } shouldBe true
        lines.toString() shouldBe "RecordingTextSink(${lines.lines.size} lines)"
    }

    test("leaves out events below its minimum") {
        val lines = RecordingTextSink()
        val container = containerOf(module("app") { single { 1 } }) { sink = lines.asContainerSink() }

        container.get<Int>()

        lines.at(TextLevel.Debug) shouldBe emptyList()
    }
})
