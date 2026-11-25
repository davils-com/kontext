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

import com.davils.kontext.core.observe.TextLevel
import com.davils.kontext.core.observe.TextSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * A [TextSink] that keeps every line in memory, for tests of what a container logs.
 *
 * It is safe to write to from every thread.
 *
 * It keeps every line until it is dropped, without a bound, so it is for tests only and never used in
 * production, where a long-running container would grow it until the memory runs out.
 *
 * @since 1.0.0
 */
public class RecordingTextSink : TextSink {
    private val recorded: MutableStateFlow<List<Pair<TextLevel, String>>> = MutableStateFlow(emptyList())

    /**
     * The lines written so far, in the order they arrived.
     *
     * @since 1.0.0
     */
    public val lines: List<String> get() = recorded.value.map { entry -> entry.second }

    /**
     * Keeps [line].
     *
     * @param level how important the line is.
     * @param line the line.
     * @since 1.0.0
     */
    override fun write(level: TextLevel, line: String) {
        recorded.update { lines -> lines + (level to line) }
    }

    /**
     * Returns the lines written at [level].
     *
     * @param level the level.
     * @return the lines, in the order they arrived.
     * @since 1.0.0
     */
    public fun at(level: TextLevel): List<String> =
        recorded.value.filter { entry -> entry.first == level }.map { entry -> entry.second }

    override fun toString(): String = "RecordingTextSink(${recorded.value.size} lines)"
}
