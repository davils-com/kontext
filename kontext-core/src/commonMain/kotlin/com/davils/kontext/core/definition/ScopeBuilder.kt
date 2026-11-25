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

package com.davils.kontext.core.definition

import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Profile
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.extension.Retention
import com.davils.kore.core.annotation.DavilsDsl
import kotlin.reflect.typeOf

private const val NONE: String = "none"

/**
 * Declares the definitions of one scope, see [ModuleBuilder.scope].
 *
 * @property key the key of the scope.
 * @property within the key of the scope this scope is opened in, `null` for one opened on the container.
 * @param profiles the profiles of the variant the scope is declared in, empty for every profile.
 * @since 1.0.0
 */
@DavilsDsl
public class ScopeBuilder internal constructor(
    public val key: ScopeKey,
    public val within: ScopeKey?,
    override val profiles: Set<Profile>,
) : DefinitionBuilder(key) {
    /**
     * Declares a scoped definition: one instance of [T] per opened scope, closed with the scope by [close]. A scoped
     * definition refuses parameters.
     *
     * @param T the type of the instance.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param retention how long the instance is reused, [Retention.FOREVER] by default.
     * @param close closes the instance when its scope closes, `null` for nothing to close.
     * @param create creates the instance and resolves what it needs from its receiver.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> scoped(
        qualifier: Qualifier? = null,
        retention: Retention = Retention.FOREVER,
        noinline close: (suspend (T) -> Unit)? = null,
        noinline create: DefinitionContext.() -> T,
    ): Definition<T> = declareSync(
        key = DefinitionKey(typeOf<T>(), qualifier),
        lifetime = Lifetime.Scoped,
        create = create,
        close = close,
        retention = retention,
    )

    /**
     * Declares a suspending scoped definition, resolved with [com.davils.kontext.core.Resolver.await]: one instance
     * per opened scope, created at most once at a time.
     *
     * @param T the type of the instance.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param retention how long the instance is reused, [Retention.FOREVER] by default.
     * @param close closes the instance when its scope closes, `null` for nothing to close.
     * @param create creates the instance, suspending as long as it needs.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> scopedAsync(
        qualifier: Qualifier? = null,
        retention: Retention = Retention.FOREVER,
        noinline close: (suspend (T) -> Unit)? = null,
        noinline create: suspend DefinitionContext.() -> T,
    ): Definition<T> = declareAsync(
        key = DefinitionKey(typeOf<T>(), qualifier),
        lifetime = Lifetime.Scoped,
        create = create,
        close = close,
        retention = retention,
    )

    /**
     * Declares a value the opener of the scope provides, see [com.davils.kontext.core.ScopeSeed.provide] and
     * [com.davils.kontext.core.Scope.declare], such as the request a request scope serves.
     *
     * Resolving it in a scope that was not provided with it fails with
     * [com.davils.kontext.core.error.ResolutionFailure.Unprovided].
     *
     * @param T the type of the value.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> provided(qualifier: Qualifier? = null): Definition<T> =
        declareProvided(DefinitionKey(typeOf<T>(), qualifier))

    internal fun declaration(): ScopeDeclaration = ScopeDeclaration(key = key, within = within, profiles = profiles)

    override fun toString(): String = "ScopeBuilder($key, within=${within ?: NONE})"
}
