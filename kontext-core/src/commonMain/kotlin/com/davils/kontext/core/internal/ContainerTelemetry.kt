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

package com.davils.kontext.core.internal

import com.davils.kollect.core.trace.Span
import com.davils.kollect.core.trace.SpanOutcome
import com.davils.konfetti.core.EventBus
import com.davils.konfetti.core.Topic
import com.davils.konfetti.core.publish.PublishResult
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.ModuleName
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.container.ContainerConfiguration
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.core.observe.ContainerSink
import com.davils.kore.core.failure.contained
import com.davils.kore.core.identity.UuidV7
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

private const val EVENT_CAPACITY: Int = 64
private const val CONTAINER_ATTRIBUTE: String = "kontext.container"
private const val PROFILE_ATTRIBUTE: String = "kontext.profile"

internal class ContainerTelemetry(val configuration: ContainerConfiguration) {
    val container: ContainerName = configuration.name
    private val sink: ContainerSink? = configuration.sink
    private val topic: Topic<ContainerEvent>? = configuration.events?.let(::topicOf)
    private val meters: ContainerMeters? = configuration.meters?.let { registry ->
        ContainerMeters(registry, container.value)
    }
    private val rejections = AtomicLong(0L)
    private val reentry: Reentry = platformReentry()

    private val local: MutableSharedFlow<ContainerEvent> = MutableSharedFlow(
        extraBufferCapacity = EVENT_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val rejected: Long get() = rejections.load()

    val events: SharedFlow<ContainerEvent> = local.asSharedFlow()

    fun modulesLoaded(modules: List<ModuleName>) {
        emit { ContainerEvent.ModulesLoaded(container, modules) }
    }

    fun modulesUnloaded(modules: List<ModuleName>) {
        emit { ContainerEvent.ModulesUnloaded(container, modules) }
    }

    fun extensionFailed(extension: String, phase: String) {
        emit { ContainerEvent.ExtensionFailed(container = container, extension = extension, phase = phase) }
    }

    fun disposalFailed(key: DefinitionKey) {
        emit { ContainerEvent.DisposalFailed(container, key) }
    }

    fun started(definitions: Int, elapsed: Duration) {
        meters?.state(ContainerState.Running)
        emit {
            ContainerEvent.Started(
                container = container,
                profile = configuration.profile,
                definitions = definitions,
                elapsed = elapsed,
            )
        }
    }

    fun closing() {
        meters?.state(ContainerState.Closing)
    }

    fun closed(failures: Int, elapsed: Duration) {
        meters?.state(ContainerState.Closed)
        emit { ContainerEvent.Closed(container = container, failures = failures, elapsed = elapsed) }
    }

    fun childOpened(child: ContainerName, open: Int) {
        meters?.children(open)
        emit { ContainerEvent.ChildOpened(container, child) }
    }

    fun childClosed(open: Int) {
        meters?.children(open)
    }

    fun scopeOpened(scope: ScopeKey, id: UuidV7, open: Int) {
        meters?.scopes(open)
        emit { ContainerEvent.ScopeOpened(container = container, scope = scope, id = id) }
    }

    fun scopeClosed(scope: ScopeKey, id: UuidV7, failures: Int, open: Int) {
        meters?.scopes(open)
        emit { ContainerEvent.ScopeClosed(container = container, scope = scope, id = id, failures = failures) }
    }

    fun created(key: DefinitionKey, lifetime: Lifetime, elapsed: Duration) {
        meters?.created(lifetime, elapsed)
        emit {
            ContainerEvent.InstanceCreated(
                container = container,
                key = key,
                lifetime = lifetime,
                elapsed = elapsed,
            )
        }
    }

    fun failed(failure: ResolutionFailure) {
        meters?.failed(failure.reason)
        emit { ContainerEvent.ResolutionFailed(container, failure) }
    }

    inline fun <T> traced(operation: String, block: () -> T): T {
        val span = spanOf(operation)
        val result = runCatching(block)
        span?.let { started -> ended(started, SpanOutcome.Ok.takeIf { result.isSuccess } ?: SpanOutcome.Error) }
        return result.getOrThrow()
    }

    fun spanOf(operation: String): Span? {
        val started = contained {
            val span = configuration.tracer.start(operation)
            span.attribute(key = CONTAINER_ATTRIBUTE, value = container.value)
            span.attribute(key = PROFILE_ATTRIBUTE, value = configuration.profile.value)
            span
        }
        return started.getOrNull()
    }

    fun ended(span: Span, outcome: SpanOutcome) {
        contained { span.end(outcome) }
    }

    private inline fun emit(event: () -> ContainerEvent) {
        val listened = topic?.takeIf { target -> target.hasConsumers }
        val isObserved = local.subscriptionCount.value > 0
        if (sink == null && listened == null && !isObserved) return
        val built = event()
        local.tryEmit(built)
        sink?.let { target -> deliver(target, built) }
        listened?.let { target -> publish(target, built) }
    }

    private fun deliver(target: ContainerSink, event: ContainerEvent) {
        val delivered = reentry.outermost { contained { target.record(event) } }
        val isDelivered = delivered?.isSuccess == true
        if (!isDelivered) reject()
    }

    private fun publish(target: Topic<ContainerEvent>, event: ContainerEvent) {
        val result = target.offer(event)
        if (result is PublishResult.Published) return
        reject()
    }

    private fun reject() {
        rejections.addAndFetch(1L)
        meters?.rejected()
    }

    private companion object {
        fun topicOf(bus: EventBus): Topic<ContainerEvent> {
            if (ContainerEvent.TOPIC !in bus) bus.declare(ContainerEvent.TOPIC)
            return bus[ContainerEvent.TOPIC]
        }
    }
}
