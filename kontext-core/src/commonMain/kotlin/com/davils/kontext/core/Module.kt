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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.core

import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.ScopeDeclaration
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList

/**
 * A named, immutable set of definitions, built by [module] and handed to a container.
 *
 * @property name the name of the module.
 * @param declarations the definitions the module declares itself.
 * @param scopes the scopes the module declares.
 * @param includes the modules the module includes.
 * @since 1.0.0
 */
public class Module internal constructor(
    public val name: ModuleName,
    internal val declarations: List<Declaration>,
    internal val scopes: List<ScopeDeclaration>,
    includes: List<Module>,
) {
    /**
     * The modules this module includes.
     *
     * @since 1.0.0
     */
    public val includes: List<Module> = includes.immutableList()

    /**
     * Returns this module and [other], for `modules(storage + web)`.
     *
     * @param other the other module.
     * @return both modules.
     * @since 1.0.0
     */
    public operator fun plus(other: Module): List<Module> = listOf(this, other)

    override fun toString(): String = "Module($name)"

    internal fun collectInto(collected: MutableList<Module>) {
        if (collected.any { module -> module === this }) return
        collected += this
        includes.forEach { module -> module.collectInto(collected) }
    }
}

/**
 * Returns these modules and [other].
 *
 * @param other the module to add.
 * @return the modules followed by [other].
 * @since 1.0.0
 */
public operator fun List<Module>.plus(other: Module): List<Module> = this + listOf(other)

internal fun flatten(roots: List<Module>): List<Module> {
    val collected = mutableListOf<Module>()
    roots.forEach { root -> root.collectInto(collected) }
    return collected
}
