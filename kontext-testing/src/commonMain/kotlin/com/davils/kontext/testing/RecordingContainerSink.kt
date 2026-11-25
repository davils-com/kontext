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

import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.core.observe.ContainerSink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * A [ContainerSink] that keeps every event in memory, for tests of what a container reports.
 *
 * It is safe to record from every thread.
 *
 * It keeps every event until it is dropped or cleared with [clear], without a bound, so it is for tests only and
 * never used in production, where a long-running container would grow it until the memory runs out.
 *
 * @since 1.0.0
 */
public class RecordingContainerSink : ContainerSink {
    private val recorded: MutableStateFlow<List<ContainerEvent>> = MutableStateFlow(emptyList())

    /**
     * The events recorded so far, in the order they arrived.
     *
     * @since 1.0.0
     */
    public val events: List<ContainerEvent> get() = recorded.value

    /**
     * Keeps [event].
     *
     * @param event the event.
     * @since 1.0.0
     */
    override fun record(event: ContainerEvent) {
        recorded.update { events -> events + event }
    }

    /**
     * Returns the recorded events of type [E].
     *
     * @param E the type of the events.
     * @return the events of that type, in the order they arrived.
     * @since 1.0.0
     */
    public inline fun <reified E : ContainerEvent> of(): List<E> = events.filterIsInstance<E>()

    /**
     * Forgets every event.
     *
     * @since 1.0.0
     */
    public fun clear() {
        recorded.value = emptyList()
    }

    override fun toString(): String = "RecordingContainerSink(${events.size} events)"
}
