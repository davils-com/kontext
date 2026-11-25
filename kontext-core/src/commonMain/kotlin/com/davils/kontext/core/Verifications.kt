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

import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.internal.verified
import com.davils.kontext.core.internal.verifiedStatically
import com.davils.kontext.core.verify.Verification

/**
 * Checks a configuration in every one of [profiles] by running it: it validates the configuration, starts the
 * container, loads its lazy modules, resolves every definition, suspending ones included, opens every scope with its
 * enclosing scopes and resolves its definitions, and closes it all.
 *
 * Values a scope is provided with when it opens are not known here, so a definition that needs one is not reported.
 * Factories run, so [block] should declare test doubles for anything with side effects. [block] runs once per
 * profile.
 *
 * @param profiles the profiles to check.
 * @param block configures the container.
 * @return the outcome with every problem found.
 * @throws IllegalArgumentException if [profiles] is empty.
 * @since 1.0.0
 */
public suspend fun verify(
    profiles: Set<Profile> = setOf(Profile.DEFAULT),
    block: ContainerBuilder.() -> Unit,
): Verification = verified(profiles, block)

/**
 * Checks a configuration in every one of [profiles] without creating any instance: it validates the configuration
 * and follows the dependencies the constructor definitions, such as `singleOf(::Service)`, declare, and reports
 * every dependency that is missing or ambiguous from where it is declared and every cycle among them.
 *
 * Definitions declared with a lambda declare no dependencies and are only validated. This check runs on every
 * platform and fits a unit test of the module graph.
 *
 * @param profiles the profiles to check.
 * @param block configures the container.
 * @return the outcome; [Verification.Passed.resolved] counts the checked dependencies.
 * @throws IllegalArgumentException if [profiles] is empty.
 * @since 1.0.0
 */
public fun verifyStatically(
    profiles: Set<Profile> = setOf(Profile.DEFAULT),
    block: ContainerBuilder.() -> Unit,
): Verification = verifiedStatically(profiles, block)
