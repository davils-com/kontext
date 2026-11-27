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

package com.davils.kontext.kheck.fixture

import com.davils.kheck.core.sink.LogLine
import com.davils.kheck.core.sink.LogSink

class RecordingLogSink : LogSink {
    private val written: MutableList<LogLine> = mutableListOf()

    var isClosed: Boolean = false
        private set

    val lines: List<LogLine> get() = written.toList()

    override suspend fun write(lines: List<LogLine>) {
        written += lines
    }

    override suspend fun close() {
        isClosed = true
    }
}
