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

package com.davils.kontext.core.definition

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Profile
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.extension.Retention

internal class Declaration(
    val key: DefinitionKey,
    val lifetime: Lifetime,
    val owner: ScopeKey?,
    val profiles: Set<Profile>,
    val production: Production,
    val isEager: Boolean = false,
    val dispose: (suspend (Any) -> Unit)? = null,
    val aliases: List<DefinitionKey> = emptyList(),
    val dependencies: List<DefinitionKey>? = null,
    val retention: Retention = Retention.FOREVER,
) {
    val keys: List<DefinitionKey> get() = listOf(key) + aliases

    fun appliesTo(profile: Profile): Boolean = profiles.isEmpty() || profile in profiles

    fun conflictsWith(other: Declaration): Boolean = owner == other.owner && key == other.key && overlaps(other)

    fun aliased(keys: List<DefinitionKey>): Declaration = Declaration(
        key = key,
        lifetime = lifetime,
        owner = owner,
        profiles = profiles,
        production = production,
        isEager = isEager,
        dispose = dispose,
        aliases = keys,
        dependencies = dependencies,
        retention = retention,
    )

    fun overlaps(other: Declaration): Boolean {
        val isUnrestricted = profiles.isEmpty() || other.profiles.isEmpty()
        return isUnrestricted || profiles.any { profile -> profile in other.profiles }
    }
}
