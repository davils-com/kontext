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

import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerConfiguration
import com.davils.kontext.core.internal.Properties
import com.davils.kontext.core.internal.ScopeRuntime
import com.davils.kontext.core.parameter.Parameters
import com.davils.kore.core.identity.UuidV7
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * An open scope: it resolves the definitions of its key, those of the scopes it is opened in, those of the scopes it
 * links to and those of its container, and closes its scoped instances.
 *
 * A scope is thread-safe.
 *
 * @since 1.0.0
 */
public class Scope internal constructor(private val runtime: ScopeRuntime) : Resolver() {
    /**
     * The key of the scope.
     *
     * @since 1.0.0
     */
    public val key: ScopeKey get() = runtime.key

    /**
     * The unique id of this opened scope.
     *
     * @since 1.0.0
     */
    public val id: UuidV7 get() = runtime.id

    /**
     * The id the opener gave the scope, `null` for none.
     *
     * @since 1.0.0
     */
    public val scopeId: ScopeId? get() = runtime.scopeId

    /**
     * Whether the scope is open.
     *
     * @since 1.0.0
     */
    public val isOpen: Boolean get() = runtime.isOpen

    /**
     * The coroutine scope of the scope, cancelled when it closes.
     *
     * @since 1.0.0
     */
    public val coroutineScope: CoroutineScope get() = runtime.coroutineScope

    /**
     * Opens a scope of [key] within this scope, with the values [seed] provides.
     *
     * @param key the key of the scope, declared within the key of this scope.
     * @param id the id to find the scope again by, `null` for none.
     * @param seed provides the values of the scope.
     * @return the open scope, which the caller closes; closing this scope closes it as well.
     * @throws com.davils.kontext.core.error.ContainerClosedException if the container is not running.
     * @throws com.davils.kontext.core.error.ScopeNotDeclaredException if no module declares the scope.
     * @throws com.davils.kontext.core.error.ScopeNestingException if the scope is not declared within this one.
     * @throws com.davils.kontext.core.error.ScopeSeedException if [seed] provides an undeclared value.
     * @since 1.0.0
     */
    public fun openScope(key: ScopeKey, id: ScopeId? = null, seed: ScopeSeed.() -> Unit = {}): Scope =
        Scope(runtime.openScope(key, id, seedsOf(seed)))

    /**
     * Provides [instance] as the value of the provided definition of [T] with [qualifier], once, after the scope was
     * opened, such as a value only known after the scope started.
     *
     * @param T the type of the value.
     * @param instance the value.
     * @param qualifier the qualifier, `null` for none.
     * @throws com.davils.kontext.core.error.ScopeSeedException if no provided definition of the scope has the key or
     *   the value is provided already.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> declare(instance: T, qualifier: Qualifier? = null) {
        declareKey(DefinitionKey(typeOf<T>(), qualifier), instance)
    }

    /**
     * Makes this scope resolve from [other] as well, after its own and its enclosing scopes and before the
     * container; the link ends when either scope closes.
     *
     * @param other an open scope of the same container.
     * @throws IllegalArgumentException if [other] is this scope or belongs to another container, or if this scope
     *   already links to [com.davils.kontext.core.ContainerLimits.MAX_LINKS] scopes.
     * @since 1.0.0
     */
    public fun link(other: Scope) {
        runtime.link(other.runtime)
    }

    /**
     * Ends the link to [other].
     *
     * @param other the linked scope.
     * @return whether there was such a link.
     * @since 1.0.0
     */
    public fun unlink(other: Scope): Boolean = runtime.unlink(other.runtime)

    /**
     * Runs [handler] once the scope is closed, at once if it is closed already. A throwing handler is ignored.
     *
     * @param handler what to run.
     * @since 1.0.0
     */
    public fun onClosed(handler: () -> Unit) {
        runtime.onClosed(handler)
    }

    /**
     * Closes the scopes opened within this one, cancels its coroutine scope and then closes its instances with a
     * close function, in the reverse order of creation. Closing runs to the end even when the calling coroutine is
     * cancelled, and nothing is created in the scope once it closes. A concurrent call waits until the first one has
     * closed everything; a call from a close function of the same scope returns at once.
     *
     * @return the report, [CloseReport.AlreadyClosed] if the scope was closed or is closing.
     * @since 1.0.0
     */
    public suspend fun close(): CloseReport = runtime.close()

    @PublishedApi
    internal fun declareKey(key: DefinitionKey, instance: Any) {
        runtime.declare(key, instance)
    }

    override fun resolve(key: DefinitionKey, parameters: Parameters): Any = runtime.context.resolve(key, parameters)

    override fun resolveOrNull(key: DefinitionKey, parameters: Parameters): Any? =
        runtime.context.resolveOrNull(key, parameters)

    override fun resolveAll(type: KType): List<Any> = runtime.context.resolveAll(type)

    override suspend fun resolveAsync(key: DefinitionKey, parameters: Parameters): Any =
        runtime.context.resolveAsync(key, parameters)

    override suspend fun resolveAsyncOrNull(key: DefinitionKey, parameters: Parameters): Any? =
        runtime.context.resolveAsyncOrNull(key, parameters)

    override suspend fun resolveAllAsync(type: KType): List<Any> = runtime.context.resolveAllAsync(type)

    override fun detached(): Resolver = runtime.context

    override fun properties(): Properties = runtime.runtime.properties

    override fun configuration(): ContainerConfiguration = runtime.runtime.configuration

    internal fun runtime(): ScopeRuntime = runtime

    override fun equals(other: Any?): Boolean = other is Scope && runtime === other.runtime

    override fun hashCode(): Int = runtime.hashCode()

    override fun toString(): String = "Scope($key, $id, open=$isOpen)"
}
