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

import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList

/**
 * The outcome of closing a container or a scope.
 *
 * @since 1.0.0
 */
public sealed interface CloseReport {
    /**
     * Whether everything closed without a failure.
     *
     * @since 1.0.0
     */
    public val isClean: Boolean

    /**
     * The instances that failed to close, empty if none did or if nothing was closed by this call.
     *
     * @since 1.0.0
     */
    public val failures: List<DisposalFailure>

    /**
     * This call closed it. Every instance was closed, children and scopes first and then in the reverse order of
     * creation, even those after a failing one.
     *
     * @since 1.0.0
     */
    public class Closed internal constructor(failures: List<DisposalFailure>) : CloseReport {
        override val failures: List<DisposalFailure> = failures.immutableList()

        override val isClean: Boolean get() = failures.isEmpty()

        override fun toString(): String = "Closed(failures=$failures)"
    }

    /**
     * An earlier call closed it, or is closing it.
     *
     * @since 1.0.0
     */
    public object AlreadyClosed : CloseReport {
        override val isClean: Boolean get() = true

        override val failures: List<DisposalFailure> get() = emptyList()

        override fun toString(): String = "AlreadyClosed"
    }
}
