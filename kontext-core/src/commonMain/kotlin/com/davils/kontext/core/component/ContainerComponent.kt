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

package com.davils.kontext.core.component

import com.davils.kontext.core.Resolver

/**
 * A class that resolves what it needs from a container it names, with [inject] and [get], instead of receiving it
 * through its constructor, such as an entry point of a framework that creates the class itself.
 *
 * Constructor injection keeps dependencies visible and stays the first choice.
 *
 * @since 1.0.0
 */
public interface ContainerComponent {
    /**
     * The resolver the component resolves from.
     *
     * @since 1.0.0
     */
    public val resolver: Resolver
}
