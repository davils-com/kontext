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

import com.davils.kontext.core.parameter.Parameters
import kotlinx.coroutines.CoroutineScope

/**
 * The receiver of every factory: it resolves what the instance needs and holds the parameters of the call.
 *
 * A context belongs to one creation; a factory does not keep it. [parameters] are only those passed for this very
 * definition. [coroutineScope] lives as long as the holder of the instance, the scope for a scoped instance and the
 * container otherwise, so that work an instance starts ends with it.
 *
 * @since 1.0.0
 */
public abstract class DefinitionContext internal constructor() : Resolver() {
    /**
     * The parameters passed for this definition, [Parameters.EMPTY] for none.
     *
     * @since 1.0.0
     */
    public abstract val parameters: Parameters

    /**
     * The coroutine scope of the holder of the instance, cancelled when the holder closes.
     *
     * @since 1.0.0
     */
    public abstract val coroutineScope: CoroutineScope
}
