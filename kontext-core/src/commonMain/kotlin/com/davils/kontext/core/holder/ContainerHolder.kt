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

@file:OptIn(ExperimentalAtomicApi::class)

package com.davils.kontext.core.holder

import com.davils.kontext.core.Container
import com.davils.kontext.core.Module
import com.davils.kontext.core.ModuleHandle
import com.davils.kontext.core.Profile
import com.davils.kontext.core.container
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.error.ContainerAlreadyStartedException
import com.davils.kontext.core.error.ContainerNotStartedException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private const val NONE: String = "none"

/**
 * Holds the one running container of an application, from its start to its stop, for code that reaches the
 * container through its holder instead of being handed it.
 *
 * A holder is thread-safe; starting and stopping are serialised. The application creates its holder and passes it
 * on, so that two applications, or two tests, never share one.
 *
 * @since 1.0.0
 */
public open class ContainerHolder {
    private val held = AtomicReference<Container?>(null)
    private val lifecycle = Mutex()

    /**
     * The running container.
     *
     * @throws ContainerNotStartedException if the holder holds none.
     * @since 1.0.0
     */
    public val current: Container get() = held.load() ?: throw ContainerNotStartedException()

    /**
     * The running container, `null` if the holder holds none.
     *
     * @since 1.0.0
     */
    public val currentOrNull: Container? get() = held.load()

    /**
     * Starts a container in [profile] and holds it.
     *
     * @param profile the profile.
     * @param block configures the container.
     * @return the running container.
     * @throws ContainerAlreadyStartedException if the holder holds a container already.
     * @throws com.davils.kore.core.validation.report.ValidationException if the configuration is refused.
     * @throws com.davils.kontext.core.error.ResolutionException if an eager single fails.
     * @since 1.0.0
     */
    public suspend fun start(profile: Profile = Profile.DEFAULT, block: ContainerBuilder.() -> Unit): Container =
        lifecycle.withLock {
            val existing = held.load()
            if (existing != null) throw ContainerAlreadyStartedException(existing.name)
            val started = container(profile, block)
            held.store(started)
            started
        }

    /**
     * Closes the held container and releases it.
     *
     * @return the report, [CloseReport.AlreadyClosed] if the holder held none.
     * @since 1.0.0
     */
    public suspend fun stop(): CloseReport = lifecycle.withLock { stopHeld() }

    /**
     * Loads [modules] into the held container, see [Container.load].
     *
     * @param modules the modules.
     * @param eager whether every single of the modules is created at once.
     * @return the handle to unload them.
     * @throws ContainerNotStartedException if the holder holds none.
     * @since 1.0.0
     */
    public suspend fun load(vararg modules: Module, eager: Boolean = false): ModuleHandle =
        current.load(modules = modules, eager = eager)

    private suspend fun stopHeld(): CloseReport {
        val existing = held.exchange(null) ?: return CloseReport.AlreadyClosed
        return existing.close()
    }

    override fun toString(): String = "ContainerHolder(${currentOrNull?.name ?: NONE})"
}
