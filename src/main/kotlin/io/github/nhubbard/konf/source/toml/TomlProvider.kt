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

package io.github.nhubbard.konf.source.toml

import com.moandjiezana.toml.Toml
import io.github.nhubbard.konf.annotation.JavaApi
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.RegisterExtension
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.asSource
import java.io.InputStream
import java.io.Reader

/**
 * Provider for the TOML source.
 */
@RegisterExtension(["toml"])
object TomlProvider : Provider {
    override fun reader(reader: Reader): Source =
        Toml().read(reader).toMap().asSource(type = "TOML")

    override fun inputStream(inputStream: InputStream): Source =
        Toml().read(inputStream).toMap().asSource(type = "TOML")

    @JavaApi
    @JvmStatic
    fun get() = this
}
