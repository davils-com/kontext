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

import com.davils.kontext.core.sameType
import kotlin.reflect.KType

@PublishedApi
internal data class Parameter(val value: Any, val type: KType?)

@PublishedApi
internal inline fun Parameter.matches(requested: KType, isInstance: (Any) -> Boolean): Boolean {
    val declared = type ?: return !isGeneric(requested) && isInstance(value)
    return sameType(first = declared, second = requested)
}

@PublishedApi
internal fun isGeneric(type: KType): Boolean = type.arguments.any { projection -> projection.type != null }
