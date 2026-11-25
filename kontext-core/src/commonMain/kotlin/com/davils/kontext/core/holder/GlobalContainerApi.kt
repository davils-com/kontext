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

package com.davils.kontext.core.holder

/**
 * Marks the process-wide [GlobalContainer] and what builds on it.
 *
 * A global container is shared state: tests that use it must stop it, and code that reaches for it hides its
 * dependencies. It exists to migrate code written for a global context; new code holds a [ContainerHolder] or a
 * container itself.
 *
 * @since 1.0.0
 */
@RequiresOptIn(
    message = "The global container is process-wide state; prefer a ContainerHolder the application owns.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
public annotation class GlobalContainerApi
