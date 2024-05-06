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

package io.github.nhubbard.konf.source.serializer

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.json.toJson
import io.github.nhubbard.konf.source.serializer.helpers.TestSerializerWrappedString
import io.github.nhubbard.konf.source.serializer.helpers.TestSerializerWrappedStringSpec
import io.github.nhubbard.konf.source.serializer.helpers.TestSerializerWrappedStringStdDeserializer
import io.github.nhubbard.konf.source.serializer.helpers.TestSerializerWrappedStringStdSerializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestPrimitiveStdSerializer {
    private val provider = {
        Config {
            addSpec(TestSerializerWrappedStringSpec)
            mapper.registerModule(
                SimpleModule().apply {
                    addSerializer(TestSerializerWrappedString::class.java, TestSerializerWrappedStringStdSerializer())
                    addDeserializer(TestSerializerWrappedString::class.java, TestSerializerWrappedStringStdDeserializer())
                }
            )
        }
    }
    private val json = """
        {
          "wrapped-string" : "1234"
        }
    """.trimIndent().replace("\n", System.lineSeparator())

    @Test
    fun testConfig_onWriteWrappedStringToJson_itShouldSerializeWrappedStringAsString() {
        val subject = provider()
        subject[TestSerializerWrappedStringSpec.wrappedString] = TestSerializerWrappedString("1234")
        val result = subject.toJson.toText()
        assertEquals(json, result)
    }

    @Test
    fun testConfig_onReadWrappedStringFromJson_itShouldDeserializeWrappedStringFromString() {
        val subject = provider()
        val config = subject.from.json.string(json)
        assertEquals(TestSerializerWrappedString("1234"), config[TestSerializerWrappedStringSpec.wrappedString])
    }
}