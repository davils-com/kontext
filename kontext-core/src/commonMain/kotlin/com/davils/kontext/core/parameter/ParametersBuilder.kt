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

package com.davils.kontext.core.parameter

import com.davils.kore.core.annotation.DavilsDsl
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Collects the values of [Parameters] together with their declared types, see [parametersOf].
 *
 * @since 1.0.0
 */
@DavilsDsl
public class ParametersBuilder internal constructor() {
    private val values = mutableListOf<Parameter>()

    /**
     * Adds [value], found by exactly the type [T], generic arguments included.
     *
     * @param T the type the value is found by.
     * @param value the value.
     * @since 1.0.0
     */
    public inline fun <reified T : Any> add(value: T) {
        put(value, typeOf<T>())
    }

    @PublishedApi
    internal fun put(value: Any, type: KType) {
        values += Parameter(value, type)
    }

    internal fun build(): Parameters = Parameters(values.toList())

    override fun toString(): String = "ParametersBuilder(${values.size} values)"
}
