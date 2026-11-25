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

package com.davils.kontext.core.extension

import com.davils.kontext.core.Container
import com.davils.kontext.core.Module

/**
 * Hooks into the life of a container, such as a bridge to a framework or an audit of loaded modules; the caller
 * implements it and hands it to the builder of the container.
 *
 * Every hook is optional. A hook that throws is reported as a
 * [com.davils.kontext.core.observe.ContainerEvent.ExtensionFailed] event and never stops the container. Extensions
 * hear [onClosing] in the reverse order of their registration, before the container closes anything. A child
 * container inherits the extensions of its parent.
 *
 * @since 1.0.0
 */
public interface ContainerExtension {
    /**
     * A short name of the extension, shown in its failure events.
     *
     * @since 1.0.0
     */
    public val name: String

    /**
     * Called once the container runs.
     *
     * @param container the container.
     * @since 1.0.0
     */
    public fun onStarted(container: Container): Unit = Unit

    /**
     * Called after [modules] were loaded into the running container.
     *
     * @param container the container.
     * @param modules the loaded modules.
     * @since 1.0.0
     */
    public fun onModulesLoaded(container: Container, modules: List<Module>): Unit = Unit

    /**
     * Called after [modules] were unloaded from the container.
     *
     * @param container the container.
     * @param modules the unloaded modules.
     * @since 1.0.0
     */
    public fun onModulesUnloaded(container: Container, modules: List<Module>): Unit = Unit

    /**
     * Called when the container starts to close, before its children, scopes and instances close. Closing waits for
     * it without a time limit, so a hook that never returns keeps the container from closing; bound slow work with
     * a timeout of its own.
     *
     * @param container the container.
     * @since 1.0.0
     */
    public suspend fun onClosing(container: Container): Unit = Unit
}
