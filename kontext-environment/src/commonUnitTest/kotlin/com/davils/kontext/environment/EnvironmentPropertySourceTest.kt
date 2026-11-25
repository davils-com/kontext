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

package com.davils.kontext.environment

import com.davils.knot.environment.environment
import com.davils.kontext.core.container
import com.davils.kontext.core.property.PropertyKey
import com.davils.kore.core.arguments.conversion.Converters
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private const val PORT: Int = 5432

class EnvironmentPropertySourceTest : FunSpec({
    test("reads properties from environment variables by their naming") {
        val variables = environment {
            variable(name = "DB_PORT", value = "$PORT")
            variable(name = "APP_DB_HOST", value = "primary")
            variable(name = "FEATURE_DARK_MODE", value = "true")
        }
        val started = container {
            properties {
                environment(variables)
                environment(variables, PropertyNaming.prefixed("APP_"))
            }
        }

        started.property("db.port", Converters.int) shouldBe PORT
        started.property("db.host") shouldBe "primary"
        started.property("feature.dark-mode", Converters.boolean) shouldBe true
        started.close()
    }

    test("names keys and describes itself without values") {
        val source = EnvironmentPropertySource(environment { variable(name = "A_B", value = "secret") })

        PropertyNaming.UPPER_SNAKE.nameOf(PropertyKey("a.b-c")) shouldBe "A_B_C"
        source.valueOf(PropertyKey("a.b")) shouldBe "secret"
        source.valueOf(PropertyKey("missing")).shouldBeNull()
        source.naming shouldBe PropertyNaming.UPPER_SNAKE
        source.toString() shouldBe "EnvironmentPropertySource(1 variables)"
    }
})
