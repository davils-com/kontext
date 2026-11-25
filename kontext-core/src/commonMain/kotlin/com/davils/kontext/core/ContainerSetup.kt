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

import com.davils.kontext.core.container.ContainerBuilder

/**
 * A configuration shared by several containers, such as the modules and sinks of every service of an application,
 * created by [containerSetup] and applied with [ContainerBuilder.include].
 *
 * @param block configures a container.
 * @since 1.0.0
 */
public class ContainerSetup internal constructor(private val block: ContainerBuilder.() -> Unit) {
    internal fun applyTo(builder: ContainerBuilder) {
        builder.block()
    }

    override fun toString(): String = "ContainerSetup"
}

/**
 * Creates a configuration shared by several containers.
 *
 * @param block configures a container; it runs once for every container that includes it.
 * @return the setup.
 * @since 1.0.0
 */
public fun containerSetup(block: ContainerBuilder.() -> Unit): ContainerSetup = ContainerSetup(block)
