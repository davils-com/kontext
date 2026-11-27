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

package com.davils.kontext.kheck

import com.davils.kheck.core.Closure
import com.davils.kheck.core.Logging
import com.davils.kheck.core.LoggingBuilder
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.definition.Definition
import com.davils.kontext.core.definition.ModuleBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.davils.kheck.core.logging as startLogging

/**
 * How long closing the container waits for a [Logging] declared by [logging] by default: 10 seconds.
 *
 * @since 1.3.0
 */
public val DEFAULT_LOGGING_CLOSE_TIMEOUT: Duration = 10.seconds

/**
 * Declares a [Logging] of kheck as a single, such as `logging { console { } }`, which the container closes when it
 * closes.
 *
 * The logging measures on the time source of the container and publishes its events on the event bus of the
 * container, if it has one; [configure] runs last and may change both. Its coroutines run on [dispatcher] in a scope
 * of their own, not in the coroutine scope of the container, which is cancelled before the instances of the container
 * are closed: closing the container writes every waiting record, flushes and closes every sink within [closeTimeout],
 * and only then stops the logging. A logging that does not close in time drops the records still waiting, counts them
 * and fails its close, so the `CloseReport` of the container lists it. Bind its meters with `Logging.bindTo` where
 * the application wants them.
 *
 * @receiver the module that declares the logging.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @param dispatcher the dispatcher of the coroutines of the logging; one that may block briefly, because sinks write
 *   synchronously. [Dispatchers.Default] by default.
 * @param closeTimeout how long closing the container waits for the logging, [DEFAULT_LOGGING_CLOSE_TIMEOUT] by
 *   default; zero or negative drops what still waits at once.
 * @param configure configures the logging, such as its level and its sinks.
 * @return the definition, to bind it to further types.
 * @since 1.3.0
 */
public fun ModuleBuilder.logging(
    qualifier: Qualifier? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    closeTimeout: Duration = DEFAULT_LOGGING_CLOSE_TIMEOUT,
    configure: LoggingBuilder.() -> Unit = {},
): Definition<Logging> = single(qualifier = qualifier, close = { logging -> closeWithin(logging, closeTimeout) }) {
    val surroundings = resilience()
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    startLogging(scope = scope, dispatcher = dispatcher) {
        timeSource = surroundings.timeSource
        surroundings.events?.let { bus -> events(bus) }
        configure()
    }
}

private suspend fun closeWithin(logging: Logging, timeout: Duration) {
    val closure = logging.close(timeout)
    val lost = (closure as? Closure.TimedOut)?.pending ?: return
    error("The logging did not close within $timeout, $lost records were lost")
}
