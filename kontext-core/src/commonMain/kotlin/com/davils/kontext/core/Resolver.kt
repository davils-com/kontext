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

import com.davils.kontext.core.internal.Properties
import com.davils.kontext.core.parameter.Parameters
import com.davils.kontext.core.property.PropertyKey
import com.davils.kore.core.arguments.conversion.Converter
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Resolves instances by their type and qualifier: a container, a scope, and the receiver of every factory.
 *
 * A resolver is thread-safe. A resolution fails with [com.davils.kontext.core.error.ResolutionException], whose
 * [com.davils.kontext.core.error.ResolutionFailure] tells why and along which path of definitions. Instances of
 * suspending definitions are resolved with [await]; [get] returns them only once they exist.
 *
 * @since 1.0.0
 */
public abstract class Resolver internal constructor() {
    /**
     * Resolves the instance of [T] with [qualifier].
     *
     * @param T the type of the definition.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance.
     * @throws com.davils.kontext.core.error.ResolutionException if nothing is declared under the key, the key is
     *   ambiguous, the definitions form a cycle, a factory fails, parameters are refused or the container is closed.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> get(
        qualifier: Qualifier? = null,
        parameters: Parameters = Parameters.EMPTY,
    ): T = resolve(DefinitionKey(typeOf<T>(), qualifier), parameters) as T

    /**
     * Resolves the instance of [T] with [qualifier], or `null` if nothing is declared under that key.
     *
     * Only a missing definition gives `null`; every other failure is thrown.
     *
     * @param T the type of the definition.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance, `null` if nothing is declared.
     * @throws com.davils.kontext.core.error.ResolutionException if a definition is declared but fails to resolve.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> getOrNull(
        qualifier: Qualifier? = null,
        parameters: Parameters = Parameters.EMPTY,
    ): T? = resolveOrNull(DefinitionKey(typeOf<T>(), qualifier), parameters) as T?

    /**
     * Resolves every instance declared as or bound to [T], with any qualifier: those of the scope, its enclosing and
     * linked scopes, the container and its ancestors, in that order.
     *
     * @param T the type of the definitions.
     * @return the instances, empty if none is declared.
     * @throws com.davils.kontext.core.error.ResolutionException if one of them fails to resolve.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> getAll(): List<T> = resolveAll(typeOf<T>()).map { instance -> instance as T }

    /**
     * Resolves the instance of [T] with [qualifier] on first use, which breaks a cycle between two definitions.
     *
     * @param T the type of the definition.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the lazily resolved instance, thread-safe.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> getLazy(
        qualifier: Qualifier? = null,
        parameters: Parameters = Parameters.EMPTY,
    ): Lazy<T> {
        val resolver = detached()
        return lazy { resolver.get<T>(qualifier, parameters) }
    }

    /**
     * Resolves the instance of [T] with [qualifier], suspending while a suspending definition creates it or lazy
     * modules load.
     *
     * @param T the type of the definition.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance.
     * @throws com.davils.kontext.core.error.ResolutionException for the same reasons as [get].
     * @since 1.0.0
     */
    public suspend inline fun <reified T : Any> await(
        qualifier: Qualifier? = null,
        parameters: Parameters = Parameters.EMPTY,
    ): T = resolveAsync(DefinitionKey(typeOf<T>(), qualifier), parameters) as T

    /**
     * Resolves the instance of [T] with [qualifier] like [await], or `null` if nothing is declared under that key.
     *
     * @param T the type of the definition.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance, `null` if nothing is declared.
     * @throws com.davils.kontext.core.error.ResolutionException if a definition is declared but fails to resolve.
     * @since 1.0.0
     */
    public suspend inline fun <reified T : Any> awaitOrNull(
        qualifier: Qualifier? = null,
        parameters: Parameters = Parameters.EMPTY,
    ): T? = resolveAsyncOrNull(DefinitionKey(typeOf<T>(), qualifier), parameters) as T?

    /**
     * Resolves every instance of [T] like [getAll], suspending for suspending definitions.
     *
     * @param T the type of the definitions.
     * @return the instances, empty if none is declared.
     * @throws com.davils.kontext.core.error.ResolutionException if one of them fails to resolve.
     * @since 1.0.0
     */
    public suspend inline fun <reified T : Any> awaitAll(): List<T> =
        resolveAllAsync(typeOf<T>()).map { instance -> instance as T }

