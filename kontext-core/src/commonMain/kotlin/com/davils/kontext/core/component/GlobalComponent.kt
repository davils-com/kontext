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
import com.davils.kontext.core.holder.GlobalContainer
import com.davils.kontext.core.holder.GlobalContainerApi

/**
 * A component that resolves from the [GlobalContainer], for code migrating from a global injection context.
 *
 * [get] resolves from the container the global holder holds at the call. A value of [inject] or [injectOrNull] is
 * resolved once and kept: after the global holder is stopped and started again, the component still returns the
 * instance of the closed container, which may already be closed. A component that outlives a restart of the holder
 * resolves with [get] on every use, or is created again.
 *
 * @since 1.0.0
 */
@GlobalContainerApi
public interface GlobalComponent : ContainerComponent {
    override val resolver: Resolver get() = GlobalContainer.current
}
