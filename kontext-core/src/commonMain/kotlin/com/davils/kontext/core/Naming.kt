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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.core

import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.text.SafeText

private val namePattern: Regex = Regex("[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*")
private val idPattern: Regex = Regex("[A-Za-z0-9][A-Za-z0-9._:-]*")

internal fun requireName(value: String, kind: String) {
    val isValid = value.length <= ContainerLimits.MAX_NAME_LENGTH && namePattern.matches(value)
    require(isValid) {
        "A $kind is a dotted name in lower case of at most ${ContainerLimits.MAX_NAME_LENGTH} characters " +
            "such as app.http, but was ${SafeText.quoted(value)}"
    }
}

internal fun requireId(value: String, kind: String) {
    val isValid = value.length <= ContainerLimits.MAX_NAME_LENGTH && idPattern.matches(value)
    require(isValid) {
        "A $kind starts with an ASCII letter or digit, holds only letters, digits, '.', '_', ':' and '-' and is at " +
            "most ${ContainerLimits.MAX_NAME_LENGTH} characters long, but was ${SafeText.quoted(value)}"
    }
}
