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

import com.davils.kontext.core.Module
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.ScopeDeclaration

internal data class AuditInput(
    val modules: List<Module>,
    val replacements: List<Module>,
    val declared: List<Declaration>,
    val replacing: List<Declaration>,
    val scopes: List<ScopeDeclaration>,
    val effective: List<Declaration>,
    val parent: ContainerRuntime?,
    val children: List<ContainerRuntime> = emptyList(),
)
