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

package com.davils.kontext.failure

import com.davils.knot.failure.FailureHandler
import com.davils.knot.failure.FailureHandlerBuilder
import com.davils.knot.shutdown.Shutdown
import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.definition.Definition
import com.davils.kontext.core.definition.ModuleBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.davils.knot.failure.failures as handleFailures

/**
 * How long a [FailureHandler] declared by [failures] may write its last reports by default, when the container
 * closes and in the drain phase of a shutdown: 5 seconds.
 *
 * @since 1.3.0
 */
public val DEFAULT_FAILURE_DRAIN_TIMEOUT: Duration = 5.seconds

/**
 * Declares the [FailureHandler] of Knot as a single, such as `failures { sinks { sink("disk", diskSink) } }`, which
 * the container closes when it closes.
 *
 * The handler reports to the tracer, the meters and the event bus of the container, and a fatal failure initiates the
 * [Shutdown] the container declares without a qualifier, if it declares one; [configure] runs last and may change
 * every one of them. The handler writes its reports in a scope of its own, on the coroutine context of the container
 * but outside its job, which is cancelled before the instances of the container are closed: closing the container lets
 * the handler write the reports it took in for at most [drainTimeout] and closes it then, which cancels what is still
 * being written. [Shutdown.adoptFailures] lets the handler write them in the drain phase of a shutdown instead. On the
 * JVM, `installUncaughtHandler` of Knot hands it the exceptions no thread handled.
 *
 * @receiver the module that declares the handler.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @param drainTimeout how long closing the container waits for the reports, [DEFAULT_FAILURE_DRAIN_TIMEOUT] by
 *   default.
 * @param configure declares the handler, such as its classification, its storm protection and its sinks.
 * @return the definition, to bind it to further types.
 * @since 1.3.0
 */
@Suppress("MissingUseCall")
public fun ModuleBuilder.failures(
    qualifier: Qualifier? = null,
    drainTimeout: Duration = DEFAULT_FAILURE_DRAIN_TIMEOUT,
    configure: FailureHandlerBuilder.() -> Unit = {},
): Definition<FailureHandler> = single(qualifier = qualifier, close = { handler -> drained(handler, drainTimeout) }) {
    handlerOf(context = this, configure = configure)
}

@Suppress("MissingUseCall")
private fun handlerOf(context: DefinitionContext, configure: FailureHandlerBuilder.() -> Unit): FailureHandler {
    val surroundings = context.resilience()
    val target = context.getOrNull<Shutdown>()
    val coroutineContext = context.coroutineScope.coroutineContext
    return handleFailures(CoroutineScope(coroutineContext.minusKey(Job) + SupervisorJob())) {
        tracer = surroundings.tracer
        meters = surroundings.meters
        events = surroundings.events
        shutdown = target
        configure()
    }
}

private suspend fun drained(handler: FailureHandler, timeout: Duration) {
    withTimeoutOrNull(timeout) { handler.drain() }
    handler.close()
}
