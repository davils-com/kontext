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

    fun instance(path: List<DefinitionKey>, key: DefinitionKey, parameters: Parameters): Any =
        when (val production = declaration.production) {
            Production.Provided -> provided(path, key)
            is Production.Sync -> created(path = path, key = key, parameters = parameters, create = production.create)
            is Production.Async -> host.existing(declaration).takeUnless { isFactory } ?: suspending(path, key)
        }

    suspend fun awaitInstance(path: List<DefinitionKey>, key: DefinitionKey, parameters: Parameters): Any {
        val production = declaration.production
        if (production !is Production.Async) return instance(path, key, parameters)
        return awaited(path = path, key = key, parameters = parameters, create = production.create)
    }

    private fun created(
        path: List<DefinitionKey>,
        key: DefinitionKey,
        parameters: Parameters,
        create: DefinitionContext.() -> Any,
    ): Any {
        val trail = path + key
        if (isFactory) return runtime.produce(located = this, trail = trail, parameters = parameters, create = create)
        ensureUnparameterized(path, key, parameters)
        return host.memoized(declaration) {
            runtime.produce(located = this, trail = trail, parameters = Parameters.EMPTY, create = create)
        }
    }

    private suspend fun awaited(
        path: List<DefinitionKey>,
        key: DefinitionKey,
        parameters: Parameters,
        create: suspend DefinitionContext.() -> Any,
    ): Any {
        val trail = path + key
        if (isFactory) {
            return runtime.produceAsync(
                located = this,
                trail = trail,
                parameters = parameters,
                create = create,
            )
        }
        ensureUnparameterized(path, key, parameters)
        val instance = host.memoizedAsync(declaration) {
            runtime.produceAsync(located = this, trail = trail, parameters = Parameters.EMPTY, create = create)
        }
        return instance ?: throw runtime.failed(ResolutionFailure.Closed(key, path))
    }

    private fun ensureUnparameterized(path: List<DefinitionKey>, key: DefinitionKey, parameters: Parameters) {
        if (parameters.isEmpty) return
        throw runtime.failed(ResolutionFailure.ParametersRefused(key, path))
    }

    private fun provided(path: List<DefinitionKey>, key: DefinitionKey): Any =
        host.seed(declaration.key) ?: throw runtime.failed(ResolutionFailure.Unprovided(key, path))

    private fun suspending(path: List<DefinitionKey>, key: DefinitionKey): Nothing =
        throw runtime.failed(ResolutionFailure.Suspending(key, path))
}
