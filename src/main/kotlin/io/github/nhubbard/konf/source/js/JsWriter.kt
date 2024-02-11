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

package io.github.nhubbard.konf.source.js

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.Writer
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import java.io.OutputStream
import java.util.regex.Pattern

/**
 * Writer for JavaScript source.
 */
class JsWriter(val config: Config) : Writer {
    override fun toWriter(writer: java.io.Writer) {
        val jsonOutput = config.mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(config.toHierarchicalMap())
        val pattern = Pattern.compile("(\")(.*)(\"\\s*):")
        val jsOutput = pattern.matcher(jsonOutput).replaceAll("$2:")
        writer.write("($jsOutput)")
    }

    override fun toOutputStream(outputStream: OutputStream) {
        outputStream.writer().use {
            toWriter(it)
        }
    }
}

/**
 * Returns writer for JavaScript source.
 */
val Config.toJs: Writer get() = JsWriter(this)
