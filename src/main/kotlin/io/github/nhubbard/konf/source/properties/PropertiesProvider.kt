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

package io.github.nhubbard.konf.source.properties

import io.github.nhubbard.konf.annotation.JavaApi
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.base.FlatSource
import java.io.InputStream
import java.io.Reader
import java.util.*

/**
 * Provider for the properties source.
 */
object PropertiesProvider : Provider {
    @Suppress("UNCHECKED_CAST")
    private fun Properties.toMap(): Map<String, String> = this as Map<String, String>

    override fun reader(reader: Reader): Source =
        FlatSource(Properties().apply { load(reader) }.toMap(), type = "properties")

    override fun inputStream(inputStream: InputStream): Source =
        FlatSource(Properties().apply { load(inputStream) }.toMap(), type = "properties")

    /**
     * Returns a new source from system properties.
     *
     * @return a new source from system properties
     */
    fun system(): Source = FlatSource(
        System.getProperties().toMap(),
        type = "system-properties",
        allowConflict = true
    )

    @JavaApi
    @JvmStatic
    fun get() = this
}
