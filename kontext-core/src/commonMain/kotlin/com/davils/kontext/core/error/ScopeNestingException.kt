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

import com.davils.kontext.core.ScopeKey

/**
 * Thrown when a scope is opened somewhere else than within the scope it is declared in.
 *
 * @property scope the key of the scope.
 * @property expected the key of the scope it is declared within, `null` for the container.
 * @property actual the key of the scope it was opened in, `null` for the container.
 * @since 1.0.0
 */
public class ScopeNestingException internal constructor(
    public val scope: ScopeKey,
    public val expected: ScopeKey?,
    public val actual: ScopeKey?,
) : IllegalArgumentException("The scope $scope is opened within ${placeOf(expected)}, not within ${placeOf(actual)}")

private fun placeOf(scope: ScopeKey?): String {
    if (scope == null) return "the container"
    return "the scope $scope"
}
