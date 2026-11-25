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

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.ScopeKey

/**
 * Thrown when a scope is opened with a value its modules do not declare as provided.
 *
 * @property scope the key of the scope.
 * @property definition the key of the value.
 * @since 1.0.0
 */
public class ScopeSeedException internal constructor(public val scope: ScopeKey, public val definition: DefinitionKey) :
    IllegalArgumentException("The scope $scope declares no provided value $definition")
