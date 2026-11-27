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

package com.davils.kontext.krypt

import com.davils.kontext.core.Resolver
import com.davils.kore.core.arguments.conversion.Converters
import com.davils.krypt.core.SecretText
import com.davils.krypt.core.secretText

/**
 * Reads the property [key] as a [SecretText] of krypt, such as a password or an API token, with
 * `Converters.secretText` of krypt.
 *
 * The secret is held as characters the caller wipes with [SecretText.destroy] or [SecretText.use] once it is used; a
 * new secret is read on every call. The text of a property source is a `String` that cannot be wiped, so keep the
 * secret in a source that holds it as briefly as possible. Neither the secret, its `toString` nor a refusal shows
 * the value.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case such as `db.password`.
 * @return the secret.
 * @throws com.davils.kontext.core.property.PropertyException if no source knows the key, a source fails or the text
 *   is empty or longer than `Limits.MAX_ARGUMENT_LENGTH` of Kore; the message names the key, never the value.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.3.0
 */
public fun Resolver.secret(key: String): SecretText = property(key, Converters.secretText)

/**
 * Reads the property [key] as a [SecretText] like [secret], or returns `null` if no source knows it.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the secret or `null`.
 * @throws com.davils.kontext.core.property.PropertyException if a source fails or the text is no secret; the message
 *   never shows the value.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.3.0
 */
public fun Resolver.secretOrNull(key: String): SecretText? = propertyOrNull(key, Converters.secretText)
