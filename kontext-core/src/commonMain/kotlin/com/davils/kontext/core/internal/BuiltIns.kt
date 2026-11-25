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

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Profile
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.definition.Production
import kotlin.reflect.typeOf

internal val profileKey: DefinitionKey = DefinitionKey(typeOf<Profile>(), null)

internal fun profileDeclaration(profile: Profile): Declaration = Declaration(
    key = profileKey,
    lifetime = Lifetime.Single,
    owner = null,
    profiles = emptySet(),
    production = Production.Sync { profile },
)
