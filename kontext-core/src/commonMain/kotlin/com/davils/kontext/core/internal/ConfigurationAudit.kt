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
import com.davils.kontext.core.definition.ScopeDeclaration
import com.davils.kontext.core.error.ContainerViolationCodes

internal fun auditOf(input: AuditInput): List<Finding> {
    val scopes = input.scopes
    val parents = scopes.associate { scope -> scope.key to scope.within }
    return buildList {
        addAll(moduleFindings(input))
        addAll(definitionFindings(input))
        addAll(scopeFindings(scopes))
        addAll(shadowFindings(input, parents))
    }
}

private fun moduleFindings(input: AuditInput): List<Finding> {
    val byName = (input.modules + input.replacements).groupBy { module -> module.name }
    val duplicated = byName.filterValues { modules -> modules.size > 1 }
    return duplicated.map { (name, modules) ->
        Finding(ContainerViolationCodes.DUPLICATE_MODULE, "declares the module $name ${modules.size} times")
    }
}

private fun definitionFindings(input: AuditInput): List<Finding> {
    val declared = input.declared
    val replacing = input.replacing
    val reserved = (declared + replacing).filter { declaration -> profileKey in declaration.keys }
    val reservations = reserved.map { declaration ->
        val message = "declares $profileKey ${placeOf(declaration.owner)}, which the container declares itself"
        Finding(ContainerViolationCodes.RESERVED_DEFINITION, message)
    }
    val unknown = replacing.filter { replacement ->
        declared.none { declaration ->
            replacement.conflictsWith(
                declaration,
            )
        }
    }
    val replacements = unknown.map { replacement ->
        val message = "replaces ${replacement.key} ${placeOf(replacement.owner)}, which no module declares"
        Finding(code = ContainerViolationCodes.UNKNOWN_REPLACEMENT, message = message, isReplacement = true)
    }
    val audited = input.effective.filterNot { declaration -> profileKey in declaration.keys }
    val duplicates = buildList {
        addAll(duplicatesOf(declared, isReplacement = false))
        addAll(duplicatesOf(replacing, isReplacement = true))
        addAll(duplicatesOf(audited, isReplacement = false))
    }
    return buildList {
        addAll(reservations)
        addAll(duplicates.distinctBy { finding -> finding.message })
        addAll(replacements)
    }
}

private fun duplicatesOf(declarations: List<Declaration>, isReplacement: Boolean): List<Finding> {
    val bound = declarations.flatMap { declaration ->
        declaration.keys.map { key -> Address(declaration.owner, key) to declaration }
    }
    val groups = bound.groupBy(keySelector = { pair -> pair.first }, valueTransform = { pair -> pair.second })
    val conflicting = groups.filter { (address, group) -> hasConflict(address.key, group) }
    return conflicting.keys.map { address ->
        val message = "declares ${address.key} ${placeOf(address.owner)} more than once in one profile"
        Finding(code = ContainerViolationCodes.DUPLICATE_DEFINITION, message = message, isReplacement = isReplacement)
    }
}

private fun hasConflict(key: DefinitionKey, group: List<Declaration>): Boolean =
    group.withIndex().any { (index, declaration) ->
        group.drop(index + 1).any { other -> collides(key = key, first = declaration, second = other) }
    }

private fun collides(key: DefinitionKey, first: Declaration, second: Declaration): Boolean {
    val isPrimary = first.key == key || second.key == key
    return isPrimary && first.overlaps(second)
}

