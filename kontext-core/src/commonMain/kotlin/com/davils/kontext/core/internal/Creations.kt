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

import com.davils.kontext.core.container.ContainerConfiguration
import com.davils.kontext.core.error.ResolutionException
import com.davils.kontext.core.error.ResolutionFailure
import com.davils.kontinuity.retry.Retry
import com.davils.kontinuity.retry.RetryConfig
import com.davils.kontinuity.retry.retry
import com.davils.kontinuity.retry.retryConfig

private const val CREATION_RETRY: String = "kontext-creation"

internal class Creations(configuration: ContainerConfiguration) {
    private val retry: Retry? = configuration.creationRetry?.let { base -> retryOf(base, configuration) }

    suspend fun produced(attempt: suspend () -> Any): Any {
        val active = retry ?: return attempt()
        return active.execute { attempt() }
    }

    private companion object {
        fun retryOf(base: RetryConfig, configuration: ContainerConfiguration): Retry {
            val config = retryConfig(base) { failures { ignoreIf { failure -> !failure.isFactoryFailure() } } }
            return retry(name = CREATION_RETRY, config = config, environment = configuration.resilience)
        }

        fun Throwable.isFactoryFailure(): Boolean {
            val failure = (this as? ResolutionException)?.failure as? ResolutionFailure.Failed ?: return false
            return failure.cause !is ResolutionException
        }
    }
}
