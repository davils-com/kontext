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

package com.davils.kontext.krypt

import com.davils.kontext.core.container
import com.davils.kontext.core.property.PropertyException
import com.davils.kore.core.Limits
import com.davils.krypt.core.SecretText
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.codepoints
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

private const val PASSWORD_KEY: String = "db.password"
private const val MISSING_KEY: String = "db.missing"
private const val PASSWORD: String = "c0rrect-h0rse-battery-st4ple"
private const val MIN_LENGTH: Int = 16
private const val MAX_LENGTH: Int = 512

private fun SecretText.text(): String = reveal { chars -> chars.concatToString() }

class SecretPropertiesTest : FunSpec({
    context("secret") {
        test("reads a property as a secret that shows nothing of itself") {
            val container = container { properties { values(PASSWORD_KEY to PASSWORD) } }

            val secret = container.secret(PASSWORD_KEY)

            secret.text() shouldBe PASSWORD
            secret.toString() shouldNotContain PASSWORD
            secret.destroy()
            secret.isDestroyed shouldBe true
            container.close()
        }

        test("refuses a missing key") {
            val container = container { }

            val failure = shouldThrow<PropertyException> { container.secret(MISSING_KEY) }

            failure.message shouldBe "The property $MISSING_KEY is not set"
            container.close()
        }

        test("refuses an empty text and one beyond the limit of Kore without showing it") {
            val tooLong = PASSWORD.repeat(Limits.MAX_ARGUMENT_LENGTH / PASSWORD.length + 1)
            val container = container { properties { values(PASSWORD_KEY to tooLong, MISSING_KEY to "") } }

            val refused = shouldThrow<PropertyException> { container.secret(PASSWORD_KEY) }
            val empty = shouldThrow<PropertyException> { container.secretOrNull(MISSING_KEY) }

            refused.message.orEmpty() shouldNotContain PASSWORD
            refused.cause.shouldBeNull()
            empty.message shouldBe "The property $MISSING_KEY is not a valid secret"
            container.close()
        }

        test("never shows any value it reads, in the secret or in a refusal") {
            checkAll(Arb.string(minSize = MIN_LENGTH, maxSize = MAX_LENGTH, codepoints = Arb.codepoints())) { value ->
                val container = container { properties { values(PASSWORD_KEY to value) } }

                val secret = container.secret(PASSWORD_KEY)

                secret.text() shouldBe value
                secret.toString() shouldNotContain value
                container.close()
            }
        }
    }

    context("secretOrNull") {
        test("reads a known key and gives null for an unknown one") {
            val container = container { properties { values(PASSWORD_KEY to PASSWORD) } }

            container.secretOrNull(MISSING_KEY).shouldBeNull()
            val secret = container.secretOrNull(PASSWORD_KEY).shouldNotBeNull()
            secret.use { known -> known.text() shouldBe PASSWORD }
            container.close()
        }
    }
})
