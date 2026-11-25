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

package com.davils.kontext.core.parameter

import com.davils.kontext.core.ContainerLimits
import com.davils.kontext.core.sameType
import com.davils.kontext.core.typeName
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

private const val UNNAMED: String = "value"

/**
 * The values a caller passes to a factory at the call, such as an id only known then.
 *
 * Parameters are immutable and only visible to the definition they are passed to, never to the definitions it
 * resolves. A value is found by its type: a value added with its type by [ParametersBuilder.add] matches that exact
 * type, a value of [parametersOf] matches every type it is an instance of whose type arguments are not named, such as
 * `String` or `List<*>`. Since type arguments are erased at runtime, a value of [parametersOf] never matches a type
 * with named type arguments such as `List<String>`: reading it as one throws a [ParameterException], and a value of
 * a generic type is passed with its type by `parametersOf { add(value) }` instead. Values never appear in a message
 * or a `toString`, since they may be secrets. Singles and scoped definitions refuse parameters, because a second
 * resolution could not honour them.
 *
 * @since 1.0.0
 */
public class Parameters internal constructor(values: List<Parameter>) {
    @PublishedApi
    internal val values: List<Parameter> = values.immutableList()

    init {
        require(values.size <= ContainerLimits.MAX_PARAMETERS) {
            "Parameters hold at most ${ContainerLimits.MAX_PARAMETERS} values, but got ${values.size}"
        }
    }

    /**
     * The number of values.
     *
     * @since 1.0.0
     */
    public val size: Int get() = values.size

    /**
     * Whether there is no value.
     *
     * @since 1.0.0
     */
    public val isEmpty: Boolean get() = values.isEmpty()

    /**
     * Returns the only value of type [T].
     *
     * @param T the type of the value.
     * @return the value.
     * @throws ParameterException if no value or more than one value has that type, or if [T] has type arguments and
     * only a value without a declared type would match it.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> get(): T = only(T::class, matching<T>()) as T

    /**
     * Returns the only value of type [T], or `null` if there is none.
     *
     * @param T the type of the value.
     * @return the value, `null` if none has that type.
     * @throws ParameterException if more than one value has that type, or if [T] has type arguments and only a value
     * without a declared type would match it.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> getOrNull(): T? = onlyOrNull(T::class, matching<T>()) as T?

    /**
     * Returns the value at [index], which must have type [T].
     *
     * @param T the type of the value.
     * @param index the position of the value.
     * @return the value.
     * @throws ParameterException if there is no value at [index] or it has another type, or if [T] has type arguments
     * and the value was passed without its type.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> get(index: Int): T {
        val parameter = at(index)
        val value = parameter.value as? T ?: throw ParameterException(
            "The value at $index is not of type ${nameOf(T::class)}",
        )
        ensureTyped(index = index, parameter = parameter, requested = typeOf<T>())
        return value
    }

    /**
     * Returns the first value, for destructuring such as `val (id: Int, name: String) = parameters`.
     *
     * @param T the type of the value.
     * @return the value.
     * @throws ParameterException if there is no such value or it has another type.
     * @since 1.0.0
     */
    public inline operator fun <reified T : Any> component1(): T = get(index = 0)

    /**
     * Returns the second value.
     *
     * @param T the type of the value.
     * @return the value.
     * @throws ParameterException if there is no such value or it has another type.
     * @since 1.0.0
     */
    public inline operator fun <reified T : Any> component2(): T = get(index = 1)

    /**
     * Returns the third value.
     *
     * @param T the type of the value.
     * @return the value.
     * @throws ParameterException if there is no such value or it has another type.
     * @since 1.0.0
     */
    public inline operator fun <reified T : Any> component3(): T = get(index = 2)

    /**
     * Returns the fourth value.
     *
     * @param T the type of the value.
     * @return the value.
     * @throws ParameterException if there is no such value or it has another type.
     * @since 1.0.0
     */
    public inline operator fun <reified T : Any> component4(): T = get(index = 3)

    /**
     * Returns the fifth value.
     *
     * @param T the type of the value.
     * @return the value.
     * @throws ParameterException if there is no such value or it has another type.
     * @since 1.0.0
     */
    public inline operator fun <reified T : Any> component5(): T = get(index = 4)

    @PublishedApi
    internal inline fun <reified T : Any> matching(): List<Parameter> {
        val requested = typeOf<T>()
        val found = values.filter { parameter -> parameter.matches(requested) { value -> value is T } }
        val isUntypedOnly = found.isEmpty() && isGeneric(requested)
        if (isUntypedOnly && values.any { parameter -> parameter.type == null && parameter.value is T }) {
            throw ParameterException(untyped(requested))
        }
        return found
    }

    @PublishedApi
    internal fun ensureTyped(index: Int, parameter: Parameter, requested: KType) {
        if (!isGeneric(requested)) return
        val declared = parameter.type ?: throw ParameterException(untyped(requested))
        if (sameType(first = declared, second = requested)) return
        throw ParameterException("The value at $index is not of type ${typeName(requested)}")
    }

    @PublishedApi
    internal fun untyped(requested: KType): String =
        "A value without a declared type never matches the generic type ${typeName(requested)}, because its type " +
            "arguments are erased; pass it with its type by parametersOf { add(value) }"

    @PublishedApi
    internal fun at(index: Int): Parameter =
        values.getOrNull(index) ?: throw ParameterException("There is no value at $index of $size")

    @PublishedApi
    internal fun only(type: KClass<*>, candidates: List<Parameter>): Any =
        onlyOrNull(type, candidates) ?: throw ParameterException("There is no value of type ${nameOf(type)}")

    @PublishedApi
    internal fun onlyOrNull(type: KClass<*>, candidates: List<Parameter>): Any? {
        if (candidates.size > 1) throw ParameterException("${candidates.size} values have type ${nameOf(type)}")
        return candidates.firstOrNull()?.value
    }

    @PublishedApi
    internal fun nameOf(type: KClass<*>): String = type.simpleName ?: UNNAMED

    override fun equals(other: Any?): Boolean = other is Parameters && values == other.values

    override fun hashCode(): Int = values.hashCode()

    override fun toString(): String = "Parameters($size values)"

    /**
     * The parameters without values.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * Parameters without a value.
         *
         * @since 1.0.0
         */
        public val EMPTY: Parameters = Parameters(emptyList())
    }
}
