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

package com.nhubbard.konfig.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhubbard.konfig.Config
import com.nhubbard.konfig.ConfigSpec
import com.nhubbard.konfig.source.properties.toProperties
import com.nhubbard.konfig.tempFile
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.nio.charset.Charset
import kotlin.test.assertTrue

object WriterSpec : SubjectSpek<Writer>({
    subject {
        val config = Config {
            addSpec(
                object : ConfigSpec() {
                    val key by optional("value")
                }
            )
        }
        config.toProperties
    }

    given("a writer") {
        val expectedString = "key=value" + System.lineSeparator()
        on("save to string") {
            val string = subject.toText()
            it("should return a string which contains content from config") {
                assertThat(string, equalTo(expectedString))
            }
        }
        on("save to byte array") {
            val byteArray = subject.toBytes()
            it("should return a byte array which contains content from config") {
                assertThat(byteArray.toString(Charset.defaultCharset()), equalTo(expectedString))
            }
        }
        on("save to writer") {
            val writer = StringWriter()
            subject.toWriter(writer)
            it("should return a writer which contains content from config") {
                assertThat(writer.toString(), equalTo(expectedString))
            }
        }
        on("save to output stream") {
            val outputStream = ByteArrayOutputStream()
            subject.toOutputStream(outputStream)
            it("should return an output stream which contains content from config") {
                assertThat(outputStream.toString(), equalTo(expectedString))
            }
        }
        on("save to file") {
            val file = tempFile()
            subject.toFile(file)
            it("should return a file which contains content from config") {
                assertThat(file.readText(), equalTo(expectedString))
            }
            it("should not lock the file") {
                assertTrue { file.delete() }
            }
        }
        on("save to file by path") {
            val file = tempFile()
            val path = file.toString()
            subject.toFile(path)
            it("should return a file which contains content from config") {
                assertThat(file.readText(), equalTo(expectedString))
            }
        }
    }
})
