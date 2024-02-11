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

package com.nhubbard.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import com.nhubbard.konf.source.properties.PropertiesProvider
import com.nhubbard.konf.tempFileOf
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike
import org.junit.jupiter.api.assertThrows
import spark.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import kotlin.test.assertTrue

object ProviderSpec : SubjectSpek<Provider>({
    subject { PropertiesProvider }

    given("a provider") {
        on("create source from reader") {
            val source = subject.reader("type = reader".reader())
            it("should return a source which contains value from reader") {
                assertThat(source["type"].asValue<String>(), equalTo("reader"))
            }
        }
        on("create source from input stream") {
            val source = subject.inputStream(
                tempFileOf("type = inputStream").inputStream()
            )
            it("should return a source which contains value from input stream") {
                assertThat(source["type"].asValue<String>(), equalTo("inputStream"))
            }
        }
        on("create source from file") {
            val file = tempFileOf("type = file")
            val source = subject.file(file)
            it("should create from the specified file") {
                assertThat(source.info["file"], equalTo(file.toString()))
            }
            it("should return a source which contains value in file") {
                assertThat(source["type"].asValue<String>(), equalTo("file"))
            }
            it("should not lock the file") {
                assertTrue { file.delete() }
            }
        }
        on("create source from not-existed file") {
            it("should throw exception") {
                assertThrows<FileNotFoundException> { subject.file(File("not_existed.json")) }
            }
            it("should return an empty source if optional") {
                assertThat(
                    subject.file(File("not_existed.json"), optional = true).tree.children,
                    equalTo(mutableMapOf())
                )
            }
        }
        on("create source from file path") {
            val file = tempFileOf("type = file").toString()
            val source = subject.file(file)
            it("should create from the specified file path") {
                assertThat(source.info["file"], equalTo(file))
            }
            it("should return a source which contains value in file") {
                assertThat(source["type"].asValue<String>(), equalTo("file"))
            }
        }
        on("create source from not-existed file path") {
            it("should throw exception") {
                assertThrows<FileNotFoundException> { subject.file("not_existed.json") }
            }
            it("should return an empty source if optional") {
                assertThat(
                    subject.file("not_existed.json", optional = true).tree.children,
                    equalTo(mutableMapOf())
                )
            }
        }
        on("create source from string") {
            val content = "type = string"
            val source = subject.string(content)
            it("should create from the specified string") {
                assertThat(source.info["content"], equalTo("\"\n$content\n\""))
            }
            it("should return a source which contains value in string") {
                assertThat(source["type"].asValue<String>(), equalTo("string"))
            }
        }
        on("create source from byte array") {
            val source = subject.bytes("type = bytes".toByteArray())
            it("should return a source which contains value in byte array") {
                assertThat(source["type"].asValue<String>(), equalTo("bytes"))
            }
        }
        on("create source from byte array slice") {
            val source = subject.bytes("|type = slice|".toByteArray(), 1, 12)
            it("should return a source which contains value in byte array slice") {
                assertThat(source["type"].asValue<String>(), equalTo("slice"))
            }
        }
        on("create source from HTTP URL") {
            val service = Service.ignite()
            service.port(0)
            service.get("/source") { _, _ -> "type = http" }
            service.awaitInitialization()
            val urlPath = "http://localhost:${service.port()}/source"
            val source = subject.url(URI(urlPath).toURL())
            it("should create from the specified URL") {
                assertThat(source.info["url"], equalTo(urlPath))
            }
            it("should return a source which contains value in URL") {
                assertThat(source["type"].asValue<String>(), equalTo("http"))
            }
            service.stop()
        }
        on("create source from not-existed HTTP URL") {
            it("should throw exception") {
                assertThrows<IOException> { subject.url(URI("http://localhost/not_existed.json").toURL()) }
            }
            it("should return an empty source if optional") {
                assertThat(
                    subject.url(URI("http://localhost/not_existed.json").toURL(), optional = true).tree.children,
                    equalTo(mutableMapOf())
                )
            }
        }
        on("create source from file URL") {
            val file = tempFileOf("type = fileUrl")
            val url = file.toURI().toURL()
            val source = subject.url(url)
            it("should create from the specified URL") {
                assertThat(source.info["url"], equalTo(url.toString()))
            }
            it("should return a source which contains value in URL") {
                assertThat(source["type"].asValue<String>(), equalTo("fileUrl"))
            }
            it("should not lock the file") {
                assertTrue { file.delete() }
            }
        }
        on("create source from not-existed file URL") {
            it("should throw exception") {
                assertThrows<FileNotFoundException> { subject.url(URI("file://localhost/not_existed.json").toURL()) }
            }
            it("should return an empty source if optional") {
                assertThat(
                    subject.url(URI("file://localhost/not_existed.json").toURL(), optional = true).tree.children,
                    equalTo(mutableMapOf())
                )
            }
        }
        on("create source from file URL string") {
            val file = tempFileOf("type = fileUrl")
            val url = file.toURI().toURL().toString()
            val source = subject.url(url)
            it("should create from the specified URL string") {
                assertThat(source.info["url"], equalTo(url))
            }
            it("should return a source which contains value in URL") {
                assertThat(source["type"].asValue<String>(), equalTo("fileUrl"))
            }
        }
        on("create source from not-existed file URL string") {
            it("should throw exception") {
                assertThrows<FileNotFoundException> { subject.url("file://localhost/not_existed.json") }
            }
            it("should return an empty source if optional") {
                assertThat(
                    subject.url("file://localhost/not_existed.json", optional = true).tree.children,
                    equalTo(mutableMapOf())
                )
            }
        }
        on("create source from resource") {
            val resource = "source/provider.properties"
            val source = subject.resource(resource)
            it("should create from the specified resource") {
                assertThat(source.info["resource"], equalTo(resource))
            }
            it("should return a source which contains value in resource") {
                assertThat(source["type"].asValue<String>(), equalTo("resource"))
            }
        }
        on("create source from non-existed resource") {
            it("should throw SourceNotFoundException") {
                assertThat(
                    { subject.resource("source/no-provider.properties") },
                    throws<SourceNotFoundException>()
                )
            }
            it("should return an empty source if optional") {
                assertThat(
                    subject.resource("source/no-provider.properties", optional = true).tree.children,
                    equalTo(mutableMapOf())
                )
            }
        }
    }
})

object MappedProviderSpec : SubjectSpek<Provider>({
    subject { PropertiesProvider.map { source -> source.withPrefix("prefix")["prefix"] } }

    itBehavesLike(ProviderSpec)
})
