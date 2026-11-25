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

package com.davils.kontext.core

import com.davils.kontext.core.definition.ModuleBuilder

/**
 * Builds a module named [name] from the definitions [block] declares.
 *
 * ```kotlin
 * val http = module("app.http") {
 *     single { HttpClient(get()) }
 *     factory { Request(get(), parameters.get<String>()) }
 * }
 * ```
 *
 * @param name the name of the module, a dotted name in lower case.
 * @param eager whether the singles of the module are created when the container starts unless they say otherwise.
 * @param block declares the definitions.
 * @return the module.
 * @throws IllegalArgumentException if [name] is not a valid name.
 * @since 1.0.0
 */
public fun module(name: String, eager: Boolean = false, block: ModuleBuilder.() -> Unit): Module {
    val builder = ModuleBuilder(ModuleName(name), eager)
    builder.block()
    return builder.build()
}

/**
 * Declares a module whose [block] runs only after the container started, in its coroutine scope, so that a large
 * declaration does not delay the start; see [com.davils.kontext.core.container.ContainerBuilder.lazyModules] and
 * [Container.awaitReady].
 *
 * @param name the name of the module, a dotted name in lower case.
 * @param block declares the definitions.
 * @return the lazy module.
 * @throws IllegalArgumentException if [name] is not a valid name.
 * @since 1.0.0
 */
public fun lazyModule(name: String, block: ModuleBuilder.() -> Unit): LazyModule = LazyModule(ModuleName(name), block)
