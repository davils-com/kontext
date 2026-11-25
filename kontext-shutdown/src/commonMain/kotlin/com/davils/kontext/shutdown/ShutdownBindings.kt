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

package com.davils.kontext.shutdown

import com.davils.knot.shutdown.Shutdown
import com.davils.knot.shutdown.ShutdownPhase
import com.davils.knot.shutdown.ShutdownRegistration
import com.davils.kontext.core.Container
import com.davils.kore.core.annotation.DavilsInternal

private const val TASK_PREFIX: String = "container:"

/**
 * Registers a task that closes [container] in [phase] of this shutdown, named `container:` and the name of the
 * container.
 *
 * The task fails with [ContainerCloseException] if instances fail to close. Once the container is closed in any
 * other way, the task is unregistered, so that a closed container never holds a shutdown back.
 *
 * @param container the container to close.
 * @param phase the phase to close it in.
 * @return the registration, refused if the shutdown already runs or holds a task of that name.
 * @since 1.0.0
 */
public fun Shutdown.adopt(container: Container, phase: ShutdownPhase = ShutdownPhase.Release): ShutdownRegistration {
    val registration = register {
        phase(phase) {
            task("$TASK_PREFIX${container.name}") {
                action { closeOrFail(container) }
            }
        }
    }
    if (registration is ShutdownRegistration.Registered) container.onClosed { registration.unregister() }
    return registration
}

private suspend fun closeOrFail(container: Container) {
    val report = container.close()
    if (report.isClean) return
    throw ContainerCloseException(container.name, report.failures)
}
