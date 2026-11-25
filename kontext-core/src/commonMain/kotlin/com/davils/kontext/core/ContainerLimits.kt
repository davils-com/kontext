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

/**
 * The limits Kontext checks before it does work that grows with its input.
 *
 * @since 1.0.0
 */
public object ContainerLimits {
    /**
     * The longest name of a container, a module, a scope, a qualifier, a profile, a scope id or a property key.
     *
     * @since 1.0.0
     */
    public const val MAX_NAME_LENGTH: Int = 128

    /**
     * The deepest chain of definitions one resolution may follow before it fails with
     * [com.davils.kontext.core.error.ResolutionFailure.TooDeep].
     *
     * @since 1.0.0
     */
    public const val MAX_RESOLUTION_DEPTH: Int = 256

    /**
     * The most values one [com.davils.kontext.core.parameter.Parameters] holds.
     *
     * @since 1.0.0
     */
    public const val MAX_PARAMETERS: Int = 16

    /**
     * The most scopes a container keeps open at once, its nested scopes included, unless its builder sets another
     * limit.
     *
     * @since 1.0.0
     */
    public const val MAX_OPEN_SCOPES: Int = 65_536

    /**
     * The most scopes one scope links to.
     *
     * @since 1.0.0
     */
    public const val MAX_LINKS: Int = 64
}
