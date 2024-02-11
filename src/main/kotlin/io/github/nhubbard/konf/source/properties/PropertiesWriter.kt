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

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.Writer
import io.github.nhubbard.konf.source.base.toFlatMap
import java.io.FilterOutputStream
import java.io.OutputStream
import java.util.*

/**
 * Writer for properties source.
 */
class PropertiesWriter(val config: Config) : Writer {
    override fun toWriter(writer: java.io.Writer) {
        NoCommentProperties().apply { putAll(config.toFlatMap()) }.store(writer, null)
    }

    override fun toOutputStream(outputStream: OutputStream) {
        NoCommentProperties().apply { putAll(config.toFlatMap()) }.store(outputStream, null)
    }
}

private class NoCommentProperties : Properties() {
    private class StripFirstLineStream(out: OutputStream) : FilterOutputStream(out) {
        private var firstLineSeen = false

        override fun write(b: Int) {
            if (firstLineSeen) {
                super.write(b)
            } else if (b == '\n'.code) {
                firstLineSeen = true
            }
        }
    }

    private class StripFirstLineWriter(writer: java.io.Writer) : java.io.FilterWriter(writer) {
        override fun write(cbuf: CharArray, off: Int, len: Int) {
            val offset = cbuf.indexOfFirst { it == '\n' }
            super.write(cbuf, offset + 1, len - offset - 1)
        }
    }

    override fun store(out: OutputStream, comments: String?) {
        super.store(StripFirstLineStream(out), null)
    }

    override fun store(writer: java.io.Writer, comments: String?) {
        super.store(StripFirstLineWriter(writer), null)
    }
}

/**
 * Returns writer for the properties source.
 */
val Config.toProperties: Writer get() = PropertiesWriter(this)
