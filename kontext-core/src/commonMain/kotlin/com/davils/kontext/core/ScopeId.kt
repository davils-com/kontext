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
 * The id an application gives an opened scope, such as the id of a session, to find the scope again with
 * [Container.scope] or [Container.openOrGetScope].
 *
 * An id starts with an ASCII letter or digit, holds only letters, digits, `.`, `_`, `:` and `-` and is at most
 * [ContainerLimits.MAX_NAME_LENGTH] characters long. Ids are unique among the open scopes of a container.
 *
 * @property value the id.
 * @throws IllegalArgumentException if [value] is not such an id.
 * @since 1.0.0
 */
@JvmInline
public value class ScopeId(public val value: String) {
    init {
        requireId(value, "scope id")
    }

    override fun toString(): String = value
}
