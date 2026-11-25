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
        return edges.keys.mapNotNull { start -> cycleFrom(current = start, path = emptyList(), explored = explored) }
    }

    private fun cycleFrom(
        current: Declaration,
        path: List<Declaration>,
        explored: MutableSet<Declaration>,
    ): ResolutionFailure? {
        if (current in path) return ResolutionFailure.Circular(current.key, path.map { step -> step.key })
        val isFresh = explored.add(current)
        val known = edges.getValue(current)
        val next = known.takeIf { isFresh }.orEmpty()
        return next.firstNotNullOfOrNull { step ->
            cycleFrom(
                current = step,
                path = path + current,
                explored = explored,
            )
        }
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
