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

import io.github.nhubbard.konf.annotation.JavaApi
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.RegisterExtension
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.base.FlatSource
import org.dom4j.Document
import org.dom4j.io.SAXReader
import java.io.InputStream
import java.io.Reader

/**
 * Provider for the XML source.
 */
@RegisterExtension(["xml"])
object XmlProvider : Provider {
    private fun Document.toMap(): Map<String, String> {
        val rootElement = this.rootElement
        val propertyNodes = rootElement.selectNodes("/configuration/property")
        return mutableMapOf<String, String>().apply {
            for (property in propertyNodes) {
                put(property.selectSingleNode("name").text, property.selectSingleNode("value").text)
            }
        }
    }

    override fun reader(reader: Reader): Source {
        return FlatSource(SAXReader().read(reader).toMap(), type = "XML")
    }

    override fun inputStream(inputStream: InputStream): Source {
        return FlatSource(SAXReader().read(inputStream).toMap(), type = "XML")
    }

    @JavaApi
    @JvmStatic
    fun get() = this
}
