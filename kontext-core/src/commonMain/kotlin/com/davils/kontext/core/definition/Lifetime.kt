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

package com.davils.kontext.core.definition

/**
 * How long the instance of a definition lives.
 *
 * @since 1.0.0
 */
public enum class Lifetime {
    /**
     * One instance per container, created on first use or, when eager, when the container starts.
     *
     * @since 1.0.0
     */
    Single,

    /**
     * A new instance on every resolution; Kontext never closes it.
     *
     * @since 1.0.0
     */
    Factory,

    /**
     * One instance per opened scope, closed with the scope.
     *
     * @since 1.0.0
     */
    Scoped,
}
