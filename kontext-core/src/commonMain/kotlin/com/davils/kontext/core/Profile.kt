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
 * The profile a container runs in, such as `live` or `mock`.
 *
 * Definitions declared in [com.davils.kontext.core.definition.ModuleBuilder.variant] exist only in the profiles named
 * there; every other definition exists in every profile. A container provides its own profile as a definition, so
 * that a factory can resolve it. A name is a dotted name in lower case of at most [ContainerLimits.MAX_NAME_LENGTH]
 * characters.
 *
 * @property value the name of the profile.
 * @throws IllegalArgumentException if [value] is not such a name.
 * @since 1.0.0
 */
@JvmInline
public value class Profile(public val value: String) {
    init {
        requireName(value, "profile")
    }

    override fun toString(): String = value

    /**
     * The profiles every application knows.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * The profile of a container that names none.
         *
         * @since 1.0.0
         */
        public val DEFAULT: Profile = Profile("default")
    }
}
