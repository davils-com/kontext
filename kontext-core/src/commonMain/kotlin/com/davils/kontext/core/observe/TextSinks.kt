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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.core.observe

import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.text.SafeText

/**
 * Turns this text sink into a container sink that writes every event of at least [minimum] as a line.
 *
 * @param minimum the least important level written.
 * @return the container sink.
 * @since 1.0.0
 */
public fun TextSink.asContainerSink(minimum: TextLevel = TextLevel.Info): ContainerSink = ContainerSink { event ->
    val level = levelOf(event)
    if (level >= minimum) write(level, SafeText.text(lineOf(event)))
}

private fun levelOf(event: ContainerEvent): TextLevel = when (event) {
    is ContainerEvent.InstanceCreated, is ContainerEvent.ScopeOpened, is ContainerEvent.ScopeClosed -> TextLevel.Debug
    is ContainerEvent.Started, is ContainerEvent.Closed, is ContainerEvent.ChildOpened -> TextLevel.Info
    is ContainerEvent.ModulesLoaded, is ContainerEvent.ModulesUnloaded -> TextLevel.Info
    is ContainerEvent.ResolutionFailed, is ContainerEvent.ExtensionFailed -> TextLevel.Warning
    is ContainerEvent.DisposalFailed -> TextLevel.Warning
}

private fun lineOf(event: ContainerEvent): String {
    val prefix = "container ${event.container}"
    return when (event) {
        is ContainerEvent.Started -> {
            "$prefix started in profile ${event.profile} with ${event.definitions} definitions in ${event.elapsed}"
        }

        is ContainerEvent.Closed -> {
            "$prefix closed with ${event.failures} failures in ${event.elapsed}"
        }

        is ContainerEvent.ChildOpened -> {
            "$prefix opened the child ${event.child}"
        }

        is ContainerEvent.ScopeOpened -> {
            "$prefix opened the scope ${event.scope} ${event.id}"
        }

        is ContainerEvent.ScopeClosed -> {
            "$prefix closed the scope ${event.scope} ${event.id}"
        }

        is ContainerEvent.InstanceCreated -> {
            "$prefix created ${event.key} (${event.lifetime}) in ${event.elapsed}"
        }

        is ContainerEvent.ResolutionFailed -> {
            "$prefix failed to resolve ${event.failure.key} (${event.failure.reason})"
        }

        is ContainerEvent.ModulesLoaded -> {
            "$prefix loaded ${event.modules}"
        }

        is ContainerEvent.ModulesUnloaded -> {
            "$prefix unloaded ${event.modules}"
        }

        is ContainerEvent.ExtensionFailed -> {
            "$prefix heard the extension ${event.extension} fail on ${event.phase}"
        }

        is ContainerEvent.DisposalFailed -> {
            "$prefix failed to close ${event.key}"
        }
    }
}
