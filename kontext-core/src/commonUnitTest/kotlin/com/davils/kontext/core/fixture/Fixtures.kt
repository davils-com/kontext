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

import com.davils.kollect.core.MeterRegistry
import com.davils.kollect.core.name.MetricName
import com.davils.kollect.core.snapshot.GaugePoint
import com.davils.kollect.core.snapshot.Point
import com.davils.kollect.core.snapshot.SumPoint
import com.davils.kontext.core.Container
import com.davils.kontext.core.Module
import com.davils.kontext.core.Profile
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.ScopeKey
import com.davils.kontext.core.container
import com.davils.kontext.core.container.ContainerBuilder
import com.davils.kore.testing.SequentialUuidV7Generator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext

val REQUEST: ScopeKey = ScopeKey("request")
val SESSION: ScopeKey = ScopeKey("session")
val PAGE: ScopeKey = ScopeKey("page")
val LIVE: Profile = Profile("live")
val MOCK: Profile = Profile("mock")
val ENGLISH: Qualifier = Qualifier("english")
val GERMAN: Qualifier = Qualifier("german")
const val HELLO: String = "hello"
const val HALLO: String = "hallo"
const val BROKEN: String = "broken"
const val UNCLOSABLE: String = "unclosable"

suspend fun containerOf(
    vararg modules: Module,
    profile: Profile = Profile.DEFAULT,
    block: ContainerBuilder.() -> Unit = {},
): Container = container(profile) {
    ids = SequentialUuidV7Generator()
    modules(*modules)
    block()
}

fun MeterRegistry.sum(name: String, vararg labels: String): Double {
    val point = pointOf(name, labels.toList()) as? SumPoint
    return point?.value ?: 0.0
}

fun MeterRegistry.gauge(name: String, vararg labels: String): Double? {
    val point = pointOf(name, labels.toList()) as? GaugePoint
    return point?.value
}

suspend fun withScope(block: suspend (CoroutineScope) -> Unit) {
    val scope = CoroutineScope(currentCoroutineContext() + Job())
    try {
        block(scope)
    } finally {
        scope.cancel()
    }
}

private fun MeterRegistry.pointOf(name: String, labels: List<String>): Point? {
    val family = snapshot().family(MetricName.of(name)) ?: return null
    return family.points.firstOrNull { point -> point.labels.values == labels }
}
