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

package com.davils.kontext.testing

import com.davils.kontext.core.Profile

/**
 * The profiles tests run in.
 *
 * @since 1.0.0
 */
public object TestProfiles {
    /**
     * The profile of the mock variants, the default of [testContainer].
     *
     * @since 1.0.0
     */
    public val MOCK: Profile = Profile("mock")

    /**
     * The profile of the live variants.
     *
     * @since 1.0.0
     */
    public val LIVE: Profile = Profile("live")
}