    /**
     * Resolves the instance of [key], for callers that hold a key instead of a type, such as a framework bridge.
     *
     * @param key the key.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance.
     * @throws com.davils.kontext.core.error.ResolutionException for the same reasons as [get].
     * @since 1.0.0
     */
    public abstract fun resolve(key: DefinitionKey, parameters: Parameters = Parameters.EMPTY): Any

    /**
     * Resolves the instance of [key], or `null` if nothing is declared under it.
     *
     * @param key the key.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance, `null` if nothing is declared.
     * @throws com.davils.kontext.core.error.ResolutionException if a definition is declared but fails to resolve.
     * @since 1.0.0
     */
    public abstract fun resolveOrNull(key: DefinitionKey, parameters: Parameters = Parameters.EMPTY): Any?

    /**
     * Resolves every instance declared as or bound to [type].
     *
     * @param type the type.
     * @return the instances.
     * @throws com.davils.kontext.core.error.ResolutionException if one of them fails to resolve.
     * @since 1.0.0
     */
    public abstract fun resolveAll(type: KType): List<Any>

    /**
     * Resolves the instance of [key] like [await].
     *
     * @param key the key.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance.
     * @throws com.davils.kontext.core.error.ResolutionException for the same reasons as [get].
     * @since 1.0.0
     */
    public abstract suspend fun resolveAsync(key: DefinitionKey, parameters: Parameters = Parameters.EMPTY): Any

    /**
     * Resolves the instance of [key] like [awaitOrNull].
     *
     * @param key the key.
     * @param parameters the parameters for a factory, [Parameters.EMPTY] for none.
     * @return the instance, `null` if nothing is declared.
     * @throws com.davils.kontext.core.error.ResolutionException if a definition is declared but fails to resolve.
     * @since 1.0.0
     */
    public abstract suspend fun resolveAsyncOrNull(key: DefinitionKey, parameters: Parameters = Parameters.EMPTY): Any?

    /**
     * Resolves every instance declared as or bound to [type] like [awaitAll].
     *
     * @param type the type.
     * @return the instances.
     * @throws com.davils.kontext.core.error.ResolutionException if one of them fails to resolve.
     * @since 1.0.0
     */
    public abstract suspend fun resolveAllAsync(type: KType): List<Any>

    /**
     * Reads the property [key] as text.
     *
     * @param key the key, a dotted name in lower case such as `db.host`.
     * @return the value.
     * @throws com.davils.kontext.core.property.PropertyException if no source knows the key or a source fails.
     * @throws IllegalArgumentException if [key] is not a valid key.
     * @since 1.0.0
     */
    public fun property(key: String): String = properties().required(PropertyKey(key), TEXT)

    /**
     * Reads the property [key] and converts it with [converter], such as `Converters.int` of Kore.
     *
     * @param T the type of the value.
     * @param key the key, a dotted name in lower case such as `db.port`.
     * @param converter converts the raw text.
     * @return the value.
     * @throws com.davils.kontext.core.property.PropertyException if no source knows the key, a source fails or the
     *   text cannot be converted; the message never shows the value.
     * @throws IllegalArgumentException if [key] is not a valid key.
     * @since 1.0.0
     */
    public fun <T> property(key: String, converter: Converter<T>): T = properties().required(
        PropertyKey(key),
        converter,
    )

    /**
     * Reads the property [key] like [property], or returns [default] if no source knows it.
     *
     * @param T the type of the value.
     * @param key the key.
     * @param converter converts the raw text.
     * @param default the value of an unknown key.
     * @return the value or [default].
     * @throws com.davils.kontext.core.property.PropertyException if a source fails or the text cannot be converted.
     * @throws IllegalArgumentException if [key] is not a valid key.
     * @since 1.0.0
     */
    public fun <T> property(key: String, converter: Converter<T>, default: T): T =
        properties().converted(PropertyKey(key), converter) ?: default

    /**
     * Reads the property [key] like [property], or returns `null` if no source knows it.
     *
     * @param T the type of the value.
     * @param key the key.
     * @param converter converts the raw text.
     * @return the value or `null`.
     * @throws com.davils.kontext.core.property.PropertyException if a source fails or the text cannot be converted.
     * @throws IllegalArgumentException if [key] is not a valid key.
     * @since 1.0.0
     */
    public fun <T> propertyOrNull(key: String, converter: Converter<T>): T? =
        properties().converted(PropertyKey(key), converter)

    @PublishedApi
    internal abstract fun detached(): Resolver

    internal abstract fun properties(): Properties
}

private val TEXT: Converter<String> = Converter { raw -> raw }
