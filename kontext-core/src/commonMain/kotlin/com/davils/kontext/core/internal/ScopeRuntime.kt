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

import com.davils.kontext.core.ContainerLimits
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.ScopeId
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.DisposalFailure
import com.davils.kontext.core.error.ScopeSeedException
import com.davils.kontext.core.parameter.Parameters
import com.davils.kore.core.failure.contained
import com.davils.kore.core.identity.UuidV7
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.TimeSource

internal class ScopeRuntime(
    val runtime: ContainerRuntime,
    val key: ScopeKey,
    val parent: ScopeRuntime?,
    val id: UuidV7,
    val scopeId: ScopeId?,
    seeds: Map<DefinitionKey, Any>,
) : Host(platformGuard()) {
    private val open = AtomicBoolean(true)
    private val nested = mutableListOf<ScopeRuntime>()
    private val links = mutableListOf<ScopeRuntime>()
    private val linkedBy = mutableListOf<ScopeRuntime>()
    private val provided = seeds.toMutableMap()
    private val closeHandlers = mutableListOf<() -> Unit>()
    private val closure = CompletableDeferred<Unit>()
    val job: CompletableJob = SupervisorJob(parentJob(parent) ?: runtime.job)

    override val scope: ScopeRuntime get() = this

    override val coroutineScope: CoroutineScope = CoroutineScope(runtime.coroutineScope.coroutineContext + job)

    override val timeSource: TimeSource.WithComparableMarks get() = runtime.configuration.timeSource

    override val registry: Registry get() = runtime.registry

    val isOpen: Boolean get() = open.load()

    val context: ResolutionContext =
        ResolutionContext(runtime = runtime, scope = this, trail = Trail.ROOT, parameters = Parameters.EMPTY)

    val chain: List<ScopeRuntime> get() = generateSequence(this) { scope -> scope.parent }.toList()

    override fun seed(key: DefinitionKey): Any? = locked { provided[key] }

    override fun disposalFailed(key: DefinitionKey) {
        runtime.disposalFailed(key)
    }

    fun searchOrder(): List<ScopeRuntime> {
        val visited = LinkedHashSet<ScopeRuntime>()
        collectInto(visited)
        return visited.toList()
    }

    fun openScope(key: ScopeKey, scopeId: ScopeId?, seeds: Map<DefinitionKey, Any>): ScopeRuntime =
        runtime.scopes.open(key = key, parent = this, seeds = seeds, scopeId = scopeId)

    fun adopt(child: ScopeRuntime): Boolean = locked {
        if (isOpen) nested += child
        isOpen
    }

    fun declare(key: DefinitionKey, instance: Any) {
        val isAccepted = runtime.registry.isProvided(this.key, key) && locked { provide(key, instance) }
        if (!isAccepted) throw ScopeSeedException(this.key, key)
    }

    private fun provide(key: DefinitionKey, instance: Any): Boolean {
        if (key in provided) return false
        provided[key] = instance
        return true
    }

    fun link(other: ScopeRuntime) {
        require(other.runtime === runtime && other !== this) { "A scope links only to another scope of its container" }
        check(other.isOpen) { "A scope links only to an open scope" }
        val isLinked = locked {
            check(isOpen) { "A closed scope links to no scope" }
            require(links.size < ContainerLimits.MAX_LINKS) { "A scope links to at most ${ContainerLimits.MAX_LINKS}" }
            other !in links && links.add(other)
        }
        if (isLinked) other.locked { other.linkedBy += this }
    }

    fun unlink(other: ScopeRuntime): Boolean {
        val isUnlinked = locked { links.remove(other) }
        other.locked { other.linkedBy.remove(this) }
        return isUnlinked
    }

    fun onClosed(handler: () -> Unit) {
        val isAlreadyClosed = locked {
            val isClosed = !isOpen
            if (!isClosed) closeHandlers += handler
            isClosed
        }
        if (isAlreadyClosed) handler()
    }

    suspend fun close(): CloseReport {
        if (!open.compareAndSet(expectedValue = true, newValue = false)) return awaitClosed()
        return withContext(Closing.contextOf(this)) { CloseReport.Closed(dispose()) }
    }

    private suspend fun awaitClosed(): CloseReport {
        if (!Closing.isWithin(this)) closure.await()
        return CloseReport.AlreadyClosed
    }

    private suspend fun dispose(): List<DisposalFailure> {
        var failures = emptyList<DisposalFailure>()
        try {
            runtime.scopes.closing(this)
            val children = locked { nested.toList().also { nested.clear() } }
            val childFailures = children.asReversed().flatMap { child -> child.close().failures }
            seal()
            job.cancel()
            failures = childFailures + disposals.drain()
        } finally {
            detach()
            val handlers = locked { closeHandlers.toList().also { closeHandlers.clear() } }
            handlers.forEach { handler -> contained { handler() } }
            runtime.scopes.closed(this, failures.size)
            closure.complete(Unit)
        }
        return failures
    }

    private fun detach() {
        parent?.locked { parent.nested.remove(this) }
        val linking = locked { linkedBy.toList().also { linkedBy.clear() } }
        linking.forEach { scope -> scope.unlink(this) }
        val linked = locked { links.toList() }
        linked.forEach { scope -> unlink(scope) }
    }

    private fun collectInto(visited: MutableSet<ScopeRuntime>) {
        val pending = ArrayDeque<ScopeRuntime>()
        pending.addLast(this)
        while (pending.isNotEmpty()) {
            val next = pending.removeLast()
            val fresh = next.chain.filter { scope -> visited.add(scope) }
            val linked = fresh.flatMap { scope -> scope.locked { scope.links.toList() } }
            linked.asReversed().forEach { link -> pending.addLast(link) }
        }
    }
}

private fun parentJob(parent: ScopeRuntime?): CompletableJob? = parent?.job
