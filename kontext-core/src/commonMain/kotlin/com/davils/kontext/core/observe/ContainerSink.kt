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
 * Where a container reports what it does; the caller implements it over its own log, audit trail or test probe.
 *
 * Kontext never writes a log or a file itself. [record] is called synchronously on the thread that resolves, opens
 * or closes, because resolution is not suspending, so an implementation returns quickly and hands slow work to its
 * own queue. A sink that throws loses the event: the container counts it as rejected and carries on.
 *
 * @since 1.0.0
 */
public fun interface ContainerSink {
    /**
     * Records [event].
     *
     * @param event what happened.
     * @since 1.0.0
     */
    public fun record(event: ContainerEvent)
}
