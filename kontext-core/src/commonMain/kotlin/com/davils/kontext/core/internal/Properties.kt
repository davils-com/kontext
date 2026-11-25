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

import com.davils.kontext.core.property.PropertyException
import com.davils.kontext.core.property.PropertyKey
import com.davils.kontext.core.property.PropertySource
import com.davils.kore.core.arguments.conversion.Converter
import com.davils.kore.core.failure.contained

internal class Properties(private val sources: List<PropertySource>) {
    fun raw(key: PropertyKey): String? {
        val read = contained { sources.asReversed().firstNotNullOfOrNull { source -> source.valueOf(key) } }
        return read.getOrElse { cause -> throw PropertyException(key, "The source of the property $key failed", cause) }
    }

    fun <T> converted(key: PropertyKey, converter: Converter<T>): T? {
        val raw = raw(key) ?: return null
        val converted = contained { converter.convert(raw) }
        return converted.getOrElse {
            throw PropertyException(
                key,
                "The property $key is not a valid ${converter.typeName}",
            )
        }
    }

    fun <T> required(key: PropertyKey, converter: Converter<T>): T =
        converted(key, converter) ?: throw PropertyException(key, "The property $key is not set")

    fun layered(over: Properties?): Properties = Properties(over?.sources.orEmpty() + sources)
}
