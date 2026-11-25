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

package com.davils.kontext.shutdown

import com.davils.kontext.core.ContainerName
import com.davils.kontext.core.container.DisposalFailure
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.immutableList

/**
 * Thrown by the shutdown task of a container when instances of the container failed to close, so that the shutdown
 * reports the task as failed.
 *
 * @property container the name of the container.
 * @since 1.0.0
 */
public class ContainerCloseException internal constructor(
    public val container: ContainerName,
    failures: List<DisposalFailure>,
) : IllegalStateException("${failures.size} instances of the container $container failed to close") {
    /**
     * The instances that failed to close.
     *
     * @since 1.0.0
     */
    public val failures: List<DisposalFailure> = failures.immutableList()
}
