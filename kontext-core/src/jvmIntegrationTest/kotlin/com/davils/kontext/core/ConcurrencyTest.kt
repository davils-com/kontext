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
import com.davils.kontext.core.fixture.Ledger
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

private const val CALLERS: Int = 64
private const val THREADS: Int = 8
private const val ROUNDS: Int = 200
private val REQUEST: ScopeKey = ScopeKey("request")

class ConcurrencyTest : FunSpec({
    val executor = Executors.newFixedThreadPool(THREADS)
    val workers = executor.asCoroutineDispatcher()

    afterSpec { executor.shutdownNow() }

    test("a single is created once however many threads resolve it at once") {
        val counter = AtomicInteger()
        val container = container {
            modules(
                module("app") {
                    single {
                        Thread.sleep(SLOW_FACTORY_MILLIS)
                        Counted(counter)
                    }
                },
            )
        }

        val resolved = withContext(workers) {
            (1..CALLERS).map { async { container.get<Counted>() } }.awaitAll()
        }

        counter.get() shouldBe 1
        resolved.distinct() shouldHaveSize 1
        container.close().isClean shouldBe true
    }

    test("scopes open, resolve and close from many threads and leave nothing open") {
        val counter = AtomicInteger()
        val ledger = Ledger()
        val container = container {
            modules(
                module("app") {
                    single { ledger }
                    scope(REQUEST) {
                        scoped(close = { counted: Counted -> ledger.write("closed ${counted.number}") }) {
                            Counted(counter)
                        }
                    }
                },
            )
        }

        withContext(workers) {
            val rounds = (1..ROUNDS).map {
                async {
                    val scope = container.openScope(REQUEST)
                    val first = scope.get<Counted>()
                    val second = scope.get<Counted>()
                    scope.close()
                    first shouldBe second
                }
            }
            rounds.awaitAll()
        }

        counter.get() shouldBe ROUNDS
        ledger.entries shouldHaveSize ROUNDS
        container.close().isClean shouldBe true
    }

    test("a scoped instance is created once per scope under concurrent resolution") {
        val counter = AtomicInteger()
        val container = container {
            modules(module("app") { scope(REQUEST) { scoped { Counted(counter) } } })
        }
        val scope = container.openScope(REQUEST)

        val resolved = withContext(workers) {
            (1..CALLERS).map { async { scope.get<Counted>() } }.awaitAll()
        }

        resolved.distinct() shouldHaveSize 1
        container.close()
        scope.isOpen shouldBe false
    }
})

private const val SLOW_FACTORY_MILLIS: Long = 20L
