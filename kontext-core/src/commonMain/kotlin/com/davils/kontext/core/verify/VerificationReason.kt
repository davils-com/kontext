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

package com.davils.kontext.core.verify

import com.davils.kontext.core.container.DisposalFailure
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kore.core.validation.report.ValidationReport

/**
 * Why a verification found a problem.
 *
 * @since 1.0.0
 */
public sealed interface VerificationReason {
    /**
     * The configuration of the container is refused.
     *
     * @property report the violations.
     * @since 1.0.0
     */
    public class Invalid internal constructor(public val report: ValidationReport) : VerificationReason {
        override fun toString(): String = "Invalid(${report.errors.size} errors)"
    }

    /**
     * A definition does not resolve.
     *
     * @property failure why it does not resolve.
     * @since 1.0.0
     */
    public class Unresolvable internal constructor(public val failure: ResolutionFailure) : VerificationReason {
        override fun toString(): String = "Unresolvable($failure)"
    }

    /**
     * An instance failed to close.
     *
     * @property failure the failure of its close function.
     * @since 1.0.0
     */
    public class Undisposable internal constructor(public val failure: DisposalFailure) : VerificationReason {
        override fun toString(): String = "Undisposable($failure)"
    }
}
