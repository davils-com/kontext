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

package com.davils.kontext.core.internal

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Profile
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontext.core.error.ScopeLimitException
import com.davils.kontext.core.verify.Verification
import com.davils.kontext.core.verify.VerificationProblem
import com.davils.kontext.core.verify.VerificationReason
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

internal class Verifier(private val profile: Profile, private val block: ContainerBuilder.() -> Unit) {
    var resolved: Int = 0
        private set

    suspend fun problems(): List<VerificationProblem> = reasons().map { reason -> VerificationProblem(profile, reason) }

    private suspend fun reasons(): List<VerificationReason> {
        val builder = ContainerBuilder(profile, null)
        builder.block()
        val report = builder.validate()
        if (!report.isValid) return listOf(VerificationReason.Invalid(report))
        return started(builder)
    }

    private suspend fun started(builder: ContainerBuilder): List<VerificationReason> {
        val runtime = try {
            ContainerRuntime(builder.build()).start()
        } catch (refusal: ResolutionException) {
            return listOf(VerificationReason.Unresolvable(refusal.failure))
        }
        val reasons = mutableListOf<VerificationReason>()
        try {
            reasons += resolvedIn(runtime)
        } finally {
            val closed = withContext(NonCancellable) { runtime.close() }
            reasons += closed.failures.map(VerificationReason::Undisposable)
        }
        return reasons
    }

    private suspend fun resolvedIn(runtime: ContainerRuntime): List<VerificationReason> {
        runtime.awaitLoaded()
        val registry = runtime.registry
        val root = registry.of(null).mapNotNull { declaration -> attempt(runtime.context, declaration.key) }
        val scoped = registry.scopes.flatMap { key -> scopeReasons(runtime, key) }
        return unresolvable(root) + scoped
    }

    private suspend fun scopeReasons(runtime: ContainerRuntime, key: ScopeKey): List<VerificationReason> {
        val chain = generateSequence(key) { place -> runtime.registry.parents[place] }
        val keys = chain.toList().asReversed()
        val outermost = try {
            runtime.scopes.open(key = keys.first(), parent = null, seeds = emptyMap(), scopeId = null)
        } catch (refusal: ScopeLimitException) {
            return listOf(VerificationReason.Unopenable(scope = key, cause = refusal))
        }
        val reasons = mutableListOf<VerificationReason>()
        try {
            reasons += chainReasons(runtime = runtime, outermost = outermost, nested = keys.drop(1))
        } finally {
            val closed = withContext(NonCancellable) { outermost.close() }
            reasons += closed.failures.map(VerificationReason::Undisposable)
        }
        return reasons
    }

    private suspend fun chainReasons(
        runtime: ContainerRuntime,
        outermost: ScopeRuntime,
        nested: List<ScopeKey>,
    ): List<VerificationReason> {
        val innermost = try {
            nested.fold(outermost) { parent, place -> parent.openScope(place, null, emptyMap()) }
        } catch (refusal: ScopeLimitException) {
            return listOf(VerificationReason.Unopenable(scope = nested.last(), cause = refusal))
        }
        val declarations = runtime.registry.of(innermost.key)
        return unresolvable(declarations.mapNotNull { declaration -> attempt(innermost.context, declaration.key) })
    }

    private suspend fun attempt(resolver: ResolutionContext, key: DefinitionKey): ResolutionFailure? = try {
        resolver.resolveAsync(key)
        resolved++
        null
    } catch (refusal: ResolutionException) {
        refusal.failure
    }
}

private fun unresolvable(failures: List<ResolutionFailure>): List<VerificationReason> {
    val reported = failures.filterNot { failure -> failure is ResolutionFailure.Unprovided }
    return reported.map(VerificationReason::Unresolvable)
}

internal suspend fun verified(profiles: Set<Profile>, block: ContainerBuilder.() -> Unit): Verification {
    require(profiles.isNotEmpty()) { "A verification names at least one profile" }
    val verifiers = profiles.map { profile -> Verifier(profile, block) }
    val problems = verifiers.flatMap { verifier -> verifier.problems() }
    if (problems.isNotEmpty()) return Verification.Failed(profiles, problems)
    return Verification.Passed(profiles = profiles, resolved = verifiers.sumOf { verifier -> verifier.resolved })
}
