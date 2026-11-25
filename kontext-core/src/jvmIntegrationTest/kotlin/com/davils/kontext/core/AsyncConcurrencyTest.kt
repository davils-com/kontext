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

import com.davils.kontext.core.fixture.Counted
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

private const val CALLERS: Int = 64
private const val THREADS: Int = 8
private const val ROUNDS: Int = 50
private const val CREATION_MILLIS: Long = 20L

class AsyncConcurrencyTest : FunSpec({
    val executor = Executors.newFixedThreadPool(THREADS)
    val workers = executor.asCoroutineDispatcher()

    afterSpec { executor.shutdownNow() }

    test("a suspending single is created once across many threads") {
        val counter = AtomicInteger()
        val started = container {
            coroutineContext = workers
            modules(
                module("app") {
                    singleAsync {
                        delay(CREATION_MILLIS)
                        Counted(counter)
                    }
                },
            )
        }

        val resolved = withContext(workers) {
            val callers = (1..CALLERS).map { async { started.await<Counted>() } }
            callers.awaitAll()
        }

        counter.get() shouldBe 1
        resolved.distinct() shouldHaveSize 1
        started.close().isClean shouldBe true
    }

    test("modules load and unload while other threads resolve") {
        val started = container {
            coroutineContext = workers
            modules(module("base") { single { 1 } })
        }

        withContext(workers) {
            val loaders = (1..ROUNDS).map { round ->
                async {
                    val handle = started.load(module("round.r$round") { single(Qualifier("r$round")) { round } })
                    started.get<Int>(Qualifier("r$round")) shouldBe round
                    handle.unload()
                }
            }
            val readers = (1..ROUNDS).map { async { started.get<Int>() } }
            loaders.awaitAll()
            readers.awaitAll().distinct() shouldBe listOf(1)
        }

        started.definitions.size shouldBe 2
        started.close().isClean shouldBe true
    }
})
