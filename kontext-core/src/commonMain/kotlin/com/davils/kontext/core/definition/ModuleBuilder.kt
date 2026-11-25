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
import com.davils.kontext.core.Module
import com.davils.kontext.core.ModuleName
import com.davils.kontext.core.Profile
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.extension.Retention
import com.davils.kore.core.annotation.DavilsDsl
import kotlin.reflect.typeOf

/**
 * Declares the definitions of a module, see [com.davils.kontext.core.module].
 *
 * @property name the name of the module.
 * @property isEager whether the singles of the module are eager unless they say otherwise.
 * @since 1.0.0
 */
@DavilsDsl
public class ModuleBuilder internal constructor(public val name: ModuleName, public val isEager: Boolean = false) :
    DefinitionBuilder(null) {
    private val includes = mutableListOf<Module>()
    private val scopes = mutableListOf<ScopeBuilder>()
    private var variantProfiles: Set<Profile> = emptySet()

    override val profiles: Set<Profile> get() = variantProfiles

    /**
     * Declares a single: one instance of [T] per container, closed with the container by [close].
     *
     * A single is created on its first resolution, at most once even under concurrent resolution; an [eager] single
     * is created when the container starts, so that a broken one fails the start. A single refuses parameters.
     *
     * @param T the type of the instance.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param eager whether the container creates the instance when it starts.
     * @param retention how long the instance is reused, [Retention.FOREVER] by default.
     * @param close closes the instance when its container closes, `null` for nothing to close.
     * @param create creates the instance and resolves what it needs from its receiver.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> single(
        qualifier: Qualifier? = null,
        eager: Boolean = this.isEager,
        retention: Retention = Retention.FOREVER,
        noinline close: (suspend (T) -> Unit)? = null,
        noinline create: DefinitionContext.() -> T,
    ): Definition<T> = declareSync(
        key = DefinitionKey(typeOf<T>(), qualifier),
        lifetime = Lifetime.Single,
        create = create,
        isEager = eager,
        close = close,
        retention = retention,
    )

    /**
     * Declares a suspending single, resolved with [com.davils.kontext.core.Resolver.await]: [create] runs at most
     * once at a time, shared by every caller; a caller that is cancelled while it waits does not cancel the creation
     * for the others. An [eager] suspending single is created when the container starts, concurrently with the other
     * eager suspending singles.
     *
     * @param T the type of the instance.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param eager whether the container creates the instance when it starts.
     * @param retention how long the instance is reused, [Retention.FOREVER] by default.
     * @param close closes the instance when its container closes, `null` for nothing to close.
     * @param create creates the instance, suspending as long as it needs.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> singleAsync(
        qualifier: Qualifier? = null,
        eager: Boolean = this.isEager,
        retention: Retention = Retention.FOREVER,
        noinline close: (suspend (T) -> Unit)? = null,
        noinline create: suspend DefinitionContext.() -> T,
    ): Definition<T> = declareAsync(
        key = DefinitionKey(typeOf<T>(), qualifier),
        lifetime = Lifetime.Single,
        create = create,
        isEager = eager,
        close = close,
        retention = retention,
    )

    /**
     * Declares the scope [key] and its definitions. Several modules may declare definitions of the same scope, as
     * long as they agree on [within].
     *
     * Within a [variant], the scope and its definitions exist only in the profiles of the variant: a container in any
     * other profile does not know the scope, unless another declaration outside a variant declares it as well.
     *
     * @param key the key of the scope.
     * @param within the key of the scope it is opened in, `null` for one opened on the container.
     * @param block declares the definitions of the scope.
     * @since 1.0.0
     */
    public fun scope(key: ScopeKey, within: ScopeKey? = null, block: ScopeBuilder.() -> Unit) {
        val builder = ScopeBuilder(key = key, within = within, profiles = variantProfiles)
        builder.block()
        scopes += builder
    }

    /**
     * Declares the definitions of [block] for [profile] only, see the overload for several profiles.
     *
     * @param profile the profile the definitions exist in.
     * @param block declares the definitions, including scopes, but no included module.
     * @throws IllegalStateException if called within another variant, or if [block] includes a module.
     * @since 1.0.0
     */
    public fun variant(profile: Profile, block: ModuleBuilder.() -> Unit) {
        variant(setOf(profile), block)
    }

    /**
     * Declares the definitions of [block] for [profiles] only: a container in any other profile does not hold
     * them, so that, for example, a live and a mock implementation share one key.
     *
     * @param profiles the profiles the definitions exist in.
     * @param block declares the definitions, including scopes, but no included module.
     * @throws IllegalArgumentException if no profile is named.
     * @throws IllegalStateException if called within another variant, or if [block] includes a module.
     * @since 1.0.0
     */
    public fun variant(profiles: Set<Profile>, block: ModuleBuilder.() -> Unit) {
        require(profiles.isNotEmpty()) { "A variant names at least one profile" }
        check(variantProfiles.isEmpty()) { "A variant cannot be declared within another variant" }
        variantProfiles = profiles.toSet()
        try {
            block()
        } finally {
            variantProfiles = emptySet()
        }
    }

    /**
     * Includes [modules]: a container that holds this module holds them as well, each once, with the definitions of
     * all their profiles.
     *
     * An include is refused within a [variant], because an included module is shared by identity and would otherwise
     * install its definitions in every profile, such as a fake module of a test profile in production. Declare the
     * variant inside the included module, or include the module only in the module set of the profile instead.
     *
     * @param modules the modules to include.
     * @throws IllegalStateException if called within a variant.
     * @since 1.0.0
     */
    public fun include(vararg modules: Module) {
        check(variantProfiles.isEmpty()) {
            "A module cannot be included within a variant, because it would hold its definitions in every profile"
        }
        includes += modules
    }

    override fun declarations(): List<Declaration> =
        super.declarations() + scopes.flatMap { scope -> scope.declarations() }

    internal fun build(): Module = Module(
        name = name,
        declarations = declarations(),
        scopes = scopes.map { scope -> scope.declaration() },
        includes = includes.toList(),
    )

    override fun toString(): String = "ModuleBuilder($name)"
}
