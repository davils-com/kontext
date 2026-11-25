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

import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.error.ScopeLimitException
import com.davils.kontext.core.fixture.BROKEN
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Handler
import com.davils.kontext.core.fixture.LIVE
import com.davils.kontext.core.fixture.MOCK
import com.davils.kontext.core.fixture.Opening
import com.davils.kontext.core.fixture.PAGE
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.UNCLOSABLE
import com.davils.kontext.core.verify.Verification
import com.davils.kontext.core.verify.VerificationReason
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf

private const val RESOLUTIONS: Int = 8

class VerificationTest : FunSpec({
    test("passes a sound configuration in every profile, scopes and provided values included") {
        val app = module("app") {
            variant(setOf(LIVE, MOCK)) { single<Greeter> { EnglishGreeter() } }
            single { Service(get()) }
            scope(SESSION) { factory { "session" } }
            scope(REQUEST, within = SESSION) {
                provided<Request>()
                scoped { Handler(get(), get()) }
            }
        }

        val verification = verify(setOf(LIVE, MOCK)) { modules(app) }

        val passed = verification.shouldBeInstanceOf<Verification.Passed>()
        passed.isPassed shouldBe true
        passed.profiles shouldBe setOf(LIVE, MOCK)
        passed.resolved shouldBe RESOLUTIONS
        passed.toString() shouldEndWith "resolved=8)"
    }

    test("reports a definition missing in one profile") {
        val app = module("app") {
            variant(LIVE) { single<Greeter> { EnglishGreeter() } }
            single { Service(get()) }
        }

        val failed = verify(setOf(LIVE, MOCK)) { modules(app) }.shouldBeInstanceOf<Verification.Failed>()

        failed.isPassed shouldBe false
        val problem = failed.problems.single()
        problem.profile shouldBe MOCK
        val reason = problem.reason.shouldBeInstanceOf<VerificationReason.Unresolvable>()
        reason.failure.shouldBeInstanceOf<ResolutionFailure.Missing>()
        problem.toString() shouldStartWith "VerificationProblem(mock, Unresolvable(Missing("
        failed.toString() shouldContain "problems=[VerificationProblem(mock"
    }

    test("reports an invalid configuration") {
        val app = module("app") { single { Profile("x") } }

        val failed = verify { modules(app) }.shouldBeInstanceOf<Verification.Failed>()

        val reason = failed.problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Invalid>()
        reason.report.isValid shouldBe false
        reason.toString() shouldBe "Invalid(1 errors)"
    }

    test("reports a failing eager single") {
        val app = module("app") { single<Greeter>(eager = true) { error(BROKEN) } }

        val failed = verify { modules(app) }.shouldBeInstanceOf<Verification.Failed>()

        val reason = failed.problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Unresolvable>()
        reason.failure.shouldBeInstanceOf<ResolutionFailure.Failed>()
    }

    test("reports an instance that fails to close") {
        val app = module("app") { single(close = { _: Greeter -> error(UNCLOSABLE) }) { EnglishGreeter() } }

        val failed = verify { modules(app) }.shouldBeInstanceOf<Verification.Failed>()

        val reason = failed.problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Undisposable>()
        reason.failure.cause.message shouldBe UNCLOSABLE
        reason.toString() shouldStartWith "Undisposable(DisposalFailure("
    }

    test("closes every scope it opens, so that the scope limit holds for one chain at a time") {
        val app = module("app") {
            scope(SESSION) { factory { "session" } }
            scope(PAGE) { factory { "page" } }
            scope(REQUEST) { scoped(close = { _: Greeter -> error(UNCLOSABLE) }) { EnglishGreeter() } }
        }

        val failed = verify {
            scopeLimit = 1
            modules(app)
        }.shouldBeInstanceOf<Verification.Failed>()

        failed.problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Undisposable>()
    }

    test("reports a scope nested deeper than the scope limit") {
        val app = module("app") {
            scope(SESSION) {}
            scope(REQUEST, within = SESSION) { factory { "request" } }
        }

        val failed = verify {
            scopeLimit = 1
            modules(app)
        }.shouldBeInstanceOf<Verification.Failed>()

        val reason = failed.problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Unopenable>()
        reason.scope shouldBe REQUEST
        reason.cause.shouldBeInstanceOf<ScopeLimitException>()
        reason.toString() shouldBe "Unopenable(request, The container already holds 1 open scopes)"
    }

    test("reports an outermost scope that the scope limit refuses") {
        val failed = verify {
            scopeLimit = 1
            modules(module("app") { scope(SESSION) {} })
            extensions(Opening(SESSION))
        }.shouldBeInstanceOf<Verification.Failed>()

        failed.problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Unopenable>()
            .scope shouldBe SESSION
    }

    test("needs a profile") {
        shouldThrow<IllegalArgumentException> { verify(emptySet()) { } }
    }
})
