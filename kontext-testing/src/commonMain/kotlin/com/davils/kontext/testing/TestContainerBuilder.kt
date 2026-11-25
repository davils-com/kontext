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

package com.davils.kontext.testing

import com.davils.kontext.core.Module
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.module
import com.davils.kore.core.annotation.DavilsDsl

/**
 * Configures a test container, see [testContainer].
 *
 * @since 1.0.0
 */
@DavilsDsl
public class TestContainerBuilder internal constructor() {
    internal val declared: MutableList<Module> = mutableListOf()
    internal val replaced: MutableList<Module> = mutableListOf()
    internal val configurations: MutableList<ContainerBuilder.() -> Unit> = mutableListOf()

    /**
     * Adds [modules] to the container.
     *
     * @param modules the modules under test.
     * @since 1.0.0
     */
    public fun modules(vararg modules: Module) {
        declared += modules
    }

    /**
     * Replaces the definitions of the added modules that [modules] declare as well, such as a client by a fake.
     * A replacement of a definition no module declares is refused.
     *
     * @param modules the replacing modules.
     * @since 1.0.0
     */
    public fun replace(vararg modules: Module) {
        replaced += modules
    }

    /**
     * Declares [instance] as a single of [T] with [qualifier] in a module of its own, such as a fake that no module
     * of the application declares.
     *
     * @param T the type the instance is found as.
     * @param instance the instance.
     * @param qualifier the qualifier, `null` for none.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> declare(instance: T, qualifier: Qualifier? = null) {
        add(module(nextName()) { single(qualifier = qualifier) { instance } })
    }

    @PublishedApi
    internal fun nextName(): String = "test.declared.d${declared.size}"

    @PublishedApi
    internal fun add(module: Module) {
        declared += module
    }

    /**
     * Configures the container further, such as its sink or its name.
     *
     * @param block the configuration.
     * @since 1.0.0
     */
    public fun configure(block: ContainerBuilder.() -> Unit) {
        configurations += block
    }

    override fun toString(): String = "TestContainerBuilder(modules=${declared.size}, replaced=${replaced.size})"
}
