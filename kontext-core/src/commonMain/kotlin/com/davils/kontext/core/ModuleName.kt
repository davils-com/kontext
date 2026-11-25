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

import kotlin.jvm.JvmInline

/**
 * The name of a module; the modules of one container have different names.
 *
 * A name is a dotted name in lower case, such as `app.http`, of at most [ContainerLimits.MAX_NAME_LENGTH] characters.
 *
 * @property value the name.
 * @throws IllegalArgumentException if [value] is not such a name.
 * @since 1.0.0
 */
@JvmInline
public value class ModuleName(public val value: String) {
    init {
        requireName(value, "module name")
    }

    override fun toString(): String = value
}
