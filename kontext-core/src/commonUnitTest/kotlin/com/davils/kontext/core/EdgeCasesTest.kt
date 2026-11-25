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

import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.definition.scopedOf
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ScopeNestingException
import com.davils.kontext.core.extension.Retention
import com.davils.kontext.core.fixture.EnglishGreeter
import com.davils.kontext.core.fixture.Greeter
import com.davils.kontext.core.fixture.Handler
import com.davils.kontext.core.fixture.Journal
import com.davils.kontext.core.fixture.REQUEST
import com.davils.kontext.core.fixture.Request
import com.davils.kontext.core.fixture.SESSION
import com.davils.kontext.core.fixture.Service
import com.davils.kontext.core.fixture.containerOf
import com.davils.kontext.core.holder.ContainerHolder
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.core.observe.TextLevel
import com.davils.kontext.core.observe.asContainerSink
import com.davils.kontext.core.parameter.ParametersBuilder
import com.davils.kontext.core.verify.Verification
import com.davils.kontext.testing.RecordingContainerSink
import com.davils.kontext.testing.RecordingTextSink
import com.davils.kore.core.arguments.conversion.Converters
import com.davils.kore.core.validation.report.ValidationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.yield
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

private const val PORT: Int = 8080

class EdgeCasesTest : FunSpec({
    test("keys and resolution by key without parameters") {
        val container = containerOf(module("app") { single<Greeter> { EnglishGreeter() } })

        container.resolve(DefinitionKey.of<Greeter>()).shouldBeInstanceOf<EnglishGreeter>()
        container.resolveAsyncOrNull(DefinitionKey.of<Service>()).shouldBeNull()
        DefinitionKey.of<List<*>>() shouldNotBe DefinitionKey.of<List<String>>()
        CloseReport.AlreadyClosed.failures shouldBe emptyList()
        ParametersBuilder().toString() shouldBe "ParametersBuilder(0 values)"
    }

    test("properties fall back only for unknown keys and scopes read them as well") {
        val container = containerOf(module("app") { scope(SESSION) {} }) { properties { values("port" to "$PORT") } }

        container.property("port", Converters.int, 1) shouldBe PORT
        container.openScope(SESSION).property("port") shouldBe "$PORT"
    }

    test("an expired instance without close function and a failing scoped close are handled") {
        val time = TestTimeSource()
        val sink = RecordingContainerSink()
        val lines = RecordingTextSink()
        val context = currentCoroutineContext()
        val container = containerOf(
            module("app") {
                single(retention = Retention.expireAfter(1.seconds)) { Journal() }
                scope(SESSION) {
                    scoped<Greeter>(retention = Retention.expireAfter(1.seconds), close = { error("x") }) {
                        EnglishGreeter()
                    }
                }
            },
        ) {
            timeSource = time
            coroutineContext = context
            this.sink = { event ->
                sink.record(event)
                lines.asContainerSink(TextLevel.Warning).record(event)
            }
        }
        val first = container.get<Journal>()
        val scope = container.openScope(SESSION)
        scope.get<Greeter>()

        time += 2.seconds
        container.get<Journal>() shouldNotBe first
        scope.get<Greeter>()
        yield()

        sink.of<ContainerEvent.DisposalFailed>().size shouldBe 1
        lines.lines shouldContain "container root failed to close ${DefinitionKey.of<Greeter>()}"
        container.close()
    }

    test("loading eagerly skips factories and scoped definitions") {
        val container = containerOf(module("base") { scope(SESSION) {} })
        val journal = Journal()

        container.load(
            module("late") {
                factory { journal.apply { write("factory") } }
                scope(REQUEST) { scoped { Request("scoped") } }
            },
            eager = true,
        )

        journal.entries shouldBe emptyList()
    }

    test("a replacement inside a scope does not replace a definition of the container") {
        shouldThrow<ValidationException> {
            containerOf(module("app") { single<Greeter> { EnglishGreeter() } }) {
                replace(module("fake") { scope(SESSION) { scoped<Greeter> { EnglishGreeter() } } })
            }
        }
    }

    test("scopes nested in scopes are verified statically and a container scope opened within a scope is refused") {
        val nested = module("nested") {
            single { Service(EnglishGreeter()) }
            scope(SESSION) { provided<Request>() }
            scope(REQUEST, within = SESSION) { scopedOf(::Handler) }
        }

        verifyStatically { modules(nested) }.shouldBeInstanceOf<Verification.Passed>()
        val session = containerOf(nested).openScope(SESSION)
        shouldThrow<ScopeNestingException> { session.openScope(SESSION) }
    }

    test("holders describe themselves without a container and scopes open by key alone") {
        ContainerHolder().toString() shouldBe "ContainerHolder(none)"
        val container = containerOf(module("app") { scope(SESSION) { scopedAsync { 1 } } })

        container.withScope(SESSION) { scope -> scope.await<Int>() } shouldBe 1
        shouldThrow<ResolutionException> { container.get<Long>() }
    }

    test("unloading reports in text") {
        val lines = RecordingTextSink()
        val container = containerOf { sink = lines.asContainerSink() }

        container.load(module("late") {}).unload()

        lines.lines shouldContain "container root unloaded [late]"
        ContainerEvent.ModulesUnloaded::class.simpleName shouldBe "ModulesUnloaded"
    }
})
