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

/**
 * Thrown when a [com.davils.kontext.core.holder.ContainerHolder] is asked for its container before it started one or
 * after it stopped it.
 *
 * @since 1.0.0
 */
public class ContainerNotStartedException internal constructor() :
    IllegalStateException("The holder has no running container")
