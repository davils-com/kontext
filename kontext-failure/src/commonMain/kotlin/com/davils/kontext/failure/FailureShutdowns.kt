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

package com.davils.kontext.failure

import com.davils.knot.failure.FailureHandler
import com.davils.knot.shutdown.Shutdown
import com.davils.knot.shutdown.ShutdownRegistration
import com.davils.kontext.core.Container
import com.davils.kontext.core.Qualifier
import com.davils.kore.core.annotation.DavilsInternal
import kotlin.time.Duration
import com.davils.knot.failure.failures as handleFailures

private const val TASK_PREFIX: String = "failures:"

/**
 * Registers the tasks of the [FailureHandler] that [container] declares with [qualifier] in this shutdown, as Knot
 * declares them: `<name>-drain` lets the handler write its last reports in the drain phase for at most
 * [drainTimeout], before the work it reports on stops, and `<name>-close` closes it in the release phase.
 *
 * Together with `Shutdown.adopt` of `kontext-shutdown`, which closes the container in its phase, the reports of an
 * orderly shutdown are written before the container closes. Once the container is closed in any other way, the tasks
 * are unregistered, so that a closed container never holds a shutdown back.
 *
 * @param container the container that declares the handler; resolving the handler creates it.
 * @param qualifier the qualifier of the handler, `null` for none.
 * @param drainTimeout how long the handler may write in the drain phase, [DEFAULT_FAILURE_DRAIN_TIMEOUT] by default.
 * @param name the prefix of the names of the two tasks, `failures:` and the name of the container by default.
 * @return the registration, refused if the shutdown already runs or holds a task of that name.
 * @throws com.davils.kontext.core.error.ResolutionException if the container declares no such handler or it fails to
 *   be created.
 * @since 1.3.0
 */
public fun Shutdown.adoptFailures(
    container: Container,
    qualifier: Qualifier? = null,
    drainTimeout: Duration = DEFAULT_FAILURE_DRAIN_TIMEOUT,
    name: String = "$TASK_PREFIX${container.name}",
): ShutdownRegistration {
    val handler = container.get<FailureHandler>(qualifier)
    val registration = register { handleFailures(handler = handler, drainTimeout = drainTimeout, name = name) }
    if (registration is ShutdownRegistration.Registered) container.onClosed { registration.unregister() }
    return registration
}
