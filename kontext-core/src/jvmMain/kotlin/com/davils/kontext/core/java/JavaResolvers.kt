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

package com.davils.kontext.core.java

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.Resolver
import com.davils.kontext.core.parameter.Parameters
import java.util.function.Supplier

/**
 * Resolves instances for Java callers, by their class instead of a reified type.
 *
 * A class finds the definitions of exactly that type without generic arguments, such as `Repository.class` for
 * `single<Repository> { … }`; a generic definition such as `List<String>` cannot be named from Java.
 *
 * @since 1.0.0
 */
public object JavaResolvers {
    /**
     * Resolves the instance of [type] with [qualifier].
     *
     * @param T the type.
     * @param resolver the container or scope to resolve from.
     * @param type the class of the definition.
     * @param qualifier the qualifier, `null` for none.
     * @param parameters the parameters for a factory.
     * @return the instance.
     * @throws com.davils.kontext.core.error.ResolutionException if it cannot be resolved.
     * @since 1.0.0
     */
    @JvmStatic
    @JvmOverloads
    public fun <T : Any> get(
        resolver: Resolver,
        type: Class<T>,
        qualifier: Qualifier? = null,
        parameters: Parameters = Parameters.EMPTY,
    ): T = type.cast(resolver.resolve(keyOf(type, qualifier), parameters))

    /**
     * Resolves the instance of [type] with [qualifier], or `null` if nothing is declared under that key.
     *
     * @param T the type.
     * @param resolver the container or scope to resolve from.
     * @param type the class of the definition.
     * @param qualifier the qualifier, `null` for none.
     * @return the instance or `null`.
     * @throws com.davils.kontext.core.error.ResolutionException if a definition fails to resolve.
     * @since 1.0.0
     */
    @JvmStatic
    @JvmOverloads
    public fun <T : Any> getOrNull(resolver: Resolver, type: Class<T>, qualifier: Qualifier? = null): T? =
        resolver.resolveOrNull(keyOf(type, qualifier))?.let(type::cast)

    /**
     * Returns a supplier that resolves the instance of [type] on its first call and returns it on every call.
     *
     * @param T the type.
     * @param resolver the container or scope to resolve from.
     * @param type the class of the definition.
     * @param qualifier the qualifier, `null` for none.
     * @return the thread-safe supplier.
     * @since 1.0.0
     */
    @JvmStatic
    @JvmOverloads
    public fun <T : Any> inject(resolver: Resolver, type: Class<T>, qualifier: Qualifier? = null): Supplier<T> {
        val resolved = lazy { get(resolver, type, qualifier) }
        return Supplier { resolved.value }
    }

    /**
     * Returns the key of [type] with [qualifier].
     *
     * @param type the class.
     * @param qualifier the qualifier, `null` for none.
     * @return the key.
     * @since 1.0.0
     */
    @JvmStatic
    @JvmOverloads
    public fun keyOf(type: Class<*>, qualifier: Qualifier? = null): DefinitionKey = DefinitionKey(
        JavaType(type),
        qualifier,
    )
}
