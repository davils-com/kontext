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

package com.davils.kontext.testing

import com.davils.kontext.core.Container
import com.davils.kontext.core.Profile
import com.davils.kontext.core.container
import com.davils.kontext.core.use
import com.davils.kore.testing.SequentialUuidV7Generator

/**
 * Starts a container for a test in [profile], with ids of scopes that count up from the epoch, so that they are the
 * same in every run.
 *
 * The ids are predictable by design, so a test container is for tests only and never runs in production, where the
 * ids of scopes must not be guessable.
 *
 * ```kotlin
 * val container = testContainer {
 *     modules(service)
 *     replace(module("fake.client") { single<Client> { FakeClient() } })
 * }
 * ```
 *
 * @param profile the profile, [TestProfiles.MOCK] unless named.
 * @param block configures the container.
 * @return the running container, which the test closes.
 * @throws com.davils.kore.core.validation.report.ValidationException if the configuration is refused.
 * @since 1.0.0
 */
public suspend fun testContainer(
    profile: Profile = TestProfiles.MOCK,
    block: TestContainerBuilder.() -> Unit,
): Container {
    val builder = TestContainerBuilder()
    builder.block()
    return container(profile) {
        ids = SequentialUuidV7Generator()
        builder.declared.forEach { declared -> modules(declared) }
        builder.replaced.forEach { replaced -> replace(replaced) }
        builder.configurations.forEach { configuration -> configuration() }
    }
}

/**
 * Starts a test container like [testContainer], runs [block] with it and closes it afterwards, also when [block]
 * fails, so that no test leaves a container open.
 *
 * @param R the result of [block].
 * @param profile the profile, [TestProfiles.MOCK] unless named.
 * @param configure configures the container.
 * @param block the test.
 * @return what [block] returns.
 * @since 1.0.0
 */
public suspend fun <R> withTestContainer(
    profile: Profile = TestProfiles.MOCK,
    configure: TestContainerBuilder.() -> Unit,
    block: suspend (Container) -> R,
): R = testContainer(profile, configure).use { container -> block(container) }
