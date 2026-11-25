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

package com.davils.kontext.core.verify

import com.davils.kontext.core.Profile
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList
import com.davils.kore.core.collection.immutableSet

/**
 * The outcome of [com.davils.kontext.core.verify]: whether every definition of every profile resolves and closes.
 *
 * @since 1.0.0
 */
public sealed interface Verification {
    /**
     * The verified profiles.
     *
     * @since 1.0.0
     */
    public val profiles: Set<Profile>

    /**
     * Whether the verification found no problem.
     *
     * @since 1.0.0
     */
    public val isPassed: Boolean

    /**
     * Every definition resolved and closed in every profile.
     *
     * @property resolved the resolutions made across all profiles.
     * @since 1.0.0
     */
    public class Passed internal constructor(profiles: Set<Profile>, public val resolved: Int) : Verification {
        override val profiles: Set<Profile> = profiles.immutableSet()

        override val isPassed: Boolean get() = true

        override fun toString(): String = "Passed($profiles, resolved=$resolved)"
    }

    /**
     * At least one definition is invalid, does not resolve or does not close.
     *
     * @since 1.0.0
     */
    public class Failed internal constructor(profiles: Set<Profile>, problems: List<VerificationProblem>) :
        Verification {
        override val profiles: Set<Profile> = profiles.immutableSet()

        /**
         * The problems found, by profile in the order of [profiles].
         *
         * @since 1.0.0
         */
        public val problems: List<VerificationProblem> = problems.immutableList()

        override val isPassed: Boolean get() = false

        override fun toString(): String = "Failed($profiles, problems=$problems)"
    }
}
