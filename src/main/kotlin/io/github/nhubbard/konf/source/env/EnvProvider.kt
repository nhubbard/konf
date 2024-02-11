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

package io.github.nhubbard.konf.source.env

import io.github.nhubbard.konf.Feature
import io.github.nhubbard.konf.annotation.JavaApi
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.base.FlatSource

/**
 * Provider for system environment source.
 */
object EnvProvider {
    private val validEnv = Regex("(\\w+)(.\\w+)*")

    /**
     * Returns a new source from system environment.
     *
     * @param nested whether to treat "AA_BB_CC" as nested format "AA.BB.CC" or not. True by default.
     * @return a new source from system environment
     */
    @JvmOverloads
    fun env(nested: Boolean = true): Source = envMap(System.getenv(), nested)

    @JvmOverloads
    fun envMap(map: Map<String, String>, nested: Boolean = true): Source {
        return FlatSource(
            map.mapKeys { (key, _) ->
                if (nested) key.replace('_', '.') else key
            }.filter { (key, _) ->
                key.matches(validEnv)
            }.toSortedMap(),
            type = "system-environment",
            allowConflict = true
        ).enabled(
            Feature.LOAD_KEYS_CASE_INSENSITIVELY
        ).disabled(
            Feature.SUBSTITUTE_SOURCE_BEFORE_LOADED
        )
    }

    @JavaApi
    @JvmStatic
    fun get() = this
}
