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

package com.nhubbard.konfig.source.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhubbard.konfig.annotation.JavaApi
import com.nhubbard.konfig.source.Provider
import com.nhubbard.konfig.source.Source
import java.io.InputStream
import java.io.Reader

/**
 * Provider for JSON source.
 */
object JsonProvider : Provider {
    override fun reader(reader: Reader): Source =
        JsonSource(ObjectMapper().readTree(reader))

    override fun inputStream(inputStream: InputStream): Source =
        JsonSource(ObjectMapper().readTree(inputStream))

    @JavaApi
    @JvmStatic
    fun get() = this
}
