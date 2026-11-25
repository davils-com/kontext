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

import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.definition.Production
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.parameter.Parameters

internal class Located(val runtime: ContainerRuntime, val host: Host, val declaration: Declaration) {
    private val isFactory: Boolean get() = declaration.lifetime == Lifetime.Factory

    fun instance(trail: Trail, key: DefinitionKey, parameters: Parameters): Any =
        when (val production = declaration.production) {
            Production.Provided -> provided(trail, key)
            is Production.Sync -> created(trail = trail, key = key, parameters = parameters, create = production.create)
            is Production.Async -> host.existing(declaration).takeUnless { isFactory } ?: suspending(trail, key)
        }

    suspend fun awaitInstance(trail: Trail, key: DefinitionKey, parameters: Parameters): Any {
        val production = declaration.production
        if (production !is Production.Async) return instance(trail, key, parameters)
        return awaited(trail = trail, key = key, parameters = parameters, create = production.create)
    }

    private fun created(
        trail: Trail,
        key: DefinitionKey,
        parameters: Parameters,
        create: DefinitionContext.() -> Any,
    ): Any {
        val next = trail + key
        if (isFactory) return runtime.produce(located = this, trail = next, parameters = parameters, create = create)
        ensureUnparameterized(trail, key, parameters)
        val memo = host.memoized(declaration) {
            runtime.produce(located = this, trail = next, parameters = Parameters.EMPTY, create = create)
        }
        return when (memo) {
            is Memo.Ready -> memo.instance
            Memo.Refused -> throw runtime.failed(ResolutionFailure.Closed(key, trail.keys))
            Memo.Reentered -> throw runtime.failed(ResolutionFailure.Circular(key, trail.keys))
        }
    }

    private suspend fun awaited(
        trail: Trail,
        key: DefinitionKey,
        parameters: Parameters,
        create: suspend DefinitionContext.() -> Any,
    ): Any {
        val next = trail + key
        if (isFactory) {
            return runtime.produceAsync(
                located = this,
                trail = next,
                parameters = parameters,
                create = create,
            )
        }
        ensureUnparameterized(trail, key, parameters)
        val instance = host.memoizedAsync(declaration) {
            runtime.produceAsync(located = this, trail = next, parameters = Parameters.EMPTY, create = create)
        }
        return instance ?: throw runtime.failed(ResolutionFailure.Closed(key, trail.keys))
    }

    private fun ensureUnparameterized(trail: Trail, key: DefinitionKey, parameters: Parameters) {
        if (parameters.isEmpty) return
        throw runtime.failed(ResolutionFailure.ParametersRefused(key, trail.keys))
    }

    private fun provided(trail: Trail, key: DefinitionKey): Any =
        host.seed(declaration.key) ?: throw runtime.failed(ResolutionFailure.Unprovided(key, trail.keys))

    private fun suspending(trail: Trail, key: DefinitionKey): Nothing =
        throw runtime.failed(ResolutionFailure.Suspending(key, trail.keys))
}
