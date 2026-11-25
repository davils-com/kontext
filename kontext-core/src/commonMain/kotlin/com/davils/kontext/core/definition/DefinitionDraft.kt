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

package com.davils.kontext.core.definition

import com.davils.kontext.core.DefinitionKey

internal class DefinitionDraft(val key: DefinitionKey, private val declaration: Declaration) {
    private val aliases = mutableListOf<DefinitionKey>()
    private var isSealed = false

    fun alias(alias: DefinitionKey) {
        check(!isSealed) { "The definition $key belongs to a module that is already built, so it takes no binding" }
        require(alias != key && alias !in aliases) { "The definition $key is already found as $alias" }
        aliases += alias
    }

    fun seal(): Declaration {
        isSealed = true
        return declaration.aliased(aliases.toList())
    }
}
