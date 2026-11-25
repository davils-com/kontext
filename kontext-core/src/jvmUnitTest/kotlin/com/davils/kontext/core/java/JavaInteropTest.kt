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

package com.davils.kontext.core.java

import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Profile
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.container
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.module
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import java.util.function.Function

private const val HELLO: String = "hello"
private val FIRST: Qualifier = Qualifier("first")
private val SECOND: Qualifier = Qualifier("second")

class JavaInteropTest : FunSpec({
    test("a class finds the definition of the same type declared from Kotlin") {
        val started = container { modules(module("app") { single<Greeter> { EnglishGreeter() } }) }

        JavaResolvers.get(started, Greeter::class.java).greet() shouldBe HELLO
        JavaResolvers.getOrNull(started, Service::class.java).shouldBeNull()
        JavaResolvers.getOrNull(started, Greeter::class.java)?.greet() shouldBe HELLO
        JavaResolvers.inject(started, Greeter::class.java).get() shouldBeSameInstanceAs started.get<Greeter>()
        JavaResolvers.keyOf(Greeter::class.java) shouldBe DefinitionKey.of<Greeter>()
        JavaResolvers.keyOf(Greeter::class.java, Qualifier("a")) shouldNotBe DefinitionKey.of<Greeter>()
        JavaResolvers.keyOf(Greeter::class.java).toString() shouldBe Greeter::class.java.name
        started.close()
    }

    test("modules declared from Java resolve from Kotlin") {
        val declared = JavaModules.module("java") { builder ->
            builder.single(EnglishGreeter::class.java, create = Function { EnglishGreeter() })
            builder.bind(EnglishGreeter::class.java, Greeter::class.java)
            builder.factory(Service::class.java, create = Function { context -> Service(context.get()) })
            builder.toString() shouldBe "JavaModuleBuilder(java)"
        }
        val started = JavaModules.start(Profile.DEFAULT) { builder -> builder.modules(declared) }

        started.get<Greeter>().greet() shouldBe HELLO
        started.get<Service>() shouldNotBeSameInstanceAs started.get<Service>()
        shouldThrow<IllegalArgumentException> {
            JavaModules.module("broken") { builder -> builder.bind(Service::class.java, Greeter::class.java) }
        }
        JavaModules.close(started).isClean shouldBe true
    }

    test("a binding targets the definition of its class and qualifier and only a supertype") {
        val first = EnglishGreeter()
        val second = EnglishGreeter()
        val declared = JavaModules.module("java") { builder ->
            builder.single(EnglishGreeter::class.java, FIRST, Function { first })
            builder.single(EnglishGreeter::class.java, SECOND, Function { second })
            builder.bind(EnglishGreeter::class.java, Greeter::class.java, SECOND)
            builder.bind(
                type = EnglishGreeter::class.java,
                `as` = Greeter::class.java,
                qualifier = FIRST,
                asQualifier = FIRST,
            )
            builder.single(Service::class.java, create = Function { context -> Service(context.get()) })
            shouldThrow<IllegalArgumentException> { builder.bind(EnglishGreeter::class.java, Greeter::class.java) }
            shouldThrow<IllegalArgumentException> { builder.bind(Service::class.java, Greeter::class.java) }
        }
        val started = container { modules(declared) }

        started.get<Greeter>() shouldBeSameInstanceAs second
        started.get<Greeter>(FIRST) shouldBeSameInstanceAs first
        started.close()
    }

    test("an instance of another class than its definition fails the resolution") {
        @Suppress("UNCHECKED_CAST")
        val raw = Function<DefinitionContext, Any> { "not a greeter" } as Function<DefinitionContext, Greeter>
        val declared = JavaModules.module("raw") { builder -> builder.factory(Greeter::class.java, create = raw) }
        val started = container { modules(declared) }

        val failure = shouldThrow<ResolutionException> { started.get<Greeter>() }.failure
        failure
            .shouldBeInstanceOf<ResolutionFailure.Failed>()
            .cause
            .shouldBeInstanceOf<ClassCastException>()
        started.close()
    }

    test("a class without type arguments is not the same type as a generic one") {
        JavaResolvers.keyOf(List::class.java) shouldNotBe DefinitionKey.of<List<String>>()
        DefinitionKey.of<List<String>>() shouldNotBe JavaResolvers.keyOf(List::class.java)
    }
})
