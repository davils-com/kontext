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

package com.davils.kontext.core.component

import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.parameter.Parameters

/**
 * Resolves the instance of [T] on first use, from the resolver of the component at that time.
 *
 * @param T the type of the definition.
 * @param qualifier the qualifier, `null` for none.
 * @param parameters the parameters for a factory.
 * @return the lazily resolved instance, thread-safe.
 * @since 1.0.0
 */
public inline fun <reified T : Any> ContainerComponent.inject(
    qualifier: Qualifier? = null,
    parameters: Parameters = Parameters.EMPTY,
): Lazy<T> = lazy { resolver.get<T>(qualifier, parameters) }

/**
 * Resolves the instance of [T] on first use, or `null` if nothing is declared under its key.
 *
 * @param T the type of the definition.
 * @param qualifier the qualifier, `null` for none.
 * @param parameters the parameters for a factory.
 * @return the lazily resolved instance or `null`, thread-safe.
 * @since 1.0.0
 */
public inline fun <reified T : Any> ContainerComponent.injectOrNull(
    qualifier: Qualifier? = null,
    parameters: Parameters = Parameters.EMPTY,
): Lazy<T?> = lazy { resolver.getOrNull<T>(qualifier, parameters) }

/**
 * Resolves the instance of [T] now.
 *
 * @param T the type of the definition.
 * @param qualifier the qualifier, `null` for none.
 * @param parameters the parameters for a factory.
 * @return the instance.
 * @throws com.davils.kontext.core.error.ResolutionException if it cannot be resolved.
 * @since 1.0.0
 */
public inline fun <reified T : Any> ContainerComponent.get(
    qualifier: Qualifier? = null,
    parameters: Parameters = Parameters.EMPTY,
): T = resolver.get(qualifier, parameters)
