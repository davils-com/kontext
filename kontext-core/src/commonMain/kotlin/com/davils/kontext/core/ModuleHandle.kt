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
import com.davils.kontext.core.internal.ContainerRuntime
import com.davils.kontext.core.internal.Installation

/**
 * Modules loaded into a running container by [Container.load] or [Container.declare], to unload them again.
 *
 * @since 1.0.0
 */
public class ModuleHandle internal constructor(
    private val runtime: ContainerRuntime,
    private val installation: Installation,
) {
    /**
     * The loaded modules, the included ones each once.
     *
     * @since 1.0.0
     */
    public val modules: List<Module> get() = installation.modules

    /**
     * Removes the definitions of the modules from the container, closes the scopes only they declared and closes
     * the instances created from them, in the reverse order of creation. Resolutions that already found a
     * definition finish; later ones no longer find it.
     *
     * @return the report, [CloseReport.AlreadyClosed] if the modules were unloaded before.
     * @since 1.0.0
     */
    public suspend fun unload(): CloseReport = runtime.loader.unload(installation)

    override fun toString(): String = "ModuleHandle(${modules.map { module -> module.name }})"
}
