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

package com.davils.kontext.core.java

import com.davils.kontext.core.DefinitionContext
import com.davils.kontext.core.Qualifier
import com.davils.kontext.core.definition.Definition
import com.davils.kontext.core.definition.Lifetime
import com.davils.kontext.core.definition.ModuleBuilder
import java.util.function.Function

/**
 * Declares the definitions of a module from Java, see [JavaModules.module].
 *
 * @param builder the module builder it fills.
 * @since 1.0.0
 */
public class JavaModuleBuilder internal constructor(private val builder: ModuleBuilder) {
    private val definitions = HashMap<Class<*>, Definition<*>>()

    /**
     * Declares a single of [type] created by [create].
     *
     * @param T the type.
     * @param type the class the instance is found by.
     * @param qualifier the qualifier, `null` for none.
     * @param create creates the instance from the context, which resolves what it needs.
     * @return this builder.
     * @since 1.0.0
     */
    @JvmOverloads
    public fun <T : Any> single(
        type: Class<T>,
        qualifier: Qualifier? = null,
        create: Function<DefinitionContext, T>,
    ): JavaModuleBuilder = declared(type = type, qualifier = qualifier, lifetime = Lifetime.Single, create = create)

    /**
     * Declares a factory of [type] created by [create] on every resolution.
     *
     * @param T the type.
     * @param type the class the instance is found by.
     * @param qualifier the qualifier, `null` for none.
     * @param create creates the instance from the context, which resolves what it needs.
     * @return this builder.
     * @since 1.0.0
     */
    @JvmOverloads
    public fun <T : Any> factory(
        type: Class<T>,
        qualifier: Qualifier? = null,
        create: Function<DefinitionContext, T>,
    ): JavaModuleBuilder = declared(type = type, qualifier = qualifier, lifetime = Lifetime.Factory, create = create)

    /**
     * Makes the definition of [type] found as [as] as well, such as an implementation under its interface.
     *
     * @param type the class of a definition this builder declared.
     * @param as the further class.
     * @return this builder.
     * @throws IllegalArgumentException if this builder declares no definition of [type].
     * @since 1.0.0
     */
    public fun bind(type: Class<*>, `as`: Class<*>): JavaModuleBuilder {
        val definition = requireNotNull(definitions[type]) { "No definition of ${type.name} is declared here" }
        definition.alias(JavaResolvers.keyOf(`as`))
        return this
    }

    private fun <T : Any> declared(
        type: Class<T>,
        qualifier: Qualifier?,
        lifetime: Lifetime,
        create: Function<DefinitionContext, T>,
    ): JavaModuleBuilder {
        val definition = builder.declareSync<T>(
            key = JavaResolvers.keyOf(type, qualifier),
            lifetime = lifetime,
            create = { create.apply(this) },
        )
        definitions[type] = definition
        return this
    }

    override fun toString(): String = "JavaModuleBuilder(${builder.name})"
}
