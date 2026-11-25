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
import com.davils.kontext.core.ScopeId
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.error.ScopeAlreadyOpenException
import com.davils.kontext.core.error.ScopeLimitException
import com.davils.kontext.core.error.ScopeNestingException
import com.davils.kontext.core.error.ScopeNotDeclaredException
import com.davils.kontext.core.error.ScopeSeedException

internal class ScopeTable(private val runtime: ContainerRuntime) {
    private val open = mutableListOf<ScopeRuntime>()
    private val named = HashMap<ScopeId, ScopeRuntime>()

    fun open(key: ScopeKey, parent: ScopeRuntime?, seeds: Map<DefinitionKey, Any>, scopeId: ScopeId?): ScopeRuntime {
        ensureOpenable(key)
        ensurePlaced(key = key, parent = parent, seeds = seeds)
        val opened = runtime.locked {
            runtime.ensureRunning()
            ensureRoom(scopeId)
            val created = ScopeRuntime(
                runtime = runtime,
                key = key,
                parent = parent,
                id = runtime.configuration.ids.next(),
                scopeId = scopeId,
                seeds = seeds,
            )
            open += created
            scopeId?.let { id -> named[id] = created }
            created
        }
        val isAdopted = parent?.adopt(opened) != false
        if (!isAdopted) runtime.locked { forget(opened) }
        check(isAdopted) { "A closed scope opens no scope" }
        runtime.telemetry.scopeOpened(scope = key, id = opened.id, open = open.size)
        return opened
    }

    fun openOrGet(key: ScopeKey, scopeId: ScopeId, seeds: Map<DefinitionKey, Any>): ScopeRuntime = runtime.locked {
        val existing = named[scopeId]
        require(existing == null || existing.key == key) { "The scope id $scopeId belongs to another scope" }
        existing ?: open(key = key, parent = null, seeds = seeds, scopeId = scopeId)
    }

    fun byId(scopeId: ScopeId): ScopeRuntime? = runtime.locked { named[scopeId] }

    fun snapshot(): List<ScopeRuntime> = runtime.locked { open.toList() }

    fun closing(scope: ScopeRuntime) {
        runtime.locked { release(scope) }
    }

    fun closed(scope: ScopeRuntime, failures: Int) {
        val remaining = runtime.locked { forget(scope) }
        runtime.telemetry.scopeClosed(scope = scope.key, id = scope.id, failures = failures, open = remaining)
    }

    private fun forget(scope: ScopeRuntime): Int {
        open.remove(scope)
        release(scope)
        return open.size
    }

    private fun release(scope: ScopeRuntime) {
        val id = scope.scopeId ?: return
        if (named[id] === scope) named.remove(id)
    }

    private fun ensureOpenable(key: ScopeKey) {
        runtime.ensureRunning()
        if (key in runtime.registry.scopes) return
        throw ScopeNotDeclaredException(key)
    }

    private fun ensureRoom(scopeId: ScopeId?) {
        if (scopeId != null && scopeId in named) throw ScopeAlreadyOpenException(scopeId)
        val limit = runtime.configuration.scopeLimit
        if (open.size < limit) return
        throw ScopeLimitException(limit)
    }

    private fun ensurePlaced(key: ScopeKey, parent: ScopeRuntime?, seeds: Map<DefinitionKey, Any>) {
        val registry = runtime.registry
        val expected = registry.parents[key]
        val actual = parent?.key
        if (expected != actual) throw ScopeNestingException(scope = key, expected = expected, actual = actual)
        val unprovided = seeds.keys.firstOrNull { seed -> !registry.isProvided(key, seed) } ?: return
        throw ScopeSeedException(key, unprovided)
    }
}
