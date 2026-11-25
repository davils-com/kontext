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

import com.davils.kollect.core.health.Health
import com.davils.kollect.core.health.HealthIndicator
import com.davils.kollect.core.health.ReasonCode
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.error.ScopeNotOpenException
import com.davils.kontext.core.internal.ContainerRuntime
import com.davils.kontext.core.internal.Properties
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.core.parameter.Parameters
import com.davils.kore.core.annotation.DavilsInternal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KType

/**
 * A running set of definitions, started by [container]: it resolves instances, opens scopes and child containers,
 * loads and unloads modules and closes everything it created.
 *
 * A container is thread-safe and no global: an application holds its container and passes it on, or keeps it in a
 * [com.davils.kontext.core.holder.ContainerHolder].
 *
 * @since 1.0.0
 */
public class Container internal constructor(private val runtime: ContainerRuntime) : Resolver(), HealthIndicator {
    /**
     * The name of the container.
     *
     * @since 1.0.0
     */
    public val name: ContainerName get() = runtime.name

    /**
     * The profile the container runs in.
     *
     * @since 1.0.0
     */
    public val profile: Profile get() = runtime.profile

    /**
     * Where the container is in its life.
     *
     * @since 1.0.0
     */
    public val state: StateFlow<ContainerState> get() = runtime.state

    /**
     * What the container does, while someone collects it; the oldest events are dropped for a slow collector.
     *
     * @since 1.0.0
     */
    public val events: SharedFlow<ContainerEvent> get() = runtime.telemetry.events

    /**
     * The coroutine scope of the container, cancelled when it closes.
     *
     * @since 1.0.0
     */
    public val coroutineScope: CoroutineScope get() = runtime.coroutineScope

    /**
     * The keys of the definitions the container holds outside of scopes, its profile included.
     *
     * @since 1.0.0
     */
    public val definitions: Set<DefinitionKey> get() = runtime.registry.keys

    /**
     * The keys of the scopes the container can open.
     *
     * @since 1.0.0
     */
    public val scopes: Set<ScopeKey> get() = runtime.registry.scopes

    /**
     * Whether every lazy module is loaded.
     *
     * @since 1.0.0
     */
    public val isReady: Boolean get() = !runtime.isLoading

    /**
     * The events the sink or the event bus of the container did not take.
     *
     * @since 1.0.0
     */
    public val rejectedEvents: Long get() = runtime.telemetry.rejected

    /**
     * Waits until every lazy module is loaded.
     *
     * @throws Throwable what failed the loading of a lazy module, or
     *   [com.davils.kontext.core.error.ContainerClosedException] if the container closed first.
     * @since 1.0.0
     */
    public suspend fun awaitReady() {
        runtime.awaitReady()
    }

    /**
     * Opens a scope of [key] on the container, with the values [seed] provides.
     *
     * @param key the key of the scope, declared outside of any other scope.
     * @param id the id to find the scope again by, `null` for none.
     * @param seed provides the values of the scope.
     * @return the open scope, which the caller closes.
     * @throws com.davils.kontext.core.error.ContainerClosedException if the container is not running.
     * @throws com.davils.kontext.core.error.ScopeNotDeclaredException if no module declares the scope.
     * @throws com.davils.kontext.core.error.ScopeNestingException if the scope is declared within another scope.
     * @throws com.davils.kontext.core.error.ScopeSeedException if [seed] provides an undeclared value.
     * @throws com.davils.kontext.core.error.ScopeAlreadyOpenException if an open scope holds [id].
     * @throws com.davils.kontext.core.error.ScopeLimitException if the container holds its limit of open scopes.
     * @since 1.0.0
     */
    public fun openScope(key: ScopeKey, id: ScopeId? = null, seed: ScopeSeed.() -> Unit = {}): Scope =
        Scope(runtime.scopes.open(key = key, parent = null, seeds = seedsOf(seed), scopeId = id))

    /**
     * Returns the open scope with [id], or opens one of [key] with it, atomically.
     *
     * @param key the key of the scope.
     * @param id the id of the scope.
     * @param seed provides the values of a newly opened scope; it does not run for an existing one.
     * @return the scope.
     * @throws IllegalArgumentException if the open scope with [id] has another key.
     * @throws com.davils.kontext.core.error.ContainerClosedException if the container is not running.
     * @since 1.0.0
     */
    public fun openOrGetScope(key: ScopeKey, id: ScopeId, seed: ScopeSeed.() -> Unit = {}): Scope =
        Scope(runtime.scopes.openOrGet(key = key, scopeId = id, seeds = seedsOf(seed)))

