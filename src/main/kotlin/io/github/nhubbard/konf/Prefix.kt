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

package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.Source

/**
 * Convenient class for constructing [Spec]/[Config]/[Source] with prefix.
 */
data class Prefix(
    /**
     * The path of the prefix
     */
    val path: String = ""
) {
    /**
     * Returns a config spec with this prefix.
     *
     * @param spec the base config spec
     * @return a config spec with this prefix
     */
    operator fun plus(spec: Spec): Spec = spec.withPrefix(path)

    /**
     * Returns a config with this prefix.
     *
     * @param config the base config
     * @return a config with this prefix
     */
    operator fun plus(config: Config): Config = config.withPrefix(path)

    /**
     * Returns a source with this prefix.
     *
     * @param source the base source
     * @return a source with this prefix
     */
    operator fun plus(source: Source): Source = source.withPrefix(path)
}
