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
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.error.ResolutionFailure

internal class StaticGraph(private val registry: Registry) {
    private val edges = HashMap<Declaration, List<Declaration>>()

    var checked: Int = 0
        private set

    fun unresolved(declaration: Declaration): List<ResolutionFailure> {
        val dependencies = declaration.dependencies.orEmpty()
        checked += dependencies.size
        val found = dependencies.map { dependency -> dependency to find(declaration.owner, dependency) }
        edges[declaration] = found.mapNotNull { (_, match) -> (match as? Match.Found)?.declaration }
        return found.mapNotNull { (dependency, match) -> failureOf(declaration, dependency, match) }
    }

    fun cycles(): List<ResolutionFailure> {
        val explored = HashSet<Declaration>()
        return edges.keys.mapNotNull { start -> freshCycleFrom(start = start, explored = explored) }
    }

    private fun freshCycleFrom(start: Declaration, explored: MutableSet<Declaration>): ResolutionFailure? {
        if (!explored.add(start)) return null
        return cycleFrom(start = start, explored = explored)
    }

    private fun cycleFrom(start: Declaration, explored: MutableSet<Declaration>): ResolutionFailure? {
        val path = ArrayDeque(listOf(start))
        val onPath = hashSetOf(start)
        val cursors = ArrayDeque(listOf(edges.getValue(start).iterator()))
        while (cursors.isNotEmpty()) {
            val cursor = cursors.last()
            if (!cursor.hasNext()) {
                cursors.removeLast()
                onPath -= path.removeLast()
                continue
            }
            val step = cursor.next()
            if (step in onPath) return ResolutionFailure.Circular(step.key, path.map { visited -> visited.key })
            if (explored.add(step)) {
                path.addLast(step)
                onPath += step
                cursors.addLast(edges.getValue(step).iterator())
            }
        }
        return null
    }

    private fun find(owner: ScopeKey?, key: DefinitionKey): Match? {
        val owners = generateSequence(owner) { place -> registry.parents[place] }.toList() + listOf(null)
        return owners.firstNotNullOfOrNull { place -> registry.match(place, key) }
    }

    private fun failureOf(declaration: Declaration, dependency: DefinitionKey, match: Match?): ResolutionFailure? =
        when (match) {
            null -> ResolutionFailure.Missing(dependency, listOf(declaration.key))

            is Match.Found -> null

            is Match.Many -> ResolutionFailure.Ambiguous(
                key = dependency,
                candidates = match.candidates.map { candidate -> candidate.key },
                path = listOf(declaration.key),
            )
        }
}
