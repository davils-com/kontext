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

@file:OptIn(ExperimentalAtomicApi::class)

package com.davils.kontext.core.internal

import com.davils.kontext.core.Container
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Profile
import com.davils.kontext.core.Resolver
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.container.ContainerConfiguration
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.container.DisposalFailure
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.Production
import com.davils.kontext.core.error.ContainerClosedException
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.extension.ContainerExtension
import com.davils.kontext.core.parameter.Parameters
import com.davils.kore.core.failure.attempted
import com.davils.kore.core.failure.contained
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.time.ComparableTimeMark
import kotlin.time.TimeSource

private const val START_SPAN: String = "kontext.start"
private const val CLOSE_SPAN: String = "kontext.close"

internal class ContainerRuntime(val configuration: ContainerConfiguration) : Host(platformGuard()) {
    private val stateFlow = MutableStateFlow(ContainerState.Starting)
    private val children = mutableListOf<ContainerRuntime>()
    private val closeHandlers = mutableListOf<() -> Unit>()
    private val ready = CompletableDeferred<Unit>()

    val loader: ModuleLoader = ModuleLoader(
        this,
        Installation(
            modules = configuration.modules,
            replacements = configuration.replacements,
            declarations = configuration.declarations(),
            scopes = configuration.scopeDeclarations(),
        ),
    )
    val scopes: ScopeTable = ScopeTable(this)

    val name: ContainerName get() = configuration.name
    val profile: Profile get() = configuration.profile
    val parent: ContainerRuntime? = configuration.parent
    val job: CompletableJob = SupervisorJob(jobOf(parent) ?: configuration.coroutineContext[Job])
    override val coroutineScope: CoroutineScope = CoroutineScope(configuration.coroutineContext + job)
    val telemetry: ContainerTelemetry = ContainerTelemetry(configuration)
    val properties: Properties = Properties(configuration.propertySources).layered(propertiesOf(parent))
    val state: StateFlow<ContainerState> = stateFlow.asStateFlow()
    val context: ResolutionContext =
        ResolutionContext(runtime = this, scope = null, path = emptyList(), parameters = Parameters.EMPTY)
    val registry: Registry get() = loader.registry
    val isResolvable: Boolean get() = stateFlow.value != ContainerState.Closed
    val isLoading: Boolean get() = !ready.isCompleted
    val facade: Container get() = Container(this)

    override val scope: ScopeRuntime? get() = null
    override val timeSource: TimeSource.WithComparableMarks get() = configuration.timeSource

    override fun seed(key: DefinitionKey): Any? = null

    override fun disposalFailed(key: DefinitionKey) {
        telemetry.disposalFailed(key)
    }

    suspend fun start(): ContainerRuntime {
        val mark = timeSource.markNow()
        val started = attempted { telemetry.traced(START_SPAN) { createEager(registry.eager) } }
        started.exceptionOrNull()?.let { failure -> abort(failure) }
        stateFlow.value = ContainerState.Running
        loadLazily()
        notifyExtensions(ExtensionPhase.Started) { extension -> extension.onStarted(facade) }
        telemetry.started(definitions = registry.keys.size, elapsed = mark.elapsedNow())
        return this
    }

    suspend fun awaitLoaded() {
        contained { ready.await() }
    }

    suspend fun awaitReady() {
        ready.await()
    }

    fun locate(scope: ScopeRuntime?, key: DefinitionKey, path: List<DefinitionKey>): Located? {
        val snapshot = registry
        val order = scope?.searchOrder().orEmpty()
        val scoped = order.firstNotNullOfOrNull { link ->
            located(snapshot = snapshot, owner = link.key, host = link, key = key, path = path)
        }
        val root = located(snapshot = snapshot, owner = null, host = this, key = key, path = path)
        return scoped ?: root ?: parent?.locate(null, key, path)
    }

