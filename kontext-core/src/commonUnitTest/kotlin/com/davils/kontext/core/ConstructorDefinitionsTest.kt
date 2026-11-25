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

import com.davils.kontext.core.definition.bind
import com.davils.kontext.core.definition.factoryOf
import com.davils.kontext.core.definition.new
import com.davils.kontext.core.definition.scopedOf
import com.davils.kontext.core.definition.singleOf
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.GermanGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Handler
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.Loop
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.Wide
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.verify.Verification
import com.davils.kontext.core.verify.VerificationReason
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeSameInstanceAs

private const val CHECKED: Int = 26
private const val WIDTH: Int = 22

private val constructed: Module = module("constructed") {
    singleOf(::EnglishGreeter).bind<Greeter>()
    singleOf(::Service)
    singleOf(::Journal)
    single { 1 }
    factoryOf(
        {
                p1: Int,
                p2: Int,
                p3: Int,
                p4: Int,
                p5: Int,
                p6: Int,
                p7: Int,
                p8: Int,
                p9: Int,
                p10: Int,
                p11: Int,
                p12: Int,
                p13: Int,
                p14: Int,
                p15: Int,
                p16: Int,
                p17: Int,
                p18: Int,
                p19: Int,
                p20: Int,
                p21: Int,
                p22: Int,
            ->
            Wide(
                p1 +
                    p2 +
                    p3 +
                    p4 +
                    p5 +
                    p6 +
                    p7 +
                    p8 +
                    p9 +
                    p10 +
                    p11 +
                    p12 +
                    p13 +
                    p14 +
                    p15 +
                    p16 +
                    p17 +
                    p18 +
                    p19 +
                    p20 +
                    p21 +
                    p22,
            )
        },
    )
    scope(REQUEST) {
        provided<Request>()
        scopedOf(::Handler)
        factoryOf({ handler: Handler -> handler.request.id })
    }
}

class ConstructorDefinitionsTest : FunSpec({
    test("constructor definitions resolve every parameter by its type") {
        val container = containerOf(constructed)
        val scope = container.openScope(REQUEST) { provide(Request("r")) }

        container.get<Service>().greeter.greet() shouldBe "hello"
        container.get<Wide>().sum shouldBe WIDTH
        container.get<Wide>() shouldNotBeSameInstanceAs container.get<Wide>()
        scope.get<Handler>().request.id shouldBe "r"
        scope.get<String>() shouldBe "r"
        val loop = scope.new(::Loop)
        loop.handler.request.id shouldBe "r"
        container.new(::Journal).entries shouldBe emptyList()
    }

    test("a static verification follows the declared dependencies without creating anything") {
        val passed = verifyStatically { modules(constructed) }.shouldBeInstanceOf<Verification.Passed>()

        passed.resolved shouldBe CHECKED
    }

    test("a static verification reports missing, ambiguous and circular dependencies") {
        val broken = module("broken") {
            singleOf(::Service)
            singleOf(::Loop)
            single { EnglishGreeter() }.bind<Greeter>()
            single { GermanGreeter() }.bind<Greeter>()
            scope(REQUEST) { scopedOf(::Handler) }
        }
        val cyclic = module("cyclic") {
            singleOf({ number: Long -> number.toInt() })
            singleOf({ number: Int -> number.toLong() })
        }

        val failed = verifyStatically { modules(broken) }.shouldBeInstanceOf<Verification.Failed>()
        val circular = verifyStatically { modules(cyclic) }.shouldBeInstanceOf<Verification.Failed>()
        val invalid = verifyStatically { modules(broken, module("broken") {}) }

        failed.problems.map { problem ->
            (problem.reason as VerificationReason.Unresolvable).failure::class.simpleName
        } shouldBe
            listOf("Ambiguous", "Missing", "Missing")
        circular.problems shouldHaveSize 1
        (circular.problems.single().reason as VerificationReason.Unresolvable)
            .failure
            .shouldBeInstanceOf<ResolutionFailure.Circular>()
        (invalid as Verification.Failed)
            .problems
            .single()
            .reason
            .shouldBeInstanceOf<VerificationReason.Invalid>()
        shouldThrow<IllegalArgumentException> { verifyStatically(emptySet()) {} }
    }
})
