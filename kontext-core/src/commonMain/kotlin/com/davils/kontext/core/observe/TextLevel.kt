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

package com.davils.kontext.core.observe

/**
 * How important a line of a [TextSink] is.
 *
 * @since 1.0.0
 */
public enum class TextLevel {
    /**
     * Instances created and scopes opened or closed.
     *
     * @since 1.0.0
     */
    Debug,

    /**
     * Containers started or closed, children opened and modules loaded or unloaded.
     *
     * @since 1.0.0
     */
    Info,

    /**
     * Failed resolutions, extensions and close functions.
     *
     * @since 1.0.0
     */
    Warning,
}
