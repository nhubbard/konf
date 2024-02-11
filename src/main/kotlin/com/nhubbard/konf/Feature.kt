/*
 * Copyright (c) 2017-2024 Uchuhimo
 * Copyright (c) 2024-present Nicholas Hubbard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for specific language governing permissions and
 * limitations under the License.
 */

package com.nhubbard.konf

/**
 * Enumeration that defines simple on/off features.
 */
enum class Feature(val enabledByDefault: Boolean) {
    /**
     * Feature that determines what happens when unknown paths appear in the source.
     * If enabled, an exception is thrown when loading from the source
     * to indicate it contains unknown paths.
     *
     * This feature is disabled by default.
     */
    FAIL_ON_UNKNOWN_PATH(false),
    /**
     * Feature that determines whether loading keys from sources case-insensitively.
     *
     * This feature is disabled by default.
     */
    LOAD_KEYS_CASE_INSENSITIVELY(false),
    /**
     * Feature that determines whether loading keys from sources as little camel case.
     *
     * This feature is enabled by default.
     */
    LOAD_KEYS_AS_LITTLE_CAMEL_CASE(true),
    /**
     * Feature that determines whether sources are optional by default.
     *
     * This feature is disabled by default.
     */
    OPTIONAL_SOURCE_BY_DEFAULT(false),
    /**
     * Feature that determines whether sources should be substituted before loaded into config.
     *
     * This feature is enabled by default.
     */
    SUBSTITUTE_SOURCE_BEFORE_LOADED(true),
    /**
     * Feature that writes descriptions assigned to [Item]s as comments
     * above the written configuration value.
     *
     * This feature is disabled by default.
     */
    WRITE_DESCRIPTIONS_AS_COMMENTS(false)
}