    fun locateAll(scope: ScopeRuntime?, type: KType): List<Located> {
        val snapshot = registry
        val order = scope?.searchOrder().orEmpty()
        val scoped = order.flatMap { link -> snapshot.all(link.key, type).map { found -> Located(this, link, found) } }
        val root = snapshot.all(null, type).map { found -> Located(this, this, found) }
        return scoped + root + parent?.locateAll(null, type).orEmpty()
    }

    fun fallback(key: DefinitionKey, resolver: Resolver, path: List<DefinitionKey>): Any? {
        val supplied = contained {
            configuration.fallbacks.firstNotNullOfOrNull { fallback ->
                fallback.resolve(
                    key,
                    resolver,
                )
            }
        }
        val instance = supplied.getOrElse { cause -> throw failed(ResolutionFailure.Failed(key, cause, path)) }
        val isMismatched = instance != null && !fits(instance, key)
        if (isMismatched) throw failed(ResolutionFailure.Mismatched(key, path))
        return instance
    }

    fun produce(
        located: Located,
        trail: List<DefinitionKey>,
        parameters: Parameters,
        create: DefinitionContext.() -> Any,
    ): Any {
        val context = ResolutionContext(
            runtime = this,
            scope = located.host.scope,
            path = trail,
            parameters = parameters,
        )
        val mark = timeSource.markNow()
        val produced = contained { context.create() }
        val instance = produced.getOrElse { cause -> throw wrapped(located.declaration.key, cause, trail.dropLast(1)) }
        return registered(located, instance, mark)
    }

    suspend fun produceAsync(
        located: Located,
        trail: List<DefinitionKey>,
        parameters: Parameters,
        create: suspend DefinitionContext.() -> Any,
    ): Any {
        val context = ResolutionContext(
            runtime = this,
            scope = located.host.scope,
            path = trail,
            parameters = parameters,
        )
        val mark = timeSource.markNow()
        val produced = attempted { context.create() }
        val instance = produced.getOrElse { cause -> throw wrapped(located.declaration.key, cause, trail.dropLast(1)) }
        return registered(located, instance, mark)
    }

    fun failed(failure: ResolutionFailure): ResolutionException {
        telemetry.failed(failure)
        return ResolutionException(failure)
    }

    suspend fun child(profile: Profile, block: ContainerBuilder.() -> Unit): ContainerRuntime {
        ensureRunning()
        val builder = ContainerBuilder(profile, this)
        builder.block()
        val child = ContainerRuntime(builder.build()).start()
        val open = locked {
            children += child
            children.size
        }
        telemetry.childOpened(child = child.name, open = open)
        return child
    }

    fun childrenSnapshot(): List<ContainerRuntime> = locked { children.toList() }

    fun onClosed(handler: () -> Unit) {
        val isAlreadyClosed = locked {
            val isClosed = stateFlow.value == ContainerState.Closed
            if (!isClosed) closeHandlers += handler
            isClosed
        }
        if (isAlreadyClosed) handler()
    }

    suspend fun close(): CloseReport {
        val isClosing = stateFlow.compareAndSet(expect = ContainerState.Running, update = ContainerState.Closing)
        if (!isClosing) return CloseReport.AlreadyClosed
        telemetry.closing()
        val mark = timeSource.markNow()
        val failures = telemetry.traced(CLOSE_SPAN) { dispose() }
        val handlers = locked {
            stateFlow.value = ContainerState.Closed
            closeHandlers.toList().also { closeHandlers.clear() }
        }
        telemetry.closed(failures = failures.size, elapsed = mark.elapsedNow())
        parent?.release(this)
        handlers.forEach { handler -> contained { handler() } }
        return CloseReport.Closed(failures)
    }

    private fun registered(located: Located, instance: Any, mark: ComparableTimeMark): Any {
        val declaration = located.declaration
        telemetry.created(key = declaration.key, lifetime = declaration.lifetime, elapsed = mark.elapsedNow())
        declaration.dispose?.let { dispose -> located.host.disposals.add(declaration, instance) { dispose(instance) } }
        return instance
    }

