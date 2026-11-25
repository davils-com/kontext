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

package com.davils.kontext.core.internal

import com.davils.kollect.core.Family
import com.davils.kollect.core.MeterRegistry
import com.davils.kollect.core.declare.FamilyBuilder
import com.davils.kollect.core.meter.Counter
import com.davils.kollect.core.meter.Gauge
import com.davils.kollect.core.meter.StateSet
import com.davils.kollect.core.meter.Timer
import com.davils.kontext.core.container.ContainerState
import com.davils.kontext.core.definition.Lifetime
import kotlin.time.Duration

private const val CONTAINER: String = "container"
private const val LIFETIME: String = "lifetime"
private const val REASON: String = "reason"

internal class ContainerMeters(registry: MeterRegistry, private val name: String) {
    private val created: Family<Counter> = counterOf(
        registry = registry,
        metric = "kontext.instances",
        text = "The instances a container created, by the lifetime of their definition.",
        labels = { labelKeys(CONTAINER, LIFETIME) },
    )
    private val creation: Family<Timer> = timerOf(registry)
    private val failures: Family<Counter> = counterOf(
        registry = registry,
        metric = "kontext.resolution.failures",
        text = "The resolutions of a container that failed, by reason.",
        labels = { labelKeys(CONTAINER, REASON) },
    )
    private val scopes: Gauge = gaugeOf(
        registry = registry,
        metric = "kontext.scopes.open",
        text = "The scopes a container holds open.",
    ).with(name)
    private val children: Gauge = gaugeOf(
        registry = registry,
        metric = "kontext.children.open",
        text = "The child containers a container holds open.",
    ).with(name)
    private val rejected: Counter = counterOf(
        registry = registry,
        metric = "kontext.events.rejected",
        text = "The events of a container that its sink or its event bus did not take.",
        labels = { labelKeys(CONTAINER) },
    ).with(name)
    private val state: StateSet<ContainerState> = stateOf(registry).with(name)

    fun created(lifetime: Lifetime, elapsed: Duration) {
        val label = lifetime.name.lowercase()
        val counter = created.with(name, label)
        counter.increment()
        val timer = creation.with(name, label)
        timer.record(elapsed)
    }

    fun failed(reason: String) {
        val counter = failures.with(name, reason)
        counter.increment()
    }

    fun scopes(open: Int) {
        scopes.set(open.toDouble())
    }

    fun children(open: Int) {
        children.set(open.toDouble())
    }

    fun rejected() {
        rejected.increment()
    }

    fun state(next: ContainerState) {
        state.set(next)
    }

    private companion object {
        fun counterOf(
            registry: MeterRegistry,
            metric: String,
            text: String,
            labels: FamilyBuilder.() -> Unit,
        ): Family<Counter> {
            val registration = registry.counter(metric) {
                description = text
                labels()
            }
            return registration.family
        }

        fun gaugeOf(registry: MeterRegistry, metric: String, text: String): Family<Gauge> {
            val registration = registry.gauge(metric) {
                description = text
                labelKeys(CONTAINER)
            }
            return registration.family
        }

        fun timerOf(registry: MeterRegistry): Family<Timer> {
            val registration = registry.timer("kontext.instance.creation") {
                description = "How long the factories of a container ran, by the lifetime of their definition."
                labelKeys(CONTAINER, LIFETIME)
            }
            return registration.family
        }

        fun stateOf(registry: MeterRegistry): Family<StateSet<ContainerState>> {
            val registration = registry.stateSet(name = "kontext.container.state", initial = ContainerState.Starting) {
                description = "Whether a container is starting, running, closing or closed."
                labelKeys(CONTAINER)
            }
            return registration.family
        }
    }
}