    /**
     * Returns the open scope with [id].
     *
     * @param id the id.
     * @return the scope.
     * @throws ScopeNotOpenException if no open scope has the id.
     * @since 1.0.0
     */
    public fun scope(id: ScopeId): Scope = scopeOrNull(id) ?: throw ScopeNotOpenException(id)

    /**
     * Returns the open scope with [id], or `null`.
     *
     * @param id the id.
     * @return the scope or `null`.
     * @since 1.0.0
     */
    public fun scopeOrNull(id: ScopeId): Scope? = runtime.scopes.byId(id)?.let(::Scope)

    /**
     * Starts a child container: it resolves its own definitions first and those of this container after them, while
     * this container never sees the definitions of the child. Closing this container closes the child first.
     *
     * @param profile the profile of the child.
     * @param block configures the child, which starts with the settings of this container.
     * @return the running child.
     * @throws com.davils.kontext.core.error.ContainerClosedException if this container is not running.
     * @throws com.davils.kore.core.validation.report.ValidationException if the configuration is refused.
     * @throws com.davils.kontext.core.error.ResolutionException if an eager single fails.
     * @since 1.0.0
     */
    public suspend fun child(profile: Profile = this.profile, block: ContainerBuilder.() -> Unit): Container =
        Container(runtime.child(profile, block))

    /**
     * Loads [modules] into the running container, validated together with what it holds; the eager singles of the
     * modules, or all of their singles if [eager] is set, are created before this returns.
     *
     * @param modules the modules.
     * @param eager whether every single of the modules is created at once.
     * @return the handle to unload the modules.
     * @throws com.davils.kore.core.validation.report.ValidationException if the modules conflict with the
     *   container, such as by a duplicate definition; nothing is loaded then.
     * @throws com.davils.kontext.core.error.ResolutionException if an eager single fails; the modules are unloaded.
     * @throws com.davils.kontext.core.error.ContainerClosedException if the container is not running.
     * @since 1.0.0
     */
    public suspend fun load(vararg modules: Module, eager: Boolean = false): ModuleHandle =
        ModuleHandle(runtime, runtime.loader.load(modules.toList(), eager))

    /**
     * Declares [instance] as a single of [T] with [qualifier] in the running container.
     *
     * @param T the type the instance is found as.
     * @param instance the instance.
     * @param qualifier the qualifier, `null` for none.
     * @param close closes the instance when it is unloaded or the container closes, `null` for nothing to close.
     * @return the handle to remove the declaration again.
     * @throws com.davils.kore.core.validation.report.ValidationException if the key is declared already.
     * @throws com.davils.kontext.core.error.ContainerClosedException if the container is not running.
     * @since 1.0.0
     */
    public suspend inline fun <reified T : Any> declare(
        instance: T,
        qualifier: Qualifier? = null,
        noinline close: (suspend (T) -> Unit)? = null,
    ): ModuleHandle = declareModule { name ->
        module(
            name,
        ) { single(qualifier = qualifier, close = close) { instance } }
    }

    /**
     * Closes the extensions, the child containers, the open scopes and the coroutine scope, and then every instance
     * with a close function, in the reverse order of creation. A failing close function does not stop the others;
     * its failure is in the report.
     *
     * @return the report, [CloseReport.AlreadyClosed] if the container was closed before.
     * @since 1.0.0
     */
    public suspend fun close(): CloseReport = runtime.close()

    /**
     * Runs [handler] once the container is closed, at once if it is closed already. A throwing handler is ignored.
     *
     * @param handler what to run.
     * @since 1.0.0
     */
    @DavilsInternal
    public fun onClosed(handler: () -> Unit) {
        runtime.onClosed(handler)
    }

    /**
     * Reports the container up while it runs and down otherwise.
     *
     * @return the health of the container.
     * @since 1.0.0
     */
    override suspend fun check(): Health {
        if (state.value == ContainerState.Running) return Health.Up
        return Health.down(ReasonCode.StateMatched)
    }

    @PublishedApi
    internal suspend fun declareModule(module: (String) -> Module): ModuleHandle =
        ModuleHandle(runtime, runtime.loader.declare(module))

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

    override fun properties(): Properties = runtime.properties

    internal fun runtime(): ContainerRuntime = runtime

    override fun toString(): String = "Container($name, $profile, ${state.value})"
}
