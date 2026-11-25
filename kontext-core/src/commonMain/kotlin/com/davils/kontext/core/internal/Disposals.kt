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

import com.davils.kontext.core.container.DisposalFailure
import com.davils.kontext.core.definition.Declaration
import com.davils.kore.core.failure.attempted

internal class Disposals(private val guard: Guard) {
    private val entries = mutableListOf<Disposal>()

    fun add(declaration: Declaration, instance: Any, action: suspend () -> Unit) {
        guard.locked { entries += Disposal(declaration, instance, action) }
    }

    fun take(instance: Any): Disposal? = guard.locked {
        val index = entries.indexOfFirst { entry -> entry.instance === instance }
        entries.getOrNull(index)?.also { entries.removeAt(index) }
    }

    suspend fun drain(): List<DisposalFailure> = drain { true }

    suspend fun drain(selected: (Declaration) -> Boolean): List<DisposalFailure> {
        val taken = guard.locked {
            val matching = entries.filter { entry -> selected(entry.declaration) }
            entries.removeAll(matching)
            matching
        }
        return taken.asReversed().mapNotNull { disposal -> disposal.run() }
    }

    class Disposal(val declaration: Declaration, val instance: Any, private val action: suspend () -> Unit) {
        suspend fun run(): DisposalFailure? {
            val failure = attempted { action() }.exceptionOrNull() ?: return null
            return DisposalFailure(declaration.key, failure)
        }
    }
}
