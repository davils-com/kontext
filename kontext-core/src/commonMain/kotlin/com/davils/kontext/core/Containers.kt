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

import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kontext.core.internal.ContainerRuntime

/**
 * Builds, validates and starts a container in [profile].
 *
 * ```kotlin
 * val container = container(Profile("live")) {
 *     name = ContainerName("app")
 *     modules(http, storage)
 * }
 * val client = container.get<HttpClient>()
 * ```
 *
 * @param profile the profile the container runs in.
 * @param block configures the container.
 * @return the running container, which the caller closes.
 * @throws com.davils.kore.core.validation.report.ValidationException with every violation if the configuration is
 *   refused, such as for a duplicate definition.
 * @throws com.davils.kontext.core.error.ResolutionException if an eager single fails; what was created is closed.
 * @since 1.0.0
 */
public suspend fun container(profile: Profile = Profile.DEFAULT, block: ContainerBuilder.() -> Unit): Container {
    val builder = ContainerBuilder(profile, null)
    builder.block()
    return Container(ContainerRuntime(builder.build()).start())
}
