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

import com.davils.kontext.core.Profile
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.verify.Verification
import com.davils.kontext.core.verify.VerificationProblem
import com.davils.kontext.core.verify.VerificationReason

internal class StaticVerifier(private val profile: Profile, private val block: ContainerBuilder.() -> Unit) {
    var checked: Int = 0
        private set

    fun problems(): List<VerificationProblem> = reasons().map { reason -> VerificationProblem(profile, reason) }

    private fun reasons(): List<VerificationReason> {
        val builder = ContainerBuilder(profile, null)
        builder.block()
        val report = builder.validate()
        if (!report.isValid) return listOf(VerificationReason.Invalid(report))
        val configuration = builder.build()
        val registry = Registry(configuration.declarations(), configuration.scopeDeclarations())
        val graph = StaticGraph(registry)
        val missing = registry.declarations.flatMap { declaration -> graph.unresolved(declaration) }
        checked = graph.checked
        return (missing + graph.cycles()).map(VerificationReason::Unresolvable)
    }
}

internal fun verifiedStatically(profiles: Set<Profile>, block: ContainerBuilder.() -> Unit): Verification {
    require(profiles.isNotEmpty()) { "A verification names at least one profile" }
    val verifiers = profiles.map { profile -> StaticVerifier(profile, block) }
    val problems = verifiers.flatMap { verifier -> verifier.problems() }
    if (problems.isNotEmpty()) return Verification.Failed(profiles, problems)
    return Verification.Passed(profiles = profiles, resolved = verifiers.sumOf { verifier -> verifier.checked })
}
