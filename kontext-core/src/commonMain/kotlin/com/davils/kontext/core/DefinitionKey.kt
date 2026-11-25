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

import kotlin.reflect.KType
import kotlin.reflect.typeOf

private const val HASH_FACTOR: Int = 31

/**
 * What a definition is found by: its full type, generic arguments and nullability included, and an optional
 * qualifier.
 *
 * `List<String>` and `List<Int>` are different keys, and so are a type with and without a qualifier. Two keys are
 * equal when their types have the same classifier, arguments and nullability, wherever the types were created.
 *
 * @property type the full type of the definition.
 * @property qualifier the qualifier of the definition, `null` for none.
 * @since 1.0.0
 */
public class DefinitionKey(public val type: KType, public val qualifier: Qualifier? = null) {
    override fun equals(other: Any?): Boolean =
        other is DefinitionKey && sameType(first = type, second = other.type) && qualifier == other.qualifier

    override fun hashCode(): Int = HASH_FACTOR * typeHash(type) + qualifier.hashCode()

    override fun toString(): String {
        val named = qualifier ?: return typeName(type)
        return "${typeName(type)}@$named"
    }

    /**
     * Creates keys.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * Creates the key of [T] with [qualifier].
         *
         * @param T the type of the definition.
         * @param qualifier the qualifier, `null` for none.
         * @return the key.
         * @since 1.0.0
         */
        public inline fun <reified T> of(qualifier: Qualifier? = null): DefinitionKey =
            DefinitionKey(typeOf<T>(), qualifier)
    }
}
