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

package com.davils.kontext.core.property

/**
 * Where a container reads its properties, implemented by the application over its configuration: environment
 * variables, a configuration service, a file the application read itself.
 *
 * Kontext never reads a file or the environment on its own. [valueOf] is called on the thread that resolves and may
 * be called often, so an implementation answers from memory. A source that throws is reported as a failing property
 * read; its value is never shown.
 *
 * @since 1.0.0
 */
public fun interface PropertySource {
    /**
     * Returns the raw value of [key], `null` if this source does not know it.
     *
     * @param key the key.
     * @return the raw value or `null`.
     * @since 1.0.0
     */
    public fun valueOf(key: PropertyKey): String?

    /**
     * Sources without a value.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * A source that knows no key.
         *
         * @since 1.0.0
         */
        public val EMPTY: PropertySource = PropertySource { null }

        /**
         * Creates a source over [values], keyed by property keys such as `db.port`.
         *
         * @param values the raw values by key.
         * @return the source, holding a copy of [values].
         * @throws IllegalArgumentException if a key is not a valid [PropertyKey].
         * @since 1.0.0
         */
        public fun of(values: Map<String, String>): PropertySource {
            val copy = values.mapKeys { entry -> PropertyKey(entry.key) }
            return PropertySource { key -> copy[key] }
        }
    }
}
