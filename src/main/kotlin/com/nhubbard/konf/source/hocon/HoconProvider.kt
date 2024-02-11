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

package com.nhubbard.konf.source.hocon

import com.typesafe.config.ConfigFactory
import com.nhubbard.konf.annotation.JavaApi
import com.nhubbard.konf.source.Provider
import com.nhubbard.konf.source.RegisterExtension
import com.nhubbard.konf.source.Source
import java.io.InputStream
import java.io.Reader

/**
 * Provider for HOCON source.
 */
@RegisterExtension(["conf"])
object HoconProvider : Provider {
    override fun reader(reader: Reader): Source =
        HoconSource(ConfigFactory.parseReader(reader).resolve())

    override fun inputStream(inputStream: InputStream): Source {
        inputStream.reader().use {
            return reader(it)
        }
    }

    @JavaApi
    @JvmStatic
    fun get() = this
}
