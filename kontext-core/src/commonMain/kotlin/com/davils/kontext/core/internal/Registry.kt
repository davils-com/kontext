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
import com.davils.kontext.core.definition.Production
import com.davils.kontext.core.definition.ScopeDeclaration
import kotlin.reflect.KType

internal class Registry(val declarations: List<Declaration>, scopeDeclarations: List<ScopeDeclaration>) {
    private val primary: Map<Address<DefinitionKey>, Declaration> =
        declarations.associateBy { declaration -> Address(declaration.owner, declaration.key) }
    private val aliases: Map<Address<DefinitionKey>, List<Declaration>> = index { declaration ->
        declaration.aliases.map { alias -> Address(declaration.owner, alias) }
    }
    private val types: Map<Address<DefinitionKey>, List<Declaration>> = index { declaration ->
        declaration.keys.map { key -> Address(declaration.owner, DefinitionKey(key.type)) }
    }

    val parents: Map<ScopeKey, ScopeKey?> = scopeDeclarations.associate { scope -> scope.key to scope.within }
    val scopes: Set<ScopeKey> = parents.keys
    val eager: List<Declaration> = declarations.filter { declaration -> declaration.isEager }
    val keys: Set<DefinitionKey> = declarations
        .filter { declaration -> declaration.owner == null }
        .mapTo(LinkedHashSet()) { declaration -> declaration.key }

    fun match(owner: ScopeKey?, key: DefinitionKey): Match? {
        val address = Address(owner, key)
        val declared = primary[address]
        if (declared != null) return Match.Found(declared)
        return matchOf(aliases[address].orEmpty())
    }

    fun all(owner: ScopeKey?, type: KType): List<Declaration> = types[Address(owner, DefinitionKey(type))].orEmpty()

    fun of(owner: ScopeKey?): List<Declaration> = declarations.filter { declaration -> declaration.owner == owner }

    fun isProvided(owner: ScopeKey, key: DefinitionKey): Boolean {
        val declaration = primary[Address(owner, key)] ?: return false
        return declaration.production == Production.Provided
    }

    private fun <K> index(keysOf: (Declaration) -> List<Address<K>>): Map<Address<K>, List<Declaration>> {
        val pairs = declarations.flatMap { declaration ->
            keysOf(
                declaration,
            ).map { address -> address to declaration }
        }
        val grouped = pairs.groupBy(keySelector = { pair -> pair.first }, valueTransform = { pair -> pair.second })
        return grouped.mapValues { entry -> entry.value.distinct() }
    }

    private fun matchOf(candidates: List<Declaration>): Match? {
        if (candidates.size > 1) return Match.Many(candidates)
        return candidates.firstOrNull()?.let(Match::Found)
    }
}
