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

package com.davils.kontext.core.error

import com.davils.kontext.core.ContainerName

/**
 * Thrown when a [com.davils.kontext.core.holder.ContainerHolder] that already holds a container is asked to start
 * another.
 *
 * @property container the name of the container the holder holds.
 * @since 1.0.0
 */
public class ContainerAlreadyStartedException internal constructor(public val container: ContainerName) :
    IllegalStateException("The holder already holds the container $container")
