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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.core.container

import com.davils.kollect.core.MeterRegistry
import com.davils.kollect.core.trace.Tracer
import com.davils.konfetti.core.EventBus
import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.LazyModule
import com.davils.kontext.core.Module
import com.davils.kontext.core.Profile
import com.davils.kontext.core.definition.Declaration
import com.davils.kontext.core.definition.ScopeDeclaration
import com.davils.kontext.core.extension.ContainerExtension
import com.davils.kontext.core.extension.ResolutionFallback
import com.davils.kontext.core.flatten
import com.davils.kontext.core.internal.AuditInput
import com.davils.kontext.core.internal.ContainerRuntime
import com.davils.kontext.core.internal.auditOf
import com.davils.kontext.core.internal.profileDeclaration
import com.davils.kontext.core.observe.ContainerSink
import com.davils.kontext.core.property.PropertySource
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList
import com.davils.kore.core.identity.UuidV7
import com.davils.kore.core.identity.generator.UuidGenerator
import com.davils.kore.core.validation.Validatable
import com.davils.kore.core.validation.ValidationScope
import com.davils.kore.core.validation.violation.ViolationCode
import kotlin.coroutines.CoroutineContext
import kotlin.time.TimeSource

/**
 * The validated configuration of a container, built by [ContainerBuilder].
 *
 * @param builder the builder the configuration is taken from.
 * @since 1.0.0
 */
public class ContainerConfiguration internal constructor(builder: ContainerBuilder) : Validatable {
    /**
     * The name of the container.
     *
     * @since 1.0.0
     */
    public val name: ContainerName = builder.name

    /**
     * The profile the container runs in.
     *
     * @since 1.0.0
     */
    public val profile: Profile = builder.profile

    /**
     * Generates the ids of the opened scopes.
     *
     * @since 1.0.0
     */
    public val ids: UuidGenerator<UuidV7> = builder.ids

    /**
     * Traces the start and the close of the container.
     *
     * @since 1.0.0
     */
    public val tracer: Tracer = builder.tracer

    /**
     * Where the container records its meters, `null` for none.
     *
     * @since 1.0.0
     */
    public val meters: MeterRegistry? = builder.meters

    /**
     * Where the container publishes its events, `null` for none.
     *
     * @since 1.0.0
     */
    public val events: EventBus? = builder.events

    /**
     * Where the container reports its events synchronously, `null` for none.
     *
     * @since 1.0.0
     */
    public val sink: ContainerSink? = builder.sink

    /**
     * Measures durations.
     *
     * @since 1.0.0
     */
    public val timeSource: TimeSource.WithComparableMarks = builder.timeSource

    /**
     * The coroutine context of the coroutine scope of the container.
     *
     * @since 1.0.0
     */
    public val coroutineContext: CoroutineContext = builder.coroutineContext

    /**
     * The most scopes the container keeps open at once.
     *
     * @since 1.0.0
     */
    public val scopeLimit: Int = builder.scopeLimit

    /**
     * The extensions of the container, in the order of their registration.
     *
     * @since 1.0.0
     */
    public val extensions: List<ContainerExtension> = builder.extensions.immutableList()

    /**
     * The fallbacks of the container, in the order they are asked.
     *
     * @since 1.0.0
     */
    public val fallbacks: List<ResolutionFallback> = builder.fallbacks.immutableList()

    /**
     * The property sources of the container itself, without those of its parent, lowest first.
     *
     * @since 1.0.0
     */
    public val propertySources: List<PropertySource> = builder.propertySources.immutableList()

    /**
     * The modules loaded only after the container started.
     *
     * @since 1.0.0
     */
    public val lazyModules: List<LazyModule> = builder.lazy.immutableList()
    internal val parent: ContainerRuntime? = builder.parent

    /**
     * The modules of the container, the included ones each once.
     *
     * @since 1.0.0
     */
    public val modules: List<Module> = flatten(builder.declared).immutableList()

    /**
     * The modules that replace definitions of [modules].
     *
     * @since 1.0.0
     */
    public val replacements: List<Module> = flatten(builder.replaced).immutableList()

    override fun ValidationScope.validate() {
        if (scopeLimit < 1) {
            fail(
                property = ::scopeLimit,
                message = "must be at least 1",
                code = ViolationCode.OUT_OF_RANGE,
            )
        }
        val findings = auditOf(auditInput())
        findings.forEach { finding ->
            val property = ::replacements.takeIf { finding.isReplacement } ?: ::modules
            fail(property = property, message = finding.message, code = finding.code)
        }
    }

    internal fun declared(): List<Declaration> = modules.flatMap { module -> module.declarations }

    internal fun replacing(): List<Declaration> = replacements.flatMap { module -> module.declarations }

    internal fun scopeDeclarations(): List<ScopeDeclaration> {
        val declared = (modules + replacements).flatMap { module -> module.scopes }
        return declared.filter { scope -> scope.profiles.isEmpty() || profile in scope.profiles }
    }

    internal fun declarations(): List<Declaration> {
        val replacing = replacing().filter { declaration -> declaration.appliesTo(profile) }
        val kept = declared().filterNot { declaration -> replacing.any { other -> other.conflictsWith(declaration) } }
        val builtIn = listOfNotNull(profileDeclaration(profile).takeIf { parent?.profile != profile })
        return (builtIn + kept + replacing).filter { declaration -> declaration.appliesTo(profile) }
    }

    internal fun auditInput(): AuditInput = AuditInput(
        modules = modules,
        replacements = replacements,
        declared = declared(),
        replacing = replacing(),
        scopes = scopeDeclarations(),
        effective = declarations(),
        parent = parent,
    )

    override fun toString(): String = "ContainerConfiguration($name, $profile, modules=${modules.size})"
}
