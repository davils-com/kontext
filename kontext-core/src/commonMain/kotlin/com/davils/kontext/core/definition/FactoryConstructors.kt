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

import com.davils.kontext.core.DefinitionKey
import com.davils.kontext.core.Qualifier
import com.davils.kore.core.annotation.DavilsGenerated
import kotlin.reflect.typeOf

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: () -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = { constructor() },
    dependencies = emptyList(),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (T1) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = { constructor(get()) },
    dependencies = listOf(DefinitionKey(typeOf<T1>())),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (T1, T2) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = { constructor(get(), get()) },
    dependencies = listOf(DefinitionKey(typeOf<T1>()), DefinitionKey(typeOf<T2>())),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (T1, T2, T3) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = { constructor(get(), get(), get()) },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
        T1,
        T2,
        T3,
        T4,
    ) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
            get(),
            get(),
            get(),
            get(),
        )
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
    ) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
 *
 * @param R the type of the instance.
 * @param T1 the type of parameter 1, resolved without a qualifier.
 * @param T2 the type of parameter 2, resolved without a qualifier.
 * @param T3 the type of parameter 3, resolved without a qualifier.
 * @param T4 the type of parameter 4, resolved without a qualifier.
 * @param T5 the type of parameter 5, resolved without a qualifier.
 * @param T6 the type of parameter 6, resolved without a qualifier.
 * @param constructor the constructor or function that creates the instance.
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
    ) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
    ) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
        T1,
        T2,
        T3,
        T4,
        T5,
        T6,
        T7,
        T8,
    ) -> R,
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
    reified T1 : Any,
    reified T2 : Any,
    reified T3 : Any,
    reified T4 : Any,
    reified T5 : Any,
    reified T6 : Any,
    reified T7 : Any,
    reified T8 : Any,
    reified T9 : Any,
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
        DefinitionKey(typeOf<T17>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
        DefinitionKey(typeOf<T17>()),
        DefinitionKey(typeOf<T18>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
        DefinitionKey(typeOf<T17>()),
        DefinitionKey(typeOf<T18>()),
        DefinitionKey(typeOf<T19>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
        DefinitionKey(typeOf<T17>()),
        DefinitionKey(typeOf<T18>()),
        DefinitionKey(typeOf<T19>()),
        DefinitionKey(typeOf<T20>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
        DefinitionKey(typeOf<T17>()),
        DefinitionKey(typeOf<T18>()),
        DefinitionKey(typeOf<T19>()),
        DefinitionKey(typeOf<T20>()),
        DefinitionKey(typeOf<T21>()),
    ),
)

/**
 * Declares a factory created by [constructor] on every resolution, with every parameter resolved by its type, such
 * as `factoryOf(::Request)`. The parameter types are recorded for [com.davils.kontext.core.verifyStatically].
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
 * @param qualifier the qualifier of the definition, `null` for none.
 * @return the definition, to bind it to further types.
 * @since 1.0.0
 */
@DavilsGenerated(by = "arity expansion", from = "factoryOf with 0 to 22 parameters")
public inline fun <
    reified R : Any,
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
    > DefinitionBuilder.factoryOf(
    crossinline constructor: (
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
    qualifier: Qualifier? = null,
): Definition<R> = declareSync(
    key = DefinitionKey(typeOf<R>(), qualifier),
    lifetime = Lifetime.Factory,
    create = {
        constructor(
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
    },
    dependencies = listOf(
        DefinitionKey(typeOf<T1>()),
        DefinitionKey(typeOf<T2>()),
        DefinitionKey(typeOf<T3>()),
        DefinitionKey(typeOf<T4>()),
        DefinitionKey(typeOf<T5>()),
        DefinitionKey(typeOf<T6>()),
        DefinitionKey(typeOf<T7>()),
        DefinitionKey(typeOf<T8>()),
        DefinitionKey(typeOf<T9>()),
        DefinitionKey(typeOf<T10>()),
        DefinitionKey(typeOf<T11>()),
        DefinitionKey(typeOf<T12>()),
        DefinitionKey(typeOf<T13>()),
        DefinitionKey(typeOf<T14>()),
        DefinitionKey(typeOf<T15>()),
        DefinitionKey(typeOf<T16>()),
        DefinitionKey(typeOf<T17>()),
        DefinitionKey(typeOf<T18>()),
        DefinitionKey(typeOf<T19>()),
        DefinitionKey(typeOf<T20>()),
        DefinitionKey(typeOf<T21>()),
        DefinitionKey(typeOf<T22>()),
    ),
)
