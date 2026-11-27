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

package com.davils.kontext.kheck

import com.davils.kheck.api.Logger
import com.davils.kheck.core.Logging
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.Resolver

/**
 * Returns the logger of [name] from the [Logging] the container declares with [qualifier]; the same name returns the
 * same logger.
 *
 * @receiver the resolver of a definition or of the container.
 * @param name the dotted name of the logger, such as `app.payments`; empty for the root.
 * @param qualifier the qualifier of the logging, `null` for none.
 * @return the logger.
 * @throws com.davils.kontext.core.error.ResolutionException if the container declares no such logging or it fails to
 *   start.
 * @throws IllegalArgumentException if [name] is longer than the limit of kheck for a name.
 * @since 1.3.0
 */
public fun Resolver.logger(name: String, qualifier: Qualifier? = null): Logger = get<Logging>(qualifier).logger(name)

/**
 * Returns the logger named after [T], as kheck names it, from the [Logging] the container declares with [qualifier].
 *
 * @param T the class that logs.
 * @receiver the resolver of a definition or of the container.
 * @param qualifier the qualifier of the logging, `null` for none.
 * @return the logger.
 * @throws com.davils.kontext.core.error.ResolutionException if the container declares no such logging or it fails to
 *   start.
 * @since 1.3.0
 */
public inline fun <reified T : Any> Resolver.logger(qualifier: Qualifier? = null): Logger =
    get<Logging>(qualifier).logger(T::class)
