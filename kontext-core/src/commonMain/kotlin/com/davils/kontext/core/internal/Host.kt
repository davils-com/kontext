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
import com.davils.kore.core.failure.contained
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

internal abstract class Host(private val guard: Guard) {
    private val instances = HashMap<Declaration, Held>()
    private val pending = HashMap<Declaration, Deferred<Any>>()

    val disposals: Disposals = Disposals(guard)

    abstract val scope: ScopeRuntime?

    abstract val coroutineScope: CoroutineScope

    abstract val timeSource: TimeSource.WithComparableMarks

    abstract fun seed(key: DefinitionKey): Any?

    abstract fun disposalFailed(key: DefinitionKey)

    fun existing(declaration: Declaration): Any? = guard.locked { valid(declaration) }

    fun memoized(declaration: Declaration, create: () -> Any): Any = guard.locked {
        valid(declaration) ?: store(declaration, create())
    }

    suspend fun memoizedAsync(declaration: Declaration, create: suspend () -> Any): Any? {
        val creation = guard.locked { creationOf(declaration, create) }
        creation.join()
        if (creation.isCancelled && !coroutineScope.isActive) return null
        return creation.await()
    }

    fun forget(declarations: Set<Declaration>) {
        guard.locked { instances.keys.removeAll(declarations) }
    }

    fun <T> locked(block: () -> T): T = guard.locked(block)

    private fun creationOf(declaration: Declaration, create: suspend () -> Any): Deferred<Any> {
        val known = valid(declaration) ?: return pendingOf(declaration, create)
        return CompletableDeferred(known)
    }

    private fun pendingOf(declaration: Declaration, create: suspend () -> Any): Deferred<Any> = pending.getOrPut(
        declaration,
    ) {
        val creation = coroutineScope.async {
            val instance = create()
            guard.locked { store(declaration, instance) }
        }
        creation.invokeOnCompletion { guard.locked { pending.remove(declaration) } }
        creation
    }

    private fun store(declaration: Declaration, instance: Any): Any {
        instances[declaration] = Held(instance, timeSource.markNow())
        return instance
    }

    private fun valid(declaration: Declaration): Any? {
        val held = instances[declaration] ?: return null
        return held.instance.takeIf { keeps(declaration, held) }
    }

    private fun keeps(declaration: Declaration, held: Held): Boolean {
        val isKept = contained { declaration.retention.keeps(held.created.elapsedNow()) }.getOrDefault(true)
        if (isKept) return true
        instances.remove(declaration)
        expire(held)
        return false
    }

    private fun expire(held: Held) {
        val disposal = disposals.take(held.instance) ?: return
        coroutineScope.launch { disposal.run()?.let { failure -> disposalFailed(failure.key) } }
    }
}
