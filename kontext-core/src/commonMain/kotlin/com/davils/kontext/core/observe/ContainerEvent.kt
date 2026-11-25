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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.core.observe

import com.davils.konfetti.core.Event
import com.davils.konfetti.core.TopicKey
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.ModuleName
import com.davils.kontext.core.Profile
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList
import com.davils.kore.core.identity.UuidV7
import com.davils.kore.core.text.SafeText
import kotlin.time.Duration

/**
 * What a container tells its [ContainerSink] and the topic [TOPIC] of its event bus.
 *
 * @property container the name of the container.
 * @since 1.0.0
 */
public sealed class ContainerEvent(public val container: ContainerName) : Event {
    /**
     * The container started.
     *
     * @property profile the profile of the container.
     * @property definitions the definitions it holds.
     * @property elapsed how long the start took, eager singles included.
     * @since 1.0.0
     */
    public class Started internal constructor(
        container: ContainerName,
        public val profile: Profile,
        public val definitions: Int,
        public val elapsed: Duration,
    ) : ContainerEvent(container) {
        override fun toString(): String = "Started($container, $profile, definitions=$definitions)"
    }

    /**
     * The container opened a child container.
     *
     * @property child the name of the child.
     * @since 1.0.0
     */
    public class ChildOpened internal constructor(container: ContainerName, public val child: ContainerName) :
        ContainerEvent(container) {
        override fun toString(): String = "ChildOpened($container, $child)"
    }

    /**
     * A scope was opened.
     *
     * @property scope the key of the scope.
     * @property id the id of the opened scope.
     * @since 1.0.0
     */
    public class ScopeOpened internal constructor(
        container: ContainerName,
        public val scope: ScopeKey,
        public val id: UuidV7,
    ) : ContainerEvent(container) {
        override fun toString(): String = "ScopeOpened($container, $scope, $id)"
    }

    /**
     * A scope was closed.
     *
     * @property scope the key of the scope.
     * @property id the id of the closed scope.
     * @property failures the instances that failed to close.
     * @since 1.0.0
     */
    public class ScopeClosed internal constructor(
        container: ContainerName,
        public val scope: ScopeKey,
        public val id: UuidV7,
        public val failures: Int,
    ) : ContainerEvent(container) {
        override fun toString(): String = "ScopeClosed($container, $scope, $id, failures=$failures)"
    }

    /**
     * An instance was created.
     *
     * @property key the key of its definition.
     * @property lifetime the lifetime of its definition.
     * @property elapsed how long its factory ran, the instances it resolved included.
     * @since 1.0.0
     */
    public class InstanceCreated internal constructor(
        container: ContainerName,
        public val key: DefinitionKey,
        public val lifetime: Lifetime,
        public val elapsed: Duration,
    ) : ContainerEvent(container) {
        override fun toString(): String = "InstanceCreated($container, $key, $lifetime)"
    }

    /**
     * A resolution failed.
     *
     * @property failure why it failed.
     * @since 1.0.0
     */
    public class ResolutionFailed internal constructor(
        container: ContainerName,
        public val failure: ResolutionFailure,
    ) :
        ContainerEvent(container) {
        override fun toString(): String = "ResolutionFailed($container, $failure)"
    }

    /**
     * The container closed.
     *
     * @property failures the instances that failed to close.
     * @property elapsed how long the close took.
     * @since 1.0.0
     */
    public class Closed internal constructor(
        container: ContainerName,
        public val failures: Int,
        public val elapsed: Duration,
    ) : ContainerEvent(container) {
        override fun toString(): String = "Closed($container, failures=$failures)"
    }

    /**
     * Modules were loaded into the running container.
     *
     * @since 1.0.0
     */
    public class ModulesLoaded internal constructor(container: ContainerName, modules: List<ModuleName>) :
        ContainerEvent(container) {
        /**
         * The names of the loaded modules.
         *
         * @since 1.0.0
         */
        public val modules: List<ModuleName> = modules.immutableList()

        override fun toString(): String = "ModulesLoaded($container, $modules)"
    }

    /**
     * Modules were unloaded from the container.
     *
     * @since 1.0.0
     */
    public class ModulesUnloaded internal constructor(container: ContainerName, modules: List<ModuleName>) :
        ContainerEvent(container) {
        /**
         * The names of the unloaded modules.
         *
         * @since 1.0.0
         */
        public val modules: List<ModuleName> = modules.immutableList()

        override fun toString(): String = "ModulesUnloaded($container, $modules)"
    }

    /**
     * A hook of an extension threw.
     *
     * @property extension the name of the extension.
     * @property phase the hook, such as `started` or `closing`.
     * @since 1.0.0
     */
    public class ExtensionFailed internal constructor(
        container: ContainerName,
        public val extension: String,
        public val phase: String,
    ) : ContainerEvent(container) {
        override fun toString(): String = "ExtensionFailed($container, ${SafeText.quoted(extension)}, $phase)"
    }

    /**
     * The close function of an instance threw outside of a close of the container, such as for an expired instance.
     *
     * @property key the key of the definition of the instance.
     * @since 1.0.0
     */
    public class DisposalFailed internal constructor(container: ContainerName, public val key: DefinitionKey) :
        ContainerEvent(container) {
        override fun toString(): String = "DisposalFailed($container, $key)"
    }

    /**
     * The topic of container events.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * The topic a container publishes its events on, when it is given an event bus.
         *
         * @since 1.0.0
         */
        public val TOPIC: TopicKey<ContainerEvent> = TopicKey("kontext.container")
    }
}
