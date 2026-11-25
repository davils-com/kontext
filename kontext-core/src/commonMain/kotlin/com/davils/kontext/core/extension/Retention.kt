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

package com.davils.kontext.core.extension

import kotlin.time.Duration

/**
 * Decides how long a single or a scoped instance is reused, such as a client with credentials that expire.
 *
 * When a resolution finds an instance its retention no longer keeps, the container creates a new one and closes the
 * old one with the close function of the definition, asynchronously in the coroutine scope of its holder. A holder
 * of the old instance therefore must not keep it beyond the retention. [keeps] runs under the lock of the holder and
 * must return quickly; a retention that throws replaces the instance, so that a broken retention never keeps an
 * expired credential.
 *
 * @since 1.0.0
 */
public fun interface Retention {
    /**
     * Returns whether an instance created [age] ago is still reused.
     *
     * @param age the time since the instance was created.
     * @return `true` to reuse it, `false` to replace it.
     * @since 1.0.0
     */
    public fun keeps(age: Duration): Boolean

    /**
     * The retentions every application needs.
     *
     * @since 1.0.0
     */
    public companion object {
        /**
         * Keeps an instance as long as its holder lives, the default.
         *
         * @since 1.0.0
         */
        public val FOREVER: Retention = Retention { true }

        /**
         * Replaces an instance once it is [duration] old.
         *
         * @param duration the longest time an instance is reused, positive.
         * @return the retention.
         * @throws IllegalArgumentException if [duration] is not positive.
         * @since 1.0.0
         */
        public fun expireAfter(duration: Duration): Retention {
            require(duration.isPositive()) { "An instance expires after a positive duration, but was $duration" }
            return Retention { age -> age < duration }
        }
    }
}
