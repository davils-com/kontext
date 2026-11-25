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
 * Takes the events of a container as lines of text, for a logger of the application such as SLF4J, without Kontext
 * depending on one; turn it into a [ContainerSink] with [asContainerSink].
 *
 * A line holds names and keys only, escaped for a terminal, never a value or an exception message.
 *
 * @since 1.0.0
 */
public fun interface TextSink {
    /**
     * Writes [line].
     *
     * @param level how important the line is.
     * @param line the line, such as `container root started in profile default with 3 definitions`.
     * @since 1.0.0
     */
    public fun write(level: TextLevel, line: String)
}
