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

/**
 * Creates parameters of [values], each found by every type it is an instance of.
 *
 * @param values the values, at most [com.davils.kontext.core.ContainerLimits.MAX_PARAMETERS].
 * @return the parameters.
 * @throws IllegalArgumentException if there are too many values.
 * @since 1.0.0
 */
public fun parametersOf(vararg values: Any): Parameters = Parameters(values.map { value -> Parameter(value, null) })

/**
 * Creates parameters of the values [block] adds, each found by exactly its declared type.
 *
 * @param block adds the values.
 * @return the parameters.
 * @throws IllegalArgumentException if there are more than [com.davils.kontext.core.ContainerLimits.MAX_PARAMETERS]
 *   values.
 * @since 1.0.0
 */
public fun parametersOf(block: ParametersBuilder.() -> Unit): Parameters {
    val builder = ParametersBuilder()
    builder.block()
    return builder.build()
}
