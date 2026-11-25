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

import com.davils.kontext.core.property.PropertyKey

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
         * @since 1.0.0
         */
        public val UPPER_SNAKE: PropertyNaming = PropertyNaming { key -> snake(key.value) }

        /**
         * Like [UPPER_SNAKE] with [prefix] in front: with the prefix `APP_`, `db.port` is read from `APP_DB_PORT`.
         *
         * @param prefix the prefix of every variable.
         * @return the naming.
         * @since 1.0.0
         */
        public fun prefixed(prefix: String): PropertyNaming = PropertyNaming { key -> prefix + snake(key.value) }

        private fun snake(value: String): String {
            val upper = value.uppercase()
            return upper.replace(oldChar = '.', newChar = '_').replace(oldChar = '-', newChar = '_')
        }
    }
}
