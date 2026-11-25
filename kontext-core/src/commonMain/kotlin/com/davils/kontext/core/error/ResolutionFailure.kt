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

package com.davils.kontext.core.error

import com.davils.kontext.core.DefinitionKey
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList

/**
 * Why a resolution failed, with the definitions it was resolving on the way to [key].
 *
 * @property key the key that could not be resolved.
 * @param path the keys being resolved when the resolution failed, outermost first.
 * @since 1.0.0
 */
public sealed class ResolutionFailure(public val key: DefinitionKey, path: List<DefinitionKey>) {
    /**
     * The keys being resolved when the resolution failed, outermost first, without [key].
     *
     * @since 1.0.0
     */
    public val path: List<DefinitionKey> = path.immutableList()

    /**
     * A short name of the reason in lower case, such as `missing`, used as a label of metrics.
     *
     * @since 1.0.0
     */
    public abstract val reason: String

    /**
     * Nothing is declared under [key].
     *
     * @since 1.0.0
     */
    public class Missing internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "missing"

        override fun toString(): String = "Missing($key, path=$path)"
    }

    /**
     * Resolving [key] needs [key] itself; [com.davils.kontext.core.Resolver.getLazy] breaks such a cycle.
     *
     * @since 1.0.0
     */
    public class Circular internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "circular"

        override fun toString(): String = "Circular($key, path=$path)"
    }

    /**
     * Several definitions are bound to [key], so none of them is chosen.
     *
     * @since 1.0.0
     */
    public class Ambiguous internal constructor(
        key: DefinitionKey,
        candidates: List<DefinitionKey>,
        path: List<DefinitionKey>,
    ) : ResolutionFailure(key, path) {
        /**
         * The keys of the definitions bound to [key].
         *
         * @since 1.0.0
         */
        public val candidates: List<DefinitionKey> = candidates.immutableList()

        override val reason: String get() = "ambiguous"

        override fun toString(): String = "Ambiguous($key, candidates=$candidates, path=$path)"
    }

    /**
     * The factory of [key] threw [cause].
     *
     * @property cause what the factory threw.
     * @since 1.0.0
     */
    public class Failed internal constructor(
        key: DefinitionKey,
        public val cause: Throwable,
        path: List<DefinitionKey>,
    ) : ResolutionFailure(key, path) {
        override val reason: String get() = "failed"

        override fun toString(): String = "Failed($key, cause=${typeNameOf(cause)}, path=$path)"
    }

    /**
     * [key] is provided by the opener of its scope, which did not provide it.
     *
     * @since 1.0.0
     */
    public class Unprovided internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "unprovided"

        override fun toString(): String = "Unprovided($key, path=$path)"
    }

    /**
     * The container or the scope resolved from is closed.
     *
     * @since 1.0.0
     */
    public class Closed internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "closed"

        override fun toString(): String = "Closed($key, path=$path)"
    }

    /**
     * [key] is created by a suspending factory and not created yet, so it is resolved with
     * [com.davils.kontext.core.Resolver.await] instead of [com.davils.kontext.core.Resolver.get].
     *
     * @since 1.0.0
     */
    public class Suspending internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "suspending"

        override fun toString(): String = "Suspending($key, path=$path)"
    }

    /**
     * Nothing is declared under [key] yet, while lazy modules are still loading; [com.davils.kontext.core.Resolver.await]
     * waits for them.
     *
     * @since 1.0.0
     */
    public class NotReady internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "not-ready"

        override fun toString(): String = "NotReady($key, path=$path)"
    }

    /**
     * Parameters were passed to a single or a scoped definition, which is created once and could not honour them on
     * a later resolution.
     *
     * @since 1.0.0
     */
    public class ParametersRefused internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "parameters-refused"

        override fun toString(): String = "ParametersRefused($key, path=$path)"
    }

    /**
     * A [com.davils.kontext.core.extension.ResolutionFallback] supplied an instance of another type than [key].
     *
     * @since 1.0.0
     */
    public class Mismatched internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "mismatched"

        override fun toString(): String = "Mismatched($key, path=$path)"
    }

    /**
     * The chain of definitions grew beyond [com.davils.kontext.core.ContainerLimits.MAX_RESOLUTION_DEPTH].
     *
     * @since 1.0.0
     */
    public class TooDeep internal constructor(key: DefinitionKey, path: List<DefinitionKey>) :
        ResolutionFailure(key, path) {
        override val reason: String get() = "too-deep"

        override fun toString(): String = "TooDeep($key, depth=${path.size})"
    }
}

internal fun typeNameOf(cause: Throwable): String = cause::class.simpleName ?: "Throwable"
