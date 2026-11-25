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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.environment

import com.davils.kontext.core.ContainerLimits
import com.davils.kontext.core.property.PropertyKey
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.text.SafeText

private val prefixPattern: Regex = Regex("[A-Z][A-Z0-9_]*_")

/**
 * Maps a property key to the name of an environment variable.
 *
 * @since 1.0.0
 */
public fun interface PropertyNaming {
    /**
     * Returns the variable name of [key].
     *
     * @param key the property key.
     * @return the variable name.
     * @since 1.0.0
     */
    public fun nameOf(key: PropertyKey): String

    /**
     * The usual namings.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * Upper case with `.` and `-` as `_`: `db.port` is read from `DB_PORT`.
         *
         * The mapping is not injective: `app.db-password` and `app.db.password` both read `APP_DB_PASSWORD`. Without a
         * prefix, every environment variable of the process can be reached by a property key, including credentials
         * that are meant for other programs, so prefer [prefixed] with a prefix of the application.
         *
         * @since 1.0.0
         */
        public val UPPER_SNAKE: PropertyNaming = PropertyNaming { key -> snake(key.value) }

        /**
         * Like [UPPER_SNAKE] with [prefix] in front: with the prefix `APP_`, `db.port` is read from `APP_DB_PORT`, so
         * that only the variables of the application are reachable. As with [UPPER_SNAKE], `db-password` and
         * `db.password` read the same variable.
         *
         * @param prefix the prefix of every variable: an upper case ASCII letter followed by upper case ASCII letters,
         * digits and `_`, ending with `_`, such as `APP_`, of at most [ContainerLimits.MAX_NAME_LENGTH] characters.
         * @return the naming.
         * @throws IllegalArgumentException if [prefix] is not such a prefix, such as `APP` without the trailing `_`.
         * @since 1.0.0
         */
        public fun prefixed(prefix: String): PropertyNaming {
            val isValid = prefix.length <= ContainerLimits.MAX_NAME_LENGTH && prefixPattern.matches(prefix)
            require(isValid) {
                "A prefix is upper case ASCII ending with '_' of at most ${ContainerLimits.MAX_NAME_LENGTH} " +
                    "characters such as APP_, but was ${SafeText.quoted(prefix)}"
            }
            return PropertyNaming { key -> prefix + snake(key.value) }
        }

        private fun snake(value: String): String {
            val upper = value.uppercase()
            return upper.replace(oldChar = '.', newChar = '_').replace(oldChar = '-', newChar = '_')
        }
    }
}
