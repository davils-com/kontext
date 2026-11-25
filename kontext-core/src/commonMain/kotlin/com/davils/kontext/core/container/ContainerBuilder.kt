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

package com.davils.kontext.core.container

import com.davils.kollect.core.MeterRegistry
import com.davils.kollect.core.trace.Tracer
import com.davils.konfetti.core.EventBus
import com.davils.kontext.core.ContainerLimits
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.ContainerSetup
import com.davils.kontext.core.LazyModule
import com.davils.kontext.core.Module
import com.davils.kontext.core.Profile
import com.davils.kontext.core.extension.ContainerExtension
import com.davils.kontext.core.extension.ResolutionFallback
import com.davils.kontext.core.internal.ContainerRuntime
import com.davils.kontext.core.observe.ContainerSink
import com.davils.kontext.core.property.PropertiesBuilder
import com.davils.kontext.core.property.PropertySource
import com.davils.kore.core.annotation.DavilsDsl
import com.davils.kore.core.dsl.ValidatingBuilder
import com.davils.kore.core.identity.UuidV7
import com.davils.kore.core.identity.generator.UuidGenerator
import com.davils.kore.core.identity.generator.UuidV7Generator
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.TimeSource

private const val SUBJECT: String = "container"

/**
 * Configures a container, see the function `container` and [com.davils.kontext.core.Container.child].
 *
 * A child container starts with the settings of its parent, except its name, its modules and its lazy modules, and
 * may change them; its property sources are layered over those of its parent.
 *
 * @property profile the profile the container runs in.
 * @param parent the container a child is started in, `null` for a root.
 * @since 1.0.0
 */
@DavilsDsl
public class ContainerBuilder internal constructor(
    public val profile: Profile,
    internal val parent: ContainerRuntime?,
) : ValidatingBuilder<ContainerConfiguration>() {
    private val inherited: ContainerConfiguration? = parent?.configuration
    internal val declared: MutableList<Module> = mutableListOf()
    internal val replaced: MutableList<Module> = mutableListOf()
    internal val lazy: MutableList<LazyModule> = mutableListOf()
    internal val propertySources: MutableList<PropertySource> = mutableListOf()
    internal val extensions: MutableList<ContainerExtension> = inherited?.extensions.orEmpty().toMutableList()
    internal val fallbacks: MutableList<ResolutionFallback> = inherited?.fallbacks.orEmpty().toMutableList()

    override val subject: String get() = SUBJECT

    /**
     * The name of the container, [ROOT] for a root and [CHILD] for a child unless set.
     *
     * @since 1.0.0
     */
    public var name: ContainerName = ROOT.takeIf { parent == null } ?: CHILD

    /**
     * Generates the ids of the opened scopes.
     *
     * @since 1.0.0
     */
    public var ids: UuidGenerator<UuidV7> = inheritedOr(UuidV7Generator) { ids }

    /**
     * Traces the start and the close of the container.
     *
     * @since 1.0.0
     */
    public var tracer: Tracer = inheritedOr(Tracer.None) { tracer }

    /**
     * Where the container records its meters, `null` for none.
     *
     * @since 1.0.0
     */
    public var meters: MeterRegistry? = inherited?.meters

    /**
     * Where the container publishes its events on [com.davils.kontext.core.observe.ContainerEvent.TOPIC], `null`
     * for none.
     *
     * @since 1.0.0
     */
    public var events: EventBus? = inherited?.events

    /**
     * Where the container reports its events synchronously, `null` for none.
     *
     * @since 1.0.0
     */
    public var sink: ContainerSink? = inherited?.sink

    /**
     * Measures how long creating instances, starting and closing take, and the age of instances.
     *
     * @since 1.0.0
     */
    public var timeSource: TimeSource.WithComparableMarks = inheritedOr(TimeSource.Monotonic) { timeSource }

    /**
     * The coroutine context of the coroutine scope of the container, such as a dispatcher and a parent job; the
     * container adds a supervisor job of its own. A child runs in the scope of its parent unless set.
     *
     * @since 1.0.0
     */
    public var coroutineContext: CoroutineContext = inheritedOr(EmptyCoroutineContext) { coroutineContext }

    /**
     * The most scopes the container keeps open at once.
     *
     * @since 1.0.0
     */
    public var scopeLimit: Int = inheritedOr(ContainerLimits.MAX_OPEN_SCOPES) { scopeLimit }

    /**
     * Adds [modules] and the modules they include to the container.
     *
     * @param modules the modules.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun modules(vararg modules: Module) {
        checkOpen()
        declared += modules
    }

    /**
     * Adds [modules] and the modules they include to the container.
     *
     * @param modules the modules.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun modules(modules: List<Module>) {
        checkOpen()
        declared += modules
    }

    /**
     * Adds modules that are declared and loaded only after the container started, see
     * [com.davils.kontext.core.Container.awaitReady].
     *
     * @param modules the lazy modules.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun lazyModules(vararg modules: LazyModule) {
        checkOpen()
        lazy += modules
    }

    /**
     * Replaces the definitions of the declared modules that [modules] declare as well, such as a client for an
     * environment. Every definition of [modules] must replace a declared one; this is the only way to override.
     *
     * @param modules the replacing modules.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun replace(vararg modules: Module) {
        checkOpen()
        replaced += modules
    }

    /**
     * Declares the property sources of the container, layered over those of its parent.
     *
     * @param block adds the sources.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun properties(block: PropertiesBuilder.() -> Unit) {
        checkOpen()
        val builder = PropertiesBuilder()
        builder.block()
        propertySources += builder.sources
    }

    /**
     * Adds [extensions], which hear the life of the container and its children.
     *
     * @param extensions the extensions.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun extensions(vararg extensions: ContainerExtension) {
        checkOpen()
        this.extensions += extensions
    }

    /**
     * Adds [fallbacks], asked in order for keys the container does not declare.
     *
     * @param fallbacks the fallbacks.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun fallbacks(vararg fallbacks: ResolutionFallback) {
        checkOpen()
        this.fallbacks += fallbacks
    }

    /**
     * Applies [setup], a configuration shared by several containers.
     *
     * @param setup the setup.
     * @throws IllegalStateException if the builder was already evaluated.
     * @since 1.0.0
     */
    public fun include(setup: ContainerSetup) {
        checkOpen()
        setup.applyTo(this)
    }

    override fun assemble(): ContainerConfiguration = ContainerConfiguration(this)

    override fun toString(): String = "ContainerBuilder($name, $profile)"

    private fun <T> inheritedOr(default: T, read: ContainerConfiguration.() -> T): T {
        val origin = inherited ?: return default
        return origin.read()
    }

    /**
     * The default names of containers.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * The default name of a root container.
         *
         * @since 1.0.0
         */
        public val ROOT: ContainerName = ContainerName("root")

        /**
         * The default name of a child container.
         *
         * @since 1.0.0
         */
        public val CHILD: ContainerName = ContainerName("child")
    }
}
