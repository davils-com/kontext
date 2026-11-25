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
import com.davils.kontext.core.Resolver
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.parameter.Parameters
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KType

internal class ResolutionContext(
    private val runtime: ContainerRuntime,
    private val scope: ScopeRuntime?,
    private val trail: Trail,
    override val parameters: Parameters,
) : DefinitionContext() {
    override val coroutineScope: CoroutineScope get() = (scope ?: runtime).coroutineScope

    override fun resolve(key: DefinitionKey, parameters: Parameters): Any =
        resolveOrNull(key, parameters) ?: throw absence(key)

    override fun resolveOrNull(key: DefinitionKey, parameters: Parameters): Any? {
        ensureResolvable(key)
        val located = runtime.locate(scope = scope, key = key, path = trail.keys) ?: return fallback(key)
        return located.instance(trail = trail, key = key, parameters = parameters)
    }

    override fun resolveAll(type: KType): List<Any> {
        ensureResolvable(DefinitionKey(type))
        val located = runtime.locateAll(scope, type)
        return located.map { candidate -> candidate.instance(trail, candidate.declaration.key, Parameters.EMPTY) }
    }

    override suspend fun resolveAsync(key: DefinitionKey, parameters: Parameters): Any =
        resolveAsyncOrNull(key, parameters) ?: throw absence(key)

    override suspend fun resolveAsyncOrNull(key: DefinitionKey, parameters: Parameters): Any? {
        ensureResolvable(key)
        val located = locateWhenReady(key) ?: return fallback(key)
        return located.awaitInstance(trail = trail, key = key, parameters = parameters)
    }

    override suspend fun resolveAllAsync(type: KType): List<Any> {
        ensureResolvable(DefinitionKey(type))
        runtime.awaitLoaded()
        val located = runtime.locateAll(scope, type)
        return located.map { candidate -> candidate.awaitInstance(trail, candidate.declaration.key, Parameters.EMPTY) }
    }

    override fun detached(): Resolver =
        ResolutionContext(runtime = runtime, scope = scope, trail = trail.detached(), parameters = Parameters.EMPTY)

    override fun properties(): Properties = runtime.properties

    private suspend fun locateWhenReady(key: DefinitionKey): Located? {
        val located = runtime.locate(scope = scope, key = key, path = trail.keys)
        if (located != null || !runtime.isLoading) return located
        runtime.awaitLoaded()
        return runtime.locate(scope = scope, key = key, path = trail.keys)
    }

    private fun fallback(key: DefinitionKey): Any? {
        val resolver = ResolutionContext(
            runtime = runtime,
            scope = scope,
            trail = trail + key,
            parameters = Parameters.EMPTY,
        )
        return runtime.fallback(key = key, resolver = resolver, path = trail.keys)
    }

    private fun absence(key: DefinitionKey): Throwable {
        val isLoading = runtime.isLoading
        if (isLoading) return runtime.failed(ResolutionFailure.NotReady(key, trail.keys))
        return runtime.failed(ResolutionFailure.Missing(key, trail.keys))
    }

    private fun ensureResolvable(key: DefinitionKey) {
        val isOpen = runtime.isResolvable && scope?.isOpen != false
        if (!isOpen) throw runtime.failed(ResolutionFailure.Closed(key, trail.keys))
        ensureAcyclic(key)
    }

    private fun ensureAcyclic(key: DefinitionKey) {
        if (key in trail) throw runtime.failed(ResolutionFailure.Circular(key, trail.keys))
        if (!trail.isExhausted) return
        throw runtime.failed(ResolutionFailure.TooDeep(key, trail.keys))
    }
}
