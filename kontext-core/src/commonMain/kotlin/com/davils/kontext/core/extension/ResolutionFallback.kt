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

package com.davils.kontext.core.extension

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Resolver

/**
 * Supplies instances a container does not declare, such as beans of another injection framework during a migration.
 *
 * A container asks its fallbacks in the order of their registration when no definition matches a key, and takes the
 * first answer that is not `null`. An answer must be an instance of the type of the key; anything else fails the
 * resolution with [com.davils.kontext.core.error.ResolutionFailure.Mismatched] instead of a later cast error. Only the
 * classifier of the key is checked, since type arguments are erased at runtime: a fallback that answers a key of
 * `List<String>` with a `List<Int>` is not refused, so a fallback must check the type arguments of the key itself. The
 * container neither caches nor closes what a fallback supplies. A fallback that throws fails the resolution with
 * [com.davils.kontext.core.error.ResolutionFailure.Failed].
 *
 * @since 1.0.0
 */
public fun interface ResolutionFallback {
    /**
     * Returns the instance of [key], `null` if this fallback has none.
     *
     * @param key the key nothing is declared under.
     * @param resolver the resolver the request came from, to resolve what the instance needs.
     * @return the instance or `null`.
     * @since 1.0.0
     */
    public fun resolve(key: DefinitionKey, resolver: Resolver): Any?
}
