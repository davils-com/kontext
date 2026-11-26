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

package com.davils.kontext.kaml

import com.davils.kontext.core.container
import com.davils.kontext.core.property.PropertyKey
import com.davils.kore.core.arguments.conversion.Converters
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

private const val SECRET: String = "s3cr3t-value"
private const val PROPERTIES: Int = 3
private const val PORT: Int = 5432

private val application: String = listOf(
    "db:",
    "  host: localhost",
    "  port: 5432",
    "  password: $SECRET",
    "cache:",
    "  ttl: ~",
).joinToString(separator = "\n")

class YamlPropertySourceTest : FunSpec({
    context("properties from YAML") {
        test("nest the keys of mappings with dots") {
            val source = YamlPropertySource.parse(application)

            source.valueOf(PropertyKey("db.host")) shouldBe "localhost"
            source.valueOf(PropertyKey("db.port")) shouldBe "5432"
            source.size shouldBe PROPERTIES
        }

        test("nest mappings at every depth") {
            val source = YamlPropertySource.parse("app:\n  http:\n    port: 8080")

            source.valueOf(PropertyKey("app.http.port")) shouldBe "8080"
        }

        test("reach a container as a source of its properties") {
            val started = container { properties { yaml(application) } }

            started.property("db.port", Converters.int) shouldBe PORT
            started.close()
        }

        test("leave a null out") {
            YamlPropertySource.parse(application).valueOf(PropertyKey("cache.ttl")) shouldBe null
        }

        test("show neither keys nor values") {
            val shown = YamlPropertySource.parse(application).toString()

            shown shouldBe "YamlPropertySource(3 properties)"
            shown shouldNotContain SECRET
        }

        test("refuse a sequence, a key that is no property key and a single value") {
            listOf(
                "- a\n- b",
                "hosts:\n  - a\n  - b",
                "Db:\n  port: 1",
                "just a value",
                "? [a]\n: b",
                "~: b",
            ).forEach { text ->
                shouldThrow<IllegalArgumentException> { YamlPropertySource.parse(text) }
            }
        }

        test("refuse text kaml cannot read and more than one document, without showing a value") {
            listOf("a: [", "a: 1\n---\nb: 2", "").forEach { text ->
                val failure = shouldThrow<IllegalArgumentException> { YamlPropertySource.parse(text) }

                failure.message.orEmpty() shouldNotContain SECRET
            }
        }
    }
})