private fun scopeFindings(scopes: List<ScopeDeclaration>): List<Finding> {
    val withins = scopes.groupBy(keySelector = { scope -> scope.key }, valueTransform = { scope -> scope.within })
    val distinct = withins.mapValues { entry -> entry.value.distinct() }
    val conflicting = distinct.filterValues { places -> places.size > 1 }.keys.map { key ->
        Finding(ContainerViolationCodes.CONFLICTING_SCOPE_NESTING, "declares the scope $key within different places")
    }
    val unknown = distinct.flatMap { (key, places) ->
        val nested = places.filterNotNull()
        val undeclared = nested.filter { place -> place !in distinct }
        undeclared.map { place ->
            Finding(
                ContainerViolationCodes.UNKNOWN_SCOPE,
                "declares the scope $key within $place, which is not declared",
            )
        }
    }
    val parents = distinct.mapValues { entry -> entry.value.first() }
    val cycles = parents.keys.filter { key -> key in ancestorsOf(key, parents) }
    val cyclic = cycles.map { key ->
        Finding(ContainerViolationCodes.CYCLIC_SCOPE_NESTING, "declares the scope $key within itself")
    }
    return buildList {
        addAll(conflicting)
        addAll(unknown)
        addAll(cyclic)
    }
}

private fun shadowFindings(input: AuditInput, parents: Map<ScopeKey, ScopeKey?>): List<Finding> {
    val effective = input.effective.filter { declaration -> declaration.key != profileKey }
    val bound = effective.flatMap { declaration ->
        declaration.keys.map { key -> Address(declaration.owner, key) }
    }
    val grouped = bound.groupBy(keySelector = { address -> address.owner }, valueTransform = { address -> address.key })
    val keysByOwner = grouped.mapValues { entry -> entry.value.toSet() }
    val scoped = effective.mapNotNull { declaration -> scopedShadow(declaration, parents, keysByOwner) }
    val inherited = effective.filter { declaration -> declaration.owner == null }.mapNotNull { declaration ->
        inheritedShadow(input, declaration)
    }
    return scoped + inherited
}

private fun scopedShadow(
    declaration: Declaration,
    parents: Map<ScopeKey, ScopeKey?>,
    keysByOwner: Map<ScopeKey?, Set<DefinitionKey>>,
): Finding? {
    val owner = declaration.owner ?: return null
    val enclosing: List<Pair<String, ScopeKey?>> = ancestorsOf(
        owner,
        parents,
    ).map { place -> "the scope $place" to place }
    val places = enclosing + ("the container" to null)
    return declaration.keys.firstNotNullOfOrNull { key ->
        val hidden = places.firstOrNull { (_, place) -> key in keysByOwner[place].orEmpty() }
        hidden?.let { (name, _) -> shadowed(key = key, owner = owner, place = name) }
    }
}

private fun inheritedShadow(input: AuditInput, declaration: Declaration): Finding? =
    declaration.keys.firstNotNullOfOrNull { key ->
        val place = inheritedFrom(input, key) ?: childOf(input, key)
        place?.let { hidden -> shadowed(key = key, owner = null, place = hidden) }
    }

private fun childOf(input: AuditInput, key: DefinitionKey): String? {
    val declaring = input.children.firstOrNull { runtime -> key in runtime.registry.rootKeys } ?: return null
    return "the child container ${declaring.name}"
}

private fun inheritedFrom(input: AuditInput, key: DefinitionKey): String? {
    val ancestors = generateSequence(input.parent) { runtime -> runtime.parent }
    val declaring = ancestors.firstOrNull { runtime -> key in runtime.registry.rootKeys } ?: return null
    return "the container ${declaring.name}"
}

private fun shadowed(key: DefinitionKey, owner: ScopeKey?, place: String): Finding {
    val message = "declares $key ${placeOf(owner)}, which hides the one of $place"
    return Finding(ContainerViolationCodes.SHADOWED_DEFINITION, message)
}

private fun ancestorsOf(key: ScopeKey, parents: Map<ScopeKey, ScopeKey?>): List<ScopeKey> {
    val ancestors = generateSequence(parents[key]) { place -> parents[place] }
    return ancestors.take(parents.size).toList()
}

private fun placeOf(owner: ScopeKey?): String {
    if (owner == null) return "in the container"
    return "in the scope $owner"
}
