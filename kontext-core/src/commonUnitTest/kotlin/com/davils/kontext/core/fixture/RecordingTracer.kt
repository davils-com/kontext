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

package com.davils.kontext.core.fixture

import com.davils.kollect.core.trace.Span
import com.davils.kollect.core.trace.SpanKind
import com.davils.kollect.core.trace.SpanOutcome
import com.davils.kollect.core.trace.Tracer

class RecordingTracer : Tracer {
    private val recorded: MutableList<String> = mutableListOf()

    val spans: List<String> get() = recorded.toList()

    override fun start(operation: String, kind: SpanKind): Span = object : Span {
        private val attributes: MutableList<String> = mutableListOf()

        override fun attribute(key: String, value: String) {
            attributes += "$key=$value"
        }

        override fun end(outcome: SpanOutcome) {
            recorded += "$operation $outcome $attributes"
        }
    }
}
