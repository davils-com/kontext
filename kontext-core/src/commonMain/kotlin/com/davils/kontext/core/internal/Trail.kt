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

import com.davils.kontext.core.ContainerLimits
import com.davils.kontext.core.DefinitionKey

internal class Trail private constructor(val keys: List<DefinitionKey>, private val depth: Int) {
    val isExhausted: Boolean get() = depth + keys.size >= ContainerLimits.MAX_RESOLUTION_DEPTH

    operator fun plus(key: DefinitionKey): Trail = Trail(keys + key, depth)

    operator fun contains(key: DefinitionKey): Boolean = key in keys

    fun detached(): Trail = Trail(emptyList(), depth + keys.size)

    companion object {
        val ROOT: Trail = Trail(emptyList(), 0)
    }
}
