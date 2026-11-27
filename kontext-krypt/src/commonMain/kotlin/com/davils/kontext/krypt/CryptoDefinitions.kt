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

import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.definition.Definition
import com.davils.kontext.core.definition.ModuleBuilder
import com.davils.krypt.core.Environment
import com.davils.krypt.core.EnvironmentBuilder
import com.davils.krypt.core.environment
import com.davils.kontinuity.core.Environment as ResilienceEnvironment

/**
 * Declares the [Environment] of krypt as a single, such as `cryptoEnvironment { policy = CryptoPolicy.Cnsa2 }`.
 *
 * The environment starts from [Environment.Default], secure randomness, the system clock and the modern policy, and
 * takes the tracer, the meter registry and the event bus of the container, so the components krypt builds report
 * next to the container; [configure] runs last and may change any of them. The container has a time source, not a
 * clock, so the clock of krypt stays the system clock unless [configure] sets another. An environment krypt refuses,
 * such as one with an event capacity out of range, fails its resolution with a
 * [ResolutionException][com.davils.kontext.core.error.ResolutionException]. The environment holds no state and is
 * safe to share between threads.
 *
 * @receiver the module that declares the environment.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @param configure changes what differs from the container, such as the policy or the audit sink.
 * @return the definition, to bind it to further types.
 * @since 1.3.0
 */
public fun ModuleBuilder.cryptoEnvironment(
    qualifier: Qualifier? = null,
    configure: EnvironmentBuilder.() -> Unit = {},
): Definition<Environment> = single(qualifier = qualifier) {
    environmentOf(surroundings = resilience(), configure = configure)
}

internal fun environmentOf(surroundings: ResilienceEnvironment, configure: EnvironmentBuilder.() -> Unit): Environment =
    environment {
        tracer = surroundings.tracer
        surroundings.meters?.let { registry -> meters(registry) }
        surroundings.events?.let { bus -> events(bus) }
        configure()
    }
