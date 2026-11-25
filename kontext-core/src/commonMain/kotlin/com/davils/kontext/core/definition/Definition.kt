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
import com.davils.kontext.core.Qualifier
import kotlin.reflect.typeOf

/**
 * A declared definition, returned by the declarations of a builder so that it can be bound to more types with
 * [bind].
 *
 * @param T the type the definition creates.
 * @param draft the declaration being built.
 * @since 1.0.0
 */
public class Definition<out T : Any> internal constructor(private val draft: DefinitionDraft) {
    /**
     * The key the definition is declared under.
     *
     * @since 1.0.0
     */
    public val key: DefinitionKey get() = draft.key

    @PublishedApi
    internal fun alias(key: DefinitionKey) {
        draft.alias(key)
    }

    override fun toString(): String = "Definition($key)"
}

/**
 * Makes the definition found as [A] with [qualifier] as well, such as an implementation under its interface.
 *
 * A key found through a binding of one definition only resolves to it; a key bound by several definitions is
 * ambiguous for [com.davils.kontext.core.Resolver.get], and [com.davils.kontext.core.Resolver.getAll] returns them
 * all. Bind while the module is being declared.
 *
 * @param A the type the definition is found as, one of its own types or supertypes.
 * @param qualifier the qualifier of the binding, `null` for none.
 * @throws IllegalStateException if the module of the definition is already built.
 * @throws IllegalArgumentException if the definition is already found under that key.
 * @since 1.0.0
 */
public inline fun <reified A : Any> Definition<A>.bind(qualifier: Qualifier? = null) {
    alias(DefinitionKey(typeOf<A>(), qualifier))
}
