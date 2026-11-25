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

package com.davils.kontext.core.java

import com.davils.kontext.core.Container
import com.davils.kontext.core.Module
import com.davils.kontext.core.Profile
import com.davils.kontext.core.container
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerBuilder
import kotlinx.coroutines.runBlocking
import java.util.function.Consumer
import com.davils.kontext.core.module as moduleOf

/**
 * Builds modules and starts and closes containers from Java, which cannot call suspending functions.
 *
 * @since 1.0.0
 */
public object JavaModules {
    /**
     * Builds a module named [name] from the definitions [declare] adds.
     *
     * @param name the name of the module.
     * @param declare declares the definitions.
     * @return the module.
     * @throws IllegalArgumentException if [name] is not a valid name.
     * @since 1.0.0
     */
    @JvmStatic
    public fun module(name: String, declare: Consumer<JavaModuleBuilder>): Module =
        moduleOf(name) { declare.accept(JavaModuleBuilder(this)) }

    /**
     * Starts a container in [profile], blocking the calling thread until it runs.
     *
     * @param profile the profile.
     * @param configure configures the container.
     * @return the running container.
     * @throws com.davils.kore.core.validation.report.ValidationException if the configuration is refused.
     * @since 1.0.0
     */
    @JvmStatic
    public fun start(profile: Profile, configure: Consumer<ContainerBuilder>): Container =
        runBlocking { container(profile) { configure.accept(this) } }

    /**
     * Closes [container], blocking the calling thread until it is closed.
     *
     * @param container the container.
     * @return the report.
     * @since 1.0.0
     */
    @JvmStatic
    public fun close(container: Container): CloseReport = runBlocking { container.close() }
}
