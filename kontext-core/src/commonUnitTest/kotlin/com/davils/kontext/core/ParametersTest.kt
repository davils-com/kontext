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
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Named
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.parameter.ParameterException
import com.davils.kontext.core.parameter.Parameters
import com.davils.kontext.core.parameter.parametersOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf

private const val NAME: String = "ada"
private const val COUNT: Int = 3

private val app: Module = module("app") {
    single<Greeter> { EnglishGreeter() }
    factory { Service(get()) }
    factory { Named(parameters.get(), get()) }
    single { parameters.size }
}

class ParametersTest : FunSpec({
    context("values") {
        test("are found by type, by position and by destructuring") {
            val parameters = parametersOf(NAME, COUNT)
            val (name: String, count: Int) = parameters

            parameters.get<String>() shouldBe NAME
            parameters.get<Int>(index = 1) shouldBe COUNT
            name shouldBe NAME
            count shouldBe COUNT
            parameters.getOrNull<Long>().shouldBeNull()
            parameters.size shouldBe 2
            parameters.isEmpty shouldBe false
            Parameters.EMPTY.isEmpty shouldBe true
        }

        test("added with a type match only that type") {
            val parameters = parametersOf { add<CharSequence>(NAME) }

            parameters.get<CharSequence>() shouldBe NAME
            parameters.getOrNull<String>().shouldBeNull()
        }

        test("refuse a missing, an ambiguous and a mistyped value without showing it") {
            val parameters = parametersOf(NAME, "other")

            shouldThrow<ParameterException> { parameters.get<String>() }.message shouldBe "2 values have type String"
            shouldThrow<ParameterException> { parameters.get<Int>() }.message shouldBe "There is no value of type Int"
            shouldThrow<ParameterException> { parameters.get<Int>(index = 0) }.message shouldBe
                "The value at 0 is not of type Int"
            shouldThrow<ParameterException> { parameters.get<String>(index = 2) }.message shouldBe
                "There is no value at 2 of 2"
            shouldThrow<ParameterException> { parameters.component3<String>() }
            shouldThrow<ParameterException> { parameters.component4<String>() }
            shouldThrow<ParameterException> { parameters.component5<String>() }
        }

        test("are bounded and compare by value") {
            shouldThrow<IllegalArgumentException> {
                parametersOf { repeat(ContainerLimits.MAX_PARAMETERS + 1) { index -> add(index) } }
            }
            parametersOf(NAME) shouldBe parametersOf(NAME)
            parametersOf(NAME).hashCode() shouldBe parametersOf(NAME).hashCode()
            parametersOf(NAME) shouldNotBe parametersOf("other")
            parametersOf(NAME).equals(NAME) shouldBe false
            parametersOf(NAME).toString() shouldBe "Parameters(1 values)"
        }
    }

    context("resolution") {
        test("hands the parameters to the requested factory only") {
            val container = containerOf(app)

            val named = container.get<Named>(parameters = parametersOf(NAME))

            named.name shouldBe NAME
            named.service.greeter.greet() shouldBe "hello"
        }

        test("a single refuses parameters") {
            val container = containerOf(app)

            val failure = shouldThrow<ResolutionException> {
                container.get<Int>(
                    parameters = parametersOf(NAME),
                )
            }.failure

            failure.shouldBeInstanceOf<ResolutionFailure.ParametersRefused>()
            failure.reason shouldBe "parameters-refused"
            failure.toString() shouldBe "ParametersRefused(${failure.key}, path=[])"
            container.get<Int>() shouldBe 0
        }

        test("a factory that asks for a missing parameter fails with it as the cause") {
            val container = containerOf(app)

            val exception = shouldThrow<ResolutionException> { container.get<Named>() }

            exception.cause.shouldBeInstanceOf<ParameterException>()
        }
    }
})
