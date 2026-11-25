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

import com.davils.kontext.core.ContainerLimits
import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Resolver
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.parameter.Parameters
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KType

internal class ResolutionContext(
    private val runtime: ContainerRuntime,
    private val scope: ScopeRuntime?,
    private val path: List<DefinitionKey>,
    override val parameters: Parameters,
) : DefinitionContext() {
    override val coroutineScope: CoroutineScope get() = (scope ?: runtime).coroutineScope

    override fun resolve(key: DefinitionKey, parameters: Parameters): Any =
        resolveOrNull(key, parameters) ?: throw absence(key)

    override fun resolveOrNull(key: DefinitionKey, parameters: Parameters): Any? {
        ensureResolvable(key)
        val located = runtime.locate(scope = scope, key = key, path = path) ?: return fallback(key)
        return located.instance(path = path, key = key, parameters = parameters)
    }

    override fun resolveAll(type: KType): List<Any> {
        ensureResolvable(DefinitionKey(type))
        val located = runtime.locateAll(scope, type)
        return located.map { candidate -> candidate.instance(path, candidate.declaration.key, Parameters.EMPTY) }
    }

    override suspend fun resolveAsync(key: DefinitionKey, parameters: Parameters): Any =
        resolveAsyncOrNull(key, parameters) ?: throw absence(key)

    override suspend fun resolveAsyncOrNull(key: DefinitionKey, parameters: Parameters): Any? {
        ensureResolvable(key)
        val located = locateWhenReady(key) ?: return fallback(key)
        return located.awaitInstance(path = path, key = key, parameters = parameters)
    }

    override suspend fun resolveAllAsync(type: KType): List<Any> {
        ensureResolvable(DefinitionKey(type))
        runtime.awaitLoaded()
        val located = runtime.locateAll(scope, type)
        return located.map { candidate -> candidate.awaitInstance(path, candidate.declaration.key, Parameters.EMPTY) }
    }

    override fun detached(): Resolver =
        ResolutionContext(runtime = runtime, scope = scope, path = emptyList(), parameters = Parameters.EMPTY)

    override fun properties(): Properties = runtime.properties

    private suspend fun locateWhenReady(key: DefinitionKey): Located? {
        val located = runtime.locate(scope = scope, key = key, path = path)
        if (located != null || !runtime.isLoading) return located
        runtime.awaitLoaded()
        return runtime.locate(scope = scope, key = key, path = path)
    }

    private fun fallback(key: DefinitionKey): Any? = runtime.fallback(key = key, resolver = detached(), path = path)

    private fun absence(key: DefinitionKey): Throwable {
        val isLoading = runtime.isLoading
        if (isLoading) return runtime.failed(ResolutionFailure.NotReady(key, path))
        return runtime.failed(ResolutionFailure.Missing(key, path))
    }

    private fun ensureResolvable(key: DefinitionKey) {
        val isOpen = runtime.isResolvable && scope?.isOpen != false
        if (!isOpen) throw runtime.failed(ResolutionFailure.Closed(key, path))
        ensureAcyclic(key)
    }

    private fun ensureAcyclic(key: DefinitionKey) {
        if (key in path) throw runtime.failed(ResolutionFailure.Circular(key, path))
        if (path.size < ContainerLimits.MAX_RESOLUTION_DEPTH) return
        throw runtime.failed(ResolutionFailure.TooDeep(key, path))
    }
}
