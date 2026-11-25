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

import com.davils.kontext.core.fixture.Color
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.GermanGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Primary
import com.davils.kontext.core.fixture.containerOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.reflect.typeOf

private const val PRIMARY_NAME: String = "db.primary"

class QualifierTest : FunSpec({
    test("named qualifiers compare by name") {
        Qualifier(PRIMARY_NAME) shouldBe Qualifier(PRIMARY_NAME)
        Qualifier(PRIMARY_NAME).hashCode() shouldBe Qualifier(PRIMARY_NAME).hashCode()
        Qualifier(PRIMARY_NAME) shouldNotBe Qualifier("db.replica")
        Qualifier(PRIMARY_NAME).equals(PRIMARY_NAME) shouldBe false
        Qualifier.of(Color.DEEP_RED) shouldBe Qualifier("deep-red")
    }

    test("typed qualifiers compare by type") {
        val primary = qualifier<Primary>()

        primary shouldBe Qualifier.of<Primary>()
        primary.hashCode() shouldBe Qualifier.of<Primary>().hashCode()
        primary shouldNotBe Qualifier.of<Color>()
        primary shouldNotBe Qualifier("primary")
        primary.toString() shouldBe "type:${typeOf<Primary>()}"
    }

    test("typed qualifiers tell definitions apart") {
        val container = containerOf(
            module("app") {
                single<Greeter>(qualifier<Primary>()) { EnglishGreeter() }
                single<Greeter>(Qualifier.of(Color.DEEP_RED)) { GermanGreeter() }
            },
        )

        container.get<Greeter>(qualifier<Primary>()).greet() shouldBe "hello"
        container.get<Greeter>(Qualifier("deep-red")).greet() shouldBe "hallo"
    }

    test("keys compare types structurally") {
        DefinitionKey.of<List<String>>() shouldBe DefinitionKey(typeOf<List<String>>())
        DefinitionKey.of<List<String>>() shouldNotBe DefinitionKey.of<List<Int>>()
        DefinitionKey.of<List<*>>() shouldBe DefinitionKey.of<List<*>>()
        DefinitionKey.of<List<*>>() shouldNotBe DefinitionKey.of<List<Any?>>()
        DefinitionKey.of<List<String>>() shouldNotBe DefinitionKey.of<List<*>>()
        DefinitionKey.of<Map<String, List<Int>>>() shouldBe DefinitionKey.of<Map<String, List<Int>>>()
        DefinitionKey.of<Array<out CharSequence>>() shouldNotBe DefinitionKey.of<Array<CharSequence>>()
        DefinitionKey.of<String?>() shouldNotBe DefinitionKey.of<String>()
        DefinitionKey.of<List<*>>().hashCode() shouldBe DefinitionKey.of<List<*>>().hashCode()
        DefinitionKey.of<String?>().hashCode() shouldNotBe DefinitionKey.of<String>().hashCode()
    }

    test("scope ids accept a restricted alphabet") {
        ScopeId("Session:42_a-b.c").toString() shouldBe "Session:42_a-b.c"
        listOf("", "-a", "a b", "a/b", "a".repeat(ContainerLimits.MAX_NAME_LENGTH + 1)).forEach { id ->
            shouldThrow<IllegalArgumentException> { ScopeId(id) }
        }
    }
})
