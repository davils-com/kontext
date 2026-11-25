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
import com.davils.kontext.core.DefinitionKey
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
    private val definitions = HashMap<DefinitionKey, Definition<*>>()

    /**
     * Declares a single of [type] created by [create].
     *
     * Since a raw or unchecked Java factory can return an object of any class, the instance is checked against
     * [type]: an instance of another class fails the resolution with a [ClassCastException] as its cause.
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
     * Since a raw or unchecked Java factory can return an object of any class, the instance is checked against
     * [type]: an instance of another class fails the resolution with a [ClassCastException] as its cause.
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
     * Makes the definition of [type] with [qualifier] found as [as] with [asQualifier] as well, such as an
     * implementation under its interface.
     *
     * @param type the class of a definition this builder declared.
     * @param as the further class, a supertype of [type] or [type] itself.
     * @param qualifier the qualifier of the definition of [type], `null` for none.
     * @param asQualifier the qualifier of the binding, `null` for none.
     * @return this builder.
     * @throws IllegalArgumentException if this builder declares no definition of [type] with [qualifier], if [as] is
     * not a supertype of [type], or if the definition is already found as [as] with [asQualifier].
     * @since 1.0.0
     */
    @JvmOverloads
    public fun bind(
        type: Class<*>,
        `as`: Class<*>,
        qualifier: Qualifier? = null,
        asQualifier: Qualifier? = null,
    ): JavaModuleBuilder {
        val key = JavaResolvers.keyOf(type, qualifier)
        val definition = requireNotNull(definitions[key]) { "No definition of $key is declared here" }
        require(`as`.isAssignableFrom(type)) {
            "${type.name} cannot be found as ${`as`.name}, which it does not extend"
        }
        definition.alias(JavaResolvers.keyOf(`as`, asQualifier))
        return this
    }

    private fun <T : Any> declared(
        type: Class<T>,
        qualifier: Qualifier?,
        lifetime: Lifetime,
        create: Function<DefinitionContext, T>,
    ): JavaModuleBuilder {
        val key = JavaResolvers.keyOf(type, qualifier)
        val definition = builder.declareSync<T>(
            key = key,
            lifetime = lifetime,
            create = { checked(type = type, instance = create.apply(this)) },
        )
        definitions[key] = definition
        return this
    }

    override fun toString(): String = "JavaModuleBuilder(${builder.name})"
}

private fun <T : Any> checked(type: Class<T>, instance: Any): T {
    if (type.isInstance(instance)) return type.cast(instance)
    throw ClassCastException("The factory of ${type.name} returned an instance of ${instance.javaClass.name}")
}
