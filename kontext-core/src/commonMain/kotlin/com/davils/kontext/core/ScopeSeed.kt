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

package com.davils.kontext.core

import com.davils.kore.core.annotation.DavilsDsl
import kotlin.reflect.typeOf

/**
 * The values the opener of a scope provides for its
 * [provided][com.davils.kontext.core.definition.ScopeBuilder.provided] definitions.
 *
 * @since 1.0.0
 */
@DavilsDsl
public class ScopeSeed internal constructor() {
    private val values = mutableMapOf<DefinitionKey, Any>()

    /**
     * Provides [instance] as the value of [T] with [qualifier].
     *
     * @param T the type the value is provided as.
     * @param instance the value.
     * @param qualifier the qualifier of the definition, `null` for none.
     * @throws IllegalArgumentException if the key is already provided.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> provide(instance: T, qualifier: Qualifier? = null) {
        put(DefinitionKey(typeOf<T>(), qualifier), instance)
    }

    @PublishedApi
    internal fun put(key: DefinitionKey, instance: Any) {
        require(key !in values) { "The scope is already provided with $key" }
        values[key] = instance
    }

    internal fun seeds(): Map<DefinitionKey, Any> = values.toMap()

    override fun toString(): String = "ScopeSeed(${values.keys})"
}

internal fun seedsOf(block: ScopeSeed.() -> Unit): Map<DefinitionKey, Any> {
    val seed = ScopeSeed()
    seed.block()
    return seed.seeds()
}
