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

package com.davils.kontext.core.internal

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.definition.Declaration
import com.davils.kore.core.failure.attempted
import com.davils.kore.core.failure.contained
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.TimeSource

internal abstract class Host(private val guard: Guard) {
    private val instances = HashMap<Declaration, Held>()
    private val pending = HashMap<Declaration, Deferred<Any>>()
    private val claims = Claims(guard)
    private var isSealed = false

    val disposals: Disposals = Disposals(guard)

    abstract val scope: ScopeRuntime?

    abstract val coroutineScope: CoroutineScope

    abstract val timeSource: TimeSource.WithComparableMarks

    abstract val registry: Registry

    abstract fun seed(key: DefinitionKey): Any?

    abstract fun disposalFailed(key: DefinitionKey)

    fun existing(declaration: Declaration): Any? = guard.locked { valid(declaration) }

    fun memoized(declaration: Declaration, create: () -> Any): Memo {
        val worker = currentWorker()
        val claim = guard.locked { claim(declaration, worker) }
        if (claim != null) return claim
        try {
            val instance = create()
            return guard.locked { kept(declaration, instance) }
        } finally {
            guard.locked { claims.release(declaration) }
        }
    }

    suspend fun memoizedAsync(declaration: Declaration, create: suspend () -> Any): Any? {
        val creation = guard.locked { creationOf(declaration, create) } ?: return null
        creation.start()
        creation.join()
        val isAbandoned = creation.isCancelled && !coroutineScope.isActive
        return creation.takeUnless { isAbandoned }?.await()
    }

    fun retain(declaration: Declaration, instance: Any): Boolean {
        val dispose = declaration.dispose
        val isAdmitted = guard.locked {
            val isHeld = admits(declaration)
            if (isHeld && dispose != null) disposals.add(declaration, instance) { dispose(instance) }
            isHeld
        }
        if (!isAdmitted && dispose != null) disposeLater(declaration) { dispose(instance) }
        return isAdmitted
    }

    fun seal() {
        guard.locked {
            isSealed = true
            instances.clear()
            guard.signalAll()
        }
    }

    fun forget(declarations: Set<Declaration>) {
        guard.locked {
            instances.keys.removeAll(declarations)
            pending.keys.removeAll(declarations)
        }
    }

    fun <T> locked(block: () -> T): T = guard.locked(block)

    private tailrec fun claim(declaration: Declaration, worker: Any): Memo? {
        val owner = claims.ownerOf(declaration)
        val isWaiting = owner != null &&
            admits(declaration) &&
            declaration !in instances &&
            claims.isBlocking(owner, worker)
        if (!isWaiting) return settle(declaration = declaration, worker = worker, owner = owner)
        claims.waitFor(declaration, worker)
        return claim(declaration, worker)
    }

    private fun settle(declaration: Declaration, worker: Any, owner: Any?): Memo? {
        val isAdmitted = admits(declaration)
        val known = valid(declaration)?.let(Memo::Ready)
        val settled = Memo.Refused.takeUnless { isAdmitted } ?: known ?: Memo.Reentered.takeIf { owner != null }
        if (settled == null) claims.claim(declaration, worker)
        return settled
    }

    private fun kept(declaration: Declaration, instance: Any): Memo {
        if (!admits(declaration)) return Memo.Refused
        store(declaration, instance)
        return Memo.Ready(instance)
    }

    private fun admits(declaration: Declaration): Boolean = !isSealed && registry.holds(declaration)

    private fun creationOf(declaration: Declaration, create: suspend () -> Any): Deferred<Any>? {
        if (!admits(declaration)) return null
        return valid(declaration)?.let { known -> CompletableDeferred(known) } ?: pendingOf(declaration, create)
    }

    private fun pendingOf(declaration: Declaration, create: suspend () -> Any): Deferred<Any> = pending.getOrPut(
        declaration,
    ) {
        val creation = coroutineScope.async(start = CoroutineStart.LAZY) {
            val instance = create()
            guard.locked { storeIfAdmitted(declaration, instance) }
        }
        creation.invokeOnCompletion { guard.locked { forgetPending(declaration, creation) } }
        creation
    }

    private fun forgetPending(declaration: Declaration, creation: Deferred<Any>) {
        if (pending[declaration] === creation) pending.remove(declaration)
    }

    private fun storeIfAdmitted(declaration: Declaration, instance: Any): Any {
        if (admits(declaration)) store(declaration, instance)
        return instance
    }

    private fun store(declaration: Declaration, instance: Any) {
        instances[declaration] = Held(instance, timeSource.markNow())
    }

    private fun valid(declaration: Declaration): Any? {
        val held = instances[declaration] ?: return null
        return held.instance.takeIf { keeps(declaration, held) }
    }

    private fun keeps(declaration: Declaration, held: Held): Boolean {
        val isKept = contained { declaration.retention.keeps(held.created.elapsedNow()) }.getOrDefault(false)
        if (isKept) return true
        instances.remove(declaration)
        expire(held)
        return false
    }

    private fun expire(held: Held) {
        val disposal = disposals.take(held.instance) ?: return
        detached { disposal.run()?.let { failure -> disposalFailed(failure.key) } }
    }

    private fun disposeLater(declaration: Declaration, action: suspend () -> Unit) {
        detached {
            val outcome = attempted { action() }
            if (outcome.isFailure) disposalFailed(declaration.key)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun detached(block: suspend () -> Unit) {
        coroutineScope.launch(start = CoroutineStart.ATOMIC) { withContext(NonCancellable) { block() } }
    }
}
