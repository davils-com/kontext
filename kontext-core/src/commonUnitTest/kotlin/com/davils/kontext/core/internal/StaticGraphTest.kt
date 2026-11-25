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
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.definition.Production
import com.davils.kontext.core.error.ResolutionFailure
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.reflect.typeOf

private const val LENGTH: Int = 50_000

private fun keyOf(index: Int): DefinitionKey = DefinitionKey(typeOf<Int>(), Qualifier("n$index"))

private fun chainOf(isClosed: Boolean): StaticGraph {
    val declarations = List(LENGTH) { index ->
        val next = (index + 1).takeIf { following -> following < LENGTH || isClosed }
        Declaration(
            key = keyOf(index),
            lifetime = Lifetime.Factory,
            owner = null,
            profiles = emptySet(),
            production = Production.Provided,
            dependencies = listOfNotNull(next?.let { following -> keyOf(following % LENGTH) }),
        )
    }
    val graph = StaticGraph(Registry(declarations = declarations, scopeDeclarations = emptyList()))
    declarations.forEach { declaration -> graph.unresolved(declaration) }
    return graph
}

class StaticGraphTest : FunSpec({
    context("cycles") {
        test("follow a long chain of dependencies without overflowing the stack") {
            val graph = chainOf(isClosed = false)

            graph.cycles().shouldBeEmpty()
            graph.checked shouldBe LENGTH - 1
        }

        test("find a cycle at the end of a long chain once") {
            val cycles = chainOf(isClosed = true).cycles()

            cycles shouldHaveSize 1
            val circular = cycles.single().shouldBeInstanceOf<ResolutionFailure.Circular>()
            circular.path.first() shouldBe circular.key
            circular.path shouldHaveSize LENGTH
        }
    }
})
