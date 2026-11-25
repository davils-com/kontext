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

package com.davils.kontext.environment

import com.davils.knot.environment.Environment
import com.davils.kontext.core.property.PropertyKey
import com.davils.kontext.core.property.PropertySource

/**
 * A [PropertySource] over an [Environment] of Knot, such as `Environment.SYSTEM` or one layered with `environment { }`.
 *
 * The environment is read on every lookup, so a live environment is seen as it is; values are never shown.
 *
 * @property environment the environment.
 * @property naming maps a property key to a variable name.
 * @since 1.0.0
 */
public class EnvironmentPropertySource(
    public val environment: Environment,
    public val naming: PropertyNaming = PropertyNaming.UPPER_SNAKE,
) : PropertySource {
    override fun valueOf(key: PropertyKey): String? = environment.variable(naming.nameOf(key))

    override fun toString(): String = "EnvironmentPropertySource(${environment.names.size} variables)"
}