    suspend fun createEager(declarations: List<Declaration>) {
        val (suspending, blocking) = declarations.partition { declaration ->
            declaration.production is Production.Async
        }
        blocking.forEach { declaration ->
            locatedHere(
                declaration,
            ).instance(emptyList(), declaration.key, Parameters.EMPTY)
        }
        coroutineScope {
            val creations = suspending.map { declaration ->
                async { locatedHere(declaration).awaitInstance(emptyList(), declaration.key, Parameters.EMPTY) }
            }
            creations.awaitAll()
        }
    }

    private fun locatedHere(declaration: Declaration): Located = Located(
        runtime = this,
        host = this,
        declaration = declaration,
    )

    private fun loadLazily() {
        val lazy = configuration.lazyModules
        if (lazy.isEmpty()) {
            ready.complete(Unit)
            return
        }
        coroutineScope.launch {
            val loaded = attempted { loader.load(lazy.map { module -> module.build() }, isEager = false) }
            loaded.fold(
                onSuccess = { ready.complete(Unit) },
                onFailure = { failure -> ready.completeExceptionally(failure) },
            )
        }
    }

    private suspend fun abort(failure: Throwable): Nothing {
        stateFlow.value = ContainerState.Closing
        job.cancel()
        disposals.drain().forEach { disposal -> failure.addSuppressed(disposal.cause) }
        stateFlow.value = ContainerState.Closed
        ready.completeExceptionally(ContainerClosedException(name))
        throw failure
    }

    private suspend fun dispose(): List<DisposalFailure> {
        notifyClosing()
        val nested = locked { children.toList() }
        val opened = scopes.snapshot()
        val childFailures = nested.asReversed().flatMap { child -> child.close().failures }
        val scopeFailures = opened.asReversed().flatMap { scope -> scope.close().failures }
        job.cancel()
        ready.completeExceptionally(ContainerClosedException(name))
        return childFailures + scopeFailures + disposals.drain()
    }

    private suspend fun notifyClosing() {
        configuration.extensions.asReversed().forEach { extension ->
            val outcome = attempted { extension.onClosing(facade) }
            if (outcome.isFailure) {
                telemetry.extensionFailed(
                    extension = extension.name,
                    phase = ExtensionPhase.Closing.label,
                )
            }
        }
    }

    inline fun notifyExtensions(phase: ExtensionPhase, hook: (ContainerExtension) -> Unit) {
        configuration.extensions.forEach { extension ->
            val outcome = contained { hook(extension) }
            if (outcome.isFailure) telemetry.extensionFailed(extension = extension.name, phase = phase.label)
        }
    }

    private fun release(child: ContainerRuntime) {
        val open = locked {
            children.remove(child)
            children.size
        }
        telemetry.childClosed(open)
    }

    fun ensureRunning() {
        if (stateFlow.value == ContainerState.Running) return
        throw ContainerClosedException(name)
    }
}

private fun jobOf(runtime: ContainerRuntime?): Job? = runtime?.job

private fun propertiesOf(runtime: ContainerRuntime?): Properties? = runtime?.properties

private fun fits(instance: Any, key: DefinitionKey): Boolean {
    val classifier = key.type.classifier as? KClass<*>
    return classifier?.isInstance(instance) != false
}

private fun ContainerRuntime.wrapped(key: DefinitionKey, cause: Throwable, path: List<DefinitionKey>): Throwable {
    if (cause is ResolutionException) return cause
    return failed(ResolutionFailure.Failed(key = key, cause = cause, path = path))
}

private fun ambiguity(key: DefinitionKey, candidates: List<Declaration>, path: List<DefinitionKey>): ResolutionFailure =
    ResolutionFailure.Ambiguous(key = key, candidates = candidates.map { candidate -> candidate.key }, path = path)

private fun ContainerRuntime.located(
    snapshot: Registry,
    owner: ScopeKey?,
    host: Host,
    key: DefinitionKey,
    path: List<DefinitionKey>,
): Located? = when (val match = snapshot.match(owner, key)) {
    null -> null
    is Match.Found -> Located(runtime = this, host = host, declaration = match.declaration)
    is Match.Many -> throw failed(ambiguity(key = key, candidates = match.candidates, path = path))
}
