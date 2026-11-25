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

import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

private const val PARTIES: Int = 2
private const val WAIT_SECONDS: Long = 5L
private val TENANT: ScopeKey = ScopeKey("tenant")
private val USER: ScopeKey = ScopeKey("user")
private val A: Qualifier = Qualifier("a")
private val B: Qualifier = Qualifier("b")
private val C: Qualifier = Qualifier("c")
private val D: Qualifier = Qualifier("d")

class CreationTest : FunSpec({
    val executor = Executors.newFixedThreadPool(PARTIES)
    val workers = executor.asCoroutineDispatcher()

    afterSpec { executor.shutdownNow() }

    test("two threads creating in scopes linked to each other do not block each other") {
        val inside = CountDownLatch(PARTIES)
        val container = container {
            modules(
                module("app") {
                    scope(TENANT) {
                        scoped(qualifier = A) { inside.meet() + get<String>(D) }
                        scoped(qualifier = B) { "b" }
                    }
                    scope(USER) {
                        scoped(qualifier = C) { inside.meet() + get<String>(B) }
                        scoped(qualifier = D) { "d" }
                    }
                },
            )
        }
        val tenant = container.openScope(TENANT)
        val user = container.openScope(USER)
        tenant.link(user)
        user.link(tenant)

        val created = withTimeout(WAIT_SECONDS.seconds) {
            withContext(workers) {
                val first = async { tenant.get<String>(A) }
                val second = async { user.get<String>(C) }
                first.await() + second.await()
            }
        }

        created shouldBe "db"
        container.close()
    }

    test("two threads creating a cycle from both ends both report it instead of waiting forever") {
        val inside = CountDownLatch(PARTIES)
        val container = container {
            modules(
                module("app") {
                    single(qualifier = A) { inside.meet() + get<String>(B) }
                    single(qualifier = B) { inside.meet() + get<String>(A) }
                },
            )
        }

        val failures = withTimeout(WAIT_SECONDS.seconds) {
            withContext(workers) {
                val first = async { shouldThrow<ResolutionException> { container.get<String>(A) } }
                val second = async { shouldThrow<ResolutionException> { container.get<String>(B) } }
                listOf(first.await(), second.await())
            }
        }

        failures.forEach { failure -> failure.failure.shouldBeInstanceOf<ResolutionFailure.Circular>() }
        container.close()
    }
})

private fun CountDownLatch.meet(): String {
    countDown()
    await(WAIT_SECONDS, TimeUnit.SECONDS)
    return ""
}
