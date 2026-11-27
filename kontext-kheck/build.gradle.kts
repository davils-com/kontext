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

plugins {
    id("kontext.kotlin-library")
}

kreate {
    project {
        description = "Binds kheck to Kontext: a logging as a single that reports on the time source and the " +
            "event bus of the container and is flushed and closed with it, and loggers resolved by name."
    }
}

dependencies {
    commonMainApi(project(":kontext-core"))
    commonMainApi(libs.kheck.api)
    commonMainApi(libs.kheck.core)
}
