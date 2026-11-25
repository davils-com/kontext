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

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Runs [block] with this container and closes the container afterwards, also when [block] fails or is cancelled.
 *
 * @param R the result of [block].
 * @param block what to do with the container.
 * @return what [block] returns.
 * @since 1.0.0
 */
public suspend inline fun <R> Container.use(block: (Container) -> R): R {
    try {
        return block(this)
    } finally {
        withContext(NonCancellable) { close() }
    }
}

/**
 * Runs [block] with this scope and closes the scope afterwards, also when [block] fails or is cancelled.
 *
 * @param R the result of [block].
 * @param block what to do with the scope.
 * @return what [block] returns.
 * @since 1.0.0
 */
public suspend inline fun <R> Scope.use(block: (Scope) -> R): R {
    try {
        return block(this)
    } finally {
        withContext(NonCancellable) { close() }
    }
}

/**
 * Opens a scope of [key], runs [block] in it and closes it afterwards, also when [block] fails or is cancelled.
 *
 * @param R the result of [block].
 * @param key the key of the scope.
 * @param id the id of the scope, `null` for none.
 * @param seed provides the values of the scope.
 * @param block what to do in the scope.
 * @return what [block] returns.
 * @since 1.0.0
 */
public suspend inline fun <R> Container.withScope(
    key: ScopeKey,
    id: ScopeId? = null,
    noinline seed: ScopeSeed.() -> Unit = {},
    block: (Scope) -> R,
): R = openScope(key, id, seed).use(block)
