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

private const val HASH_SEED: Int = 17

/**
 * Tells apart two definitions of the same type, such as two databases: by a name or by a marker type.
 *
 * @since 1.0.0
 */
public sealed interface Qualifier {
    /**
     * A qualifier by name, a dotted name in lower case such as `db.primary`.
     *
     * @property name the name.
     * @since 1.0.0
     */
    public class Named internal constructor(public val name: String) : Qualifier {
        init {
            requireName(value = name, kind = "qualifier")
        }

        override fun equals(other: Any?): Boolean = other is Named && name == other.name

        override fun hashCode(): Int = name.hashCode()

        override fun toString(): String = name
    }

    /**
     * A qualifier by a marker type, such as an annotation-free `object Primary`.
     *
     * @property type the marker type.
     * @since 1.0.0
     */
    public class Typed internal constructor(public val type: KType) : Qualifier {
        override fun equals(other: Any?): Boolean = other is Typed && sameType(first = type, second = other.type)

        override fun hashCode(): Int = HASH_SEED + typeHash(type)

        override fun toString(): String = "type:${typeName(type)}"
    }

    /**
     * Creates qualifiers.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * Creates the qualifier named after [constant]: its name in lower case with `_` as `-`, such as `db-primary`
         * for `DB_PRIMARY`.
         *
         * @param constant the enum constant.
         * @return the qualifier.
         * @throws IllegalArgumentException if the name of the constant does not make a valid name.
         * @since 1.0.0
         */
        public fun of(constant: Enum<*>): Qualifier = Named(
            constant.name.lowercase().replace(oldChar = '_', newChar = '-'),
        )

        /**
         * Creates the qualifier of the marker type [T].
         *
         * @param T the marker type.
         * @return the qualifier.
         * @since 1.0.0
         */
        public inline fun <reified T> of(): Qualifier = typed(typeOf<T>())

        @PublishedApi
        internal fun typed(type: KType): Qualifier = Typed(type)
    }
}

/**
 * Creates the qualifier named [name].
 *
 * @param name a dotted name in lower case of at most [ContainerLimits.MAX_NAME_LENGTH] characters.
 * @return the qualifier.
 * @throws IllegalArgumentException if [name] is not such a name.
 * @since 1.0.0
 */
@Suppress("FunctionNaming")
public fun Qualifier(name: String): Qualifier = Qualifier.Named(name)

/**
 * Creates the qualifier of the marker type [T], like [Qualifier.of].
 *
 * @param T the marker type.
 * @return the qualifier.
 * @since 1.0.0
 */
public inline fun <reified T> qualifier(): Qualifier = Qualifier.of<T>()
