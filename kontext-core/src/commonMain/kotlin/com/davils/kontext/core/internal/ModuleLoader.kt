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

import com.davils.kontext.core.Module
import com.davils.kontext.core.container.CloseReport
import com.davils.kontext.core.container.DisposalFailure
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.flatten
import com.davils.kore.core.failure.attempted
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private const val DECLARED_PREFIX: String = "kontext.declared.d"

internal class ModuleLoader(private val runtime: ContainerRuntime, private val initial: Installation) {
    private val installations = mutableListOf(initial)
    private val registryRef = AtomicReference(registryOf(installations))
    private val lock = Mutex()
    private val declaredCount = AtomicInt(0)

    val registry: Registry get() = registryRef.load()

    suspend fun load(modules: List<Module>, isEager: Boolean): Installation {
        val installation = lock.withLock { install(modules) }
        val eager = installation.declarations.filter { declaration -> isCreatedOnLoad(declaration, isEager) }
        val created = attempted { runtime.createEager(eager) }
        created.exceptionOrNull()?.let { failure ->
            unload(installation)
            throw failure
        }
        runtime.telemetry.modulesLoaded(installation.modules.map { module -> module.name })
        runtime.notifyExtensions(ExtensionPhase.Loaded) { extension ->
            extension.onModulesLoaded(runtime.facade, installation.modules)
        }
        return installation
    }

    suspend fun declare(module: (String) -> Module): Installation {
        val name = DECLARED_PREFIX + declaredCount.addAndFetch(1)
        return load(listOf(module(name)), isEager = true)
    }

    suspend fun unload(installation: Installation): CloseReport {
        val isRemoved = lock.withLock {
            val isFound = installations.remove(installation)
            if (isFound) registryRef.store(registryOf(installations))
            isFound
        }
        if (!isRemoved) return CloseReport.AlreadyClosed
        val failures = released(installation)
        runtime.telemetry.modulesUnloaded(installation.modules.map { module -> module.name })
        runtime.notifyExtensions(ExtensionPhase.Unloaded) { extension ->
            extension.onModulesUnloaded(runtime.facade, installation.modules)
        }
        return CloseReport.Closed(failures)
    }

    private fun install(modules: List<Module>): Installation {
        runtime.ensureRunning()
        val known = installations.flatMap { installed -> installed.modules }
        val fresh = flatten(modules).filterNot { module -> known.any { installed -> installed === module } }
        val candidates = fresh.flatMap { module -> module.declarations }
        val declared = candidates.filter { declaration -> declaration.appliesTo(runtime.profile) }
        val installed = installations.flatMap { installation -> installation.declarations }
        val installation = Installation(
            modules = fresh,
            replacements = emptyList(),
            declarations = declared,
            scopes = fresh.flatMap { module -> module.scopes },
        )
        val input = AuditInput(
            modules = known + fresh,
            replacements = initial.replacements,
            declared = installed.filter(::isDeclared) + declared,
            replacing = emptyList(),
            scopes = (installations + installation).flatMap { entry -> entry.scopes },
            effective = installed + declared,
            parent = runtime.parent,
            children = runtime.childrenSnapshot(),
        )
        LoadBuilder(input).build()
        installations += installation
        registryRef.store(registryOf(installations))
        return installation
    }

    private suspend fun released(installation: Installation): List<DisposalFailure> {
        val removed = installation.declarations.toSet()
        val scopes = registry.scopes
        val orphaned = runtime.scopes.snapshot().filter { opened -> opened.key !in scopes }
        val scopeFailures = orphaned.asReversed().flatMap { opened -> opened.close().failures }
        val hosts = listOf<Host>(runtime) + runtime.scopes.snapshot()
        hosts.forEach { host -> host.forget(removed) }
        return scopeFailures + hosts.flatMap { host -> host.disposals.drain { declaration -> declaration in removed } }
    }
}

private fun isCreatedOnLoad(declaration: Declaration, isEager: Boolean): Boolean {
    val isSingle = declaration.owner == null && declaration.lifetime == Lifetime.Single
    return isSingle && (declaration.isEager || isEager)
}

private fun isDeclared(declaration: Declaration): Boolean = declaration.key != profileKey

private fun registryOf(installations: List<Installation>): Registry = Registry(
    declarations = installations.flatMap { installation -> installation.declarations },
    scopeDeclarations = installations.flatMap { installation -> installation.scopes },
)
