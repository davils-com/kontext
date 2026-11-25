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

import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.property.PropertiesBuilder
import com.davils.kontext.core.property.PropertyException
import com.davils.kontext.core.property.PropertyKey
import com.davils.kontext.core.property.PropertySource
import com.davils.kore.core.arguments.conversion.Converters
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

private const val DB_PORT: String = "db.port"
private const val DB_HOST: String = "db.host"
private const val SECRET: String = "s3cr3t-value"
private const val PORT: Int = 5432
private const val FALLBACK: Int = 80

class PropertiesTest : FunSpec({
    test("reads text and converted values, later sources winning") {
        val container = containerOf {
            properties {
                values(DB_HOST to "primary", DB_PORT to "1")
                source(PropertySource.of(mapOf(DB_PORT to "$PORT")))
            }
        }

        container.property(DB_HOST) shouldBe "primary"
        container.property(DB_PORT, Converters.int) shouldBe PORT
        container.property("db.missing", Converters.int, FALLBACK) shouldBe FALLBACK
        container.propertyOrNull("db.missing", Converters.int).shouldBeNull()
        PropertySource.EMPTY.valueOf(PropertyKey(DB_HOST)).shouldBeNull()
    }

    test("a child layers its sources over those of its parent and factories read them") {
        val parent = containerOf { properties { values("a" to "parent", "b" to "parent") } }
        val child = parent.child {
            properties { values("b" to "child") }
            modules(module("reader") { single { property("a") + property("b") } })
        }

        child.get<String>() shouldBe "parentchild"
    }

    test("refuses missing, unconvertible and failing values without showing them") {
        val container = containerOf {
            properties {
                values(DB_PORT to SECRET)
                source { key -> error(SECRET + key) }
            }
        }

        val missing = shouldThrow<PropertyException> { containerOf().property(DB_HOST) }
        val unconvertible = shouldThrow<PropertyException> {
            containerOf {
                properties {
                    values(
                        DB_PORT to SECRET,
                    )
                }
            }.property(DB_PORT, Converters.int)
        }
        val failing = shouldThrow<PropertyException> { container.property(DB_PORT) }

        missing.message shouldBe "The property $DB_HOST is not set"
        missing.key shouldBe PropertyKey(DB_HOST)
        unconvertible.message shouldBe "The property $DB_PORT is not a valid int"
        unconvertible.cause.shouldBeNull()
        failing.message shouldBe "The source of the property $DB_PORT failed"
        listOf(missing, unconvertible, failing).forEach { exception ->
            exception.message.orEmpty() shouldNotContain SECRET
        }
    }

    test("keys are validated names") {
        shouldThrow<IllegalArgumentException> { PropertyKey("DB_PORT") }
        shouldThrow<IllegalArgumentException> { PropertySource.of(mapOf("Bad Key" to "x")) }
        PropertyKey(DB_PORT).toString() shouldBe DB_PORT
        PropertiesBuilder().toString() shouldBe "PropertiesBuilder(0 sources)"
    }
})
