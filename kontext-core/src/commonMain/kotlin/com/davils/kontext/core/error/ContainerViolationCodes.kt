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

package com.davils.kontext.core.error

import com.davils.kore.core.validation.violation.ViolationCode

/**
 * The codes of the violations the configuration of a container is refused with.
 *
 * @since 1.0.0
 */
public object ContainerViolationCodes {
    /**
     * Two definitions share a key and a scope in an overlapping profile.
     *
     * @since 1.0.0
     */
    public val DUPLICATE_DEFINITION: ViolationCode = ViolationCode("kontext.duplicate-definition")

    /**
     * A definition hides one of an enclosing scope, the container or an ancestor container.
     *
     * @since 1.0.0
     */
    public val SHADOWED_DEFINITION: ViolationCode = ViolationCode("kontext.shadowed-definition")

    /**
     * A definition takes a key the container declares itself, such as its profile.
     *
     * @since 1.0.0
     */
    public val RESERVED_DEFINITION: ViolationCode = ViolationCode("kontext.reserved-definition")

    /**
     * Two different modules share a name.
     *
     * @since 1.0.0
     */
    public val DUPLICATE_MODULE: ViolationCode = ViolationCode("kontext.duplicate-module")

    /**
     * Modules declare one scope within different scopes.
     *
     * @since 1.0.0
     */
    public val CONFLICTING_SCOPE_NESTING: ViolationCode = ViolationCode("kontext.conflicting-scope-nesting")

    /**
     * Scopes are declared within each other in a cycle.
     *
     * @since 1.0.0
     */
    public val CYCLIC_SCOPE_NESTING: ViolationCode = ViolationCode("kontext.cyclic-scope-nesting")

    /**
     * A scope is declared within a scope no module declares.
     *
     * @since 1.0.0
     */
    public val UNKNOWN_SCOPE: ViolationCode = ViolationCode("kontext.unknown-scope")

    /**
     * A replacement replaces no declared definition.
     *
     * @since 1.0.0
     */
    public val UNKNOWN_REPLACEMENT: ViolationCode = ViolationCode("kontext.unknown-replacement")
}
