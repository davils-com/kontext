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

/**
 * Thrown when a resolution fails; [failure] tells why.
 *
 * The cause is what the factory threw for [ResolutionFailure.Failed], `null` otherwise.
 *
 * @property failure why the resolution failed.
 * @since 1.0.0
 */
public class ResolutionException internal constructor(public val failure: ResolutionFailure) :
    IllegalStateException(messageOf(failure), (failure as? ResolutionFailure.Failed)?.cause)

private fun messageOf(failure: ResolutionFailure): String {
    val trail = (failure.path + failure.key).joinToString(separator = " -> ")
    return "Cannot resolve ${failure.key} (${failure.reason}) along $trail"
}
