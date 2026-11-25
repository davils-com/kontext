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
import com.davils.kontext.core.Resolver
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.extension.Retention
import com.davils.kore.core.annotation.DavilsDsl
import kotlin.reflect.typeOf

/**
 * The declarations a module and a scope share: factories, suspending factories and assisted factories.
 *
 * A builder is not thread-safe; it is filled by the one thread that runs its block.
 *
 * @since 1.0.0
 */
@DavilsDsl
public abstract class DefinitionBuilder internal constructor(private val owner: ScopeKey?) {
    private val drafts = mutableListOf<DefinitionDraft>()

    internal abstract val profiles: Set<Profile>

    /**
     * Declares a factory: every resolution of [T] with [qualifier] runs [create] again, with the parameters of that
     * resolution, and Kontext never closes what it returns.
     *
     * @param T the type the factory creates.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param create creates an instance and resolves what it needs from its receiver.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> factory(
        qualifier: Qualifier? = null,
        noinline create: DefinitionContext.() -> T,
    ): Definition<T> = declareSync(
        key = DefinitionKey(typeOf<T>(), qualifier),
        lifetime = Lifetime.Factory,
        create = create,
    )

    /**
     * Declares a suspending factory, resolved with [Resolver.await]: every resolution runs [create] again.
     *
     * @param T the type the factory creates.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param create creates an instance, suspending as long as it needs, and resolves what it needs from its
     *   receiver.
     * @return the definition, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> factoryAsync(
        qualifier: Qualifier? = null,
        noinline create: suspend DefinitionContext.() -> T,
    ): Definition<T> = declareAsync(
        key = DefinitionKey(typeOf<T>(), qualifier),
        lifetime = Lifetime.Factory,
        create = create,
    )

    /**
     * Declares an assisted factory: the definition resolves to a function `(P) -> T` that runs [create] with the
     * argument the caller passes, for values only known at the call, such as an id.
     *
     * The function is created once per container, or once per scope when declared in a scope, and resolves from
     * where it is declared.
     *
     * @param P the type of the argument.
     * @param T the type the function creates.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @param create creates an instance from the argument and resolves what it needs from its receiver.
     * @return the definition of the function, to bind it to further types.
     * @since 1.0.0
     */
    public inline fun <reified P, reified T : Any> assisted(
        qualifier: Qualifier? = null,
        noinline create: Resolver.(P) -> T,
    ): Definition<(P) -> T> = declareAssisted(DefinitionKey(typeOf<(P) -> T>(), qualifier), create)

    @PublishedApi
    internal fun <T : Any> declareSync(
        key: DefinitionKey,
        lifetime: Lifetime,
        create: DefinitionContext.() -> T,
        isEager: Boolean = false,
        close: (suspend (T) -> Unit)? = null,
        dependencies: List<DefinitionKey>? = null,
        retention: Retention = Retention.FOREVER,
    ): Definition<T> = declare(
        key = key,
        lifetime = lifetime,
        production = Production.Sync(create),
        isEager = isEager,
        close = close,
        dependencies = dependencies,
        retention = retention,
    )

    @PublishedApi
    internal fun <T : Any> declareAsync(
        key: DefinitionKey,
        lifetime: Lifetime,
        create: suspend DefinitionContext.() -> T,
        isEager: Boolean = false,
        close: (suspend (T) -> Unit)? = null,
        retention: Retention = Retention.FOREVER,
    ): Definition<T> = declare(
        key = key,
        lifetime = lifetime,
        production = Production.Async(create),
        isEager = isEager,
        close = close,
        retention = retention,
    )

    @PublishedApi
    internal fun <T : Any> declareProvided(key: DefinitionKey): Definition<T> =
        declare(key = key, lifetime = Lifetime.Scoped, production = Production.Provided)

    internal fun <T : Any> declare(
        key: DefinitionKey,
        lifetime: Lifetime,
        production: Production,
        isEager: Boolean = false,
        close: (suspend (T) -> Unit)? = null,
        dependencies: List<DefinitionKey>? = null,
        retention: Retention = Retention.FOREVER,
    ): Definition<T> {
        val declaration = Declaration(
            key = key,
            lifetime = lifetime,
            owner = owner,
            profiles = profiles,
            production = production,
            isEager = isEager,
            dispose = close?.let(::disposerOf),
            dependencies = dependencies,
            retention = retention,
        )
        val draft = DefinitionDraft(key, declaration)
        drafts += draft
        return Definition(draft)
    }

    @PublishedApi
    internal fun <P, T : Any> declareAssisted(key: DefinitionKey, create: Resolver.(P) -> T): Definition<(P) -> T> {
        val produce: DefinitionContext.() -> (P) -> T = { assistedBy(detached(), create) }
        return declareSync(key = key, lifetime = assistedLifetime(), create = produce)
    }

    internal open fun declarations(): List<Declaration> = drafts.map { draft -> draft.seal() }

    private fun assistedLifetime(): Lifetime {
        if (owner == null) return Lifetime.Single
        return Lifetime.Scoped
    }
}

private fun <P, T : Any> assistedBy(resolver: Resolver, create: Resolver.(P) -> T): (P) -> T =
    { argument -> resolver.create(argument) }

@Suppress("UNCHECKED_CAST")
private fun <T : Any> disposerOf(close: suspend (T) -> Unit): suspend (Any) -> Unit =
    { instance -> close(instance as T) }
