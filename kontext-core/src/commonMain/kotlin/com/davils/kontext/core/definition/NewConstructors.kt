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

package com.davils.kontext.core.definition

import com.davils.kontext.core.Resolver
import com.davils.kore.core.annotation.DavilsGenerated

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    > Resolver.new(constructor: () -> R): R = constructor()

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    > Resolver.new(constructor: (T1) -> R): R = constructor(get())

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    > Resolver.new(
    constructor: (T1, T2) -> R,
): R = constructor(get(), get())

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    > Resolver.new(
    constructor: (T1, T2, T3) -> R,
): R = constructor(get(), get(), get())

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param T17 the type of parameter 17, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    reified T17 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
        T17,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param T17 the type of parameter 17, resolved without a qualifier.
 * @param T18 the type of parameter 18, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    reified T17 : Any,
    reified T18 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
        T17,
        T18,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param T17 the type of parameter 17, resolved without a qualifier.
 * @param T18 the type of parameter 18, resolved without a qualifier.
 * @param T19 the type of parameter 19, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    reified T17 : Any,
    reified T18 : Any,
    reified T19 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
        T17,
        T18,
        T19,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param T17 the type of parameter 17, resolved without a qualifier.
 * @param T18 the type of parameter 18, resolved without a qualifier.
 * @param T19 the type of parameter 19, resolved without a qualifier.
 * @param T20 the type of parameter 20, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    reified T17 : Any,
    reified T18 : Any,
    reified T19 : Any,
    reified T20 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
        T17,
        T18,
        T19,
        T20,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param T17 the type of parameter 17, resolved without a qualifier.
 * @param T18 the type of parameter 18, resolved without a qualifier.
 * @param T19 the type of parameter 19, resolved without a qualifier.
 * @param T20 the type of parameter 20, resolved without a qualifier.
 * @param T21 the type of parameter 21, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    reified T17 : Any,
    reified T18 : Any,
    reified T19 : Any,
    reified T20 : Any,
    reified T21 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
        T17,
        T18,
        T19,
        T20,
        T21,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)

/**
 * Creates an instance with [constructor], every parameter resolved by its type from this resolver, without declaring
 * it, such as `new(::Handler)` inside a factory.
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param T7 the type of parameter 7, resolved without a qualifier.
 * @param T8 the type of parameter 8, resolved without a qualifier.
 * @param T9 the type of parameter 9, resolved without a qualifier.
 * @param T10 the type of parameter 10, resolved without a qualifier.
 * @param T11 the type of parameter 11, resolved without a qualifier.
 * @param T12 the type of parameter 12, resolved without a qualifier.
 * @param T13 the type of parameter 13, resolved without a qualifier.
 * @param T14 the type of parameter 14, resolved without a qualifier.
 * @param T15 the type of parameter 15, resolved without a qualifier.
 * @param T16 the type of parameter 16, resolved without a qualifier.
 * @param T17 the type of parameter 17, resolved without a qualifier.
 * @param T18 the type of parameter 18, resolved without a qualifier.
 * @param T19 the type of parameter 19, resolved without a qualifier.
 * @param T20 the type of parameter 20, resolved without a qualifier.
 * @param T21 the type of parameter 21, resolved without a qualifier.
 * @param T22 the type of parameter 22, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @return the new instance.
 * @throws com.davils.kontext.core.error.ResolutionException if a parameter cannot be resolved.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "new with 0 to 22 parameters")
public inline fun <
    R,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    reified T10 : Any,
    reified T11 : Any,
    reified T12 : Any,
    reified T13 : Any,
    reified T14 : Any,
    reified T15 : Any,
    reified T16 : Any,
    reified T17 : Any,
    reified T18 : Any,
    reified T19 : Any,
    reified T20 : Any,
    reified T21 : Any,
    reified T22 : Any,
    > Resolver.new(
    constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
        T9,
        T10,
        T11,
        T12,
        T13,
        T14,
        T15,
        T16,
        T17,
        T18,
        T19,
        T20,
        T21,
        T22,
    ) -> R,
): R = constructor(
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
    get(),
)
