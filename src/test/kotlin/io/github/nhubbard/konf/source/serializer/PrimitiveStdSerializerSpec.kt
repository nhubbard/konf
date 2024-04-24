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
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.json.toJson
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object PrimitiveStdSerializerSpec : SubjectSpek<Config>({
    subject {
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

    given("a config") {
        val json = """
            {
              "wrapped-string" : "1234"
            }
        """.trimIndent().replace("\n", System.lineSeparator())
        on("write wrapped string to json") {
            subject[TestSerializerWrappedStringSpec.wrappedString] = TestSerializerWrappedString("1234")
            val result = subject.toJson.toText()
            it("should serialize wrapped string as string") {
                assertThat(result, equalTo(json))
            }
        }
        on("read wrapped string from json") {
            val config = subject.from.json.string(json)
            it("should deserialize wrapped string from string") {
                assertThat(config[TestSerializerWrappedStringSpec.wrappedString], equalTo(TestSerializerWrappedString("1234")))
            }
        }
    }
})
