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

import kotlin.reflect.KClass
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
         * Creates the qualifier named after [constant] and its enum class [E]: the name of the class, with its package
         * where the platform knows it, followed by the name of the constant, in lower case with the words of the
         * class joined by `-` and `_` as `-`, such as `com.example.db-role.read-only` for `DbRole.READ_ONLY` in the
         * package `com.example`. Constants of the same name in different enum classes give different qualifiers. On
         * a platform that does not know the package of a class, such as `wasmJs` in some configurations, only the
         * simple name of the class is used, so that two enum classes of the same simple name are only told apart
         * where the package is known.
         *
         * @param E the enum class.
         * @param constant the enum constant.
         * @return the qualifier.
         * @throws IllegalArgumentException if the names do not make a valid name, such as one longer than
         * [ContainerLimits.MAX_NAME_LENGTH] characters or one with characters other than ASCII letters, digits and
         * `_`.
         * @since 1.0.0
         */
        public inline fun <reified E : Enum<E>> of(constant: E): Qualifier =
            enumNamed(owner = E::class, constant = constant.name)

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

        @PublishedApi
        internal fun enumNamed(owner: KClass<*>, constant: String): Qualifier {
            val className = owner.qualifiedName ?: owner.simpleName.orEmpty()
            val segments = className.split('.').map(::wordsOf)
            val name = (segments + constant.lowercase().replace(oldChar = '_', newChar = '-')).joinToString(".")
            return Named(name)
        }
    }
}

private fun wordsOf(segment: String): String {
    val words = StringBuilder()
    segment.forEachIndexed { index, char ->
        val previous = segment.getOrNull(index - 1)
        val isWordStart = char.isUpperCase() && previous != null && (previous.isLowerCase() || previous.isDigit())
        if (isWordStart) words.append('-')
        words.append(char.lowercaseChar())
    }
    return words.toString().replace(oldChar = '_', newChar = '-')
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
