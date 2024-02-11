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

package io.github.nhubbard.konf.source.xml

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.Writer
import io.github.nhubbard.konf.source.base.toFlatMap
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import java.io.OutputStream

/**
 * Writer for the XML source.
 */
class XmlWriter(val config: Config) : Writer {
    private fun Map<String, String>.toDocument(): Document {
        val document = DocumentHelper.createDocument()
        val rootElement = document.addElement("configuration")
        for ((key, value) in this) {
            val propertyElement = rootElement.addElement("property")
            propertyElement.addElement("name").text = key
            propertyElement.addElement("value").text = value
        }
        return document
    }

    private val outputFormat = OutputFormat.createPrettyPrint().apply {
        lineSeparator = System.lineSeparator()
    }

    override fun toWriter(writer: java.io.Writer) {
        val xmlWriter = XMLWriter(writer, outputFormat)
        xmlWriter.write(config.toFlatMap().toDocument())
        xmlWriter.close()
    }

    override fun toOutputStream(outputStream: OutputStream) {
        val xmlWriter = XMLWriter(outputStream, outputFormat)
        xmlWriter.write(config.toFlatMap().toDocument())
        xmlWriter.close()
    }
}

/**
 * Returns writer for the XML source.
 */
val Config.toXml: Writer get() = XmlWriter(this)
