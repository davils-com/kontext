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

package com.davils.kontext.benchmark

import com.davils.kontext.core.Container
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.Scope
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.container
import com.davils.kontext.core.module
import com.davils.kontext.core.parameter.Parameters
import com.davils.kontext.core.parameter.parametersOf
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.coroutines.runBlocking
import kotlinx.benchmark.Scope as BenchmarkScope

private val REQUEST: ScopeKey = ScopeKey("request")
private val SUSPENDING: Qualifier = Qualifier("suspending")
private val SCOPED: Qualifier = Qualifier("scoped")

@State(BenchmarkScope.Benchmark)
class ResolutionBenchmark {
    private var container: Container? = null
    private var scope: Scope? = null
    private val parameters: Parameters = parametersOf("benchmark")

    @Setup
    fun prepare() {
        val graph = module("benchmark") {
            single { Repository() }
            factory { Service(get()) }
            factory { Handler(parameters.get<String>(), get()) }
            singleAsync(SUSPENDING) { Repository() }
            scope(REQUEST) { scoped(SCOPED) { Service(get()) } }
        }
        val started = runBlocking { container { modules(graph) } }
        container = started
        scope = started.openScope(REQUEST)
    }

    @TearDown
    fun release() {
        runBlocking { container?.close() }
    }

    @Benchmark
    fun single(): Repository = running().get()

    @Benchmark
    fun factory(): Service = running().get()

    @Benchmark
    fun factoryWithParameters(): Handler = running().get(parameters = parameters)

    @Benchmark
    fun scoped(): Service = checkNotNull(scope).get(SCOPED)

    @Benchmark
    fun awaitedSingle(): Repository = runBlocking { running().await(SUSPENDING) }

    private fun running(): Container = checkNotNull(container)
}
