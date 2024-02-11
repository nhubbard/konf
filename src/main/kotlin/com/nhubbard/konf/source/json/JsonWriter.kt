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

package com.nhubbard.konf.source.json

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectWriter
import com.nhubbard.konf.Config
import com.nhubbard.konf.source.Writer
import com.nhubbard.konf.source.base.toHierarchicalMap
import java.io.OutputStream

/**
 * Writer for JSON source.
 */
class JsonWriter(val config: Config) : Writer {
    private val objectWriter: ObjectWriter = config.mapper.writer(
        DefaultPrettyPrinter().withObjectIndenter(
            DefaultIndenter().withLinefeed(System.lineSeparator())
        )
    )
    override fun toWriter(writer: java.io.Writer) {
        objectWriter.writeValue(writer, config.toHierarchicalMap())
    }

    override fun toOutputStream(outputStream: OutputStream) {
        objectWriter.writeValue(outputStream, config.toHierarchicalMap())
    }
}

/**
 * Returns Writer for JSON source.
 */
val Config.toJson: Writer get() = JsonWriter(this)
