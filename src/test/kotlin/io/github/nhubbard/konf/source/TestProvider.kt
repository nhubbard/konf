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

package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.singleArgumentsOf
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import io.github.nhubbard.konf.tempFileOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import spark.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.util.stream.Stream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestProvider {
    companion object {
        @JvmStatic
        fun providerSource(): Stream<Arguments> = singleArgumentsOf(
            { PropertiesProvider },
            { PropertiesProvider.map { source -> source.withPrefix("prefix")["prefix"] } }
        )
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromReader_itShouldReturnASourceWithValueFromReader(provider: () -> Provider) {
        val subject = provider()
        val source = subject.reader("type = reader".reader())
        assertEquals(source["type"].asValue<String>(), "reader")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromInputStream_itShouldReturnASourceWithValueFromInputStream(provider: () -> Provider) {
        val subject = provider()
        val source = subject.inputStream(
            tempFileOf("type = inputStream").inputStream()
        )
        assertEquals(source["type"].asValue<String>(), "inputStream")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFile_itShouldCreateFromTheSpecifiedFile(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = file")
        val source = subject.file(file)
        assertEquals(source.info["file"], file.toString())
        assertEquals(source["type"].asValue<String>(), "file")
        assertTrue(file.delete())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentFile_itShouldThrowException(provider: () -> Provider) {
        val subject = provider()
        assertThrows<FileNotFoundException> {
            subject.file(File("not_existed.json"))
        }
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentFile_itShouldReturnAnEmptySourceIfOptional(provider: () -> Provider) {
        val subject = provider()
        assertEquals(subject.file(File("not_existed.json"), optional = true).tree.children, mutableMapOf())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFilePath_itShouldCreateFromTheSpecifiedFilePath(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = file").toString()
        val source = subject.file(file)
        assertEquals(source.info["file"], file)
        assertEquals(source["type"].asValue<String>(), "file")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentFilePath_itShouldThrowException(provider: () -> Provider) {
        val subject = provider()
        assertThrows<FileNotFoundException> {
            subject.file("non_existed.json")
        }
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentFilePath_itShouldReturnAnEmptySourceIfOptional(provider: () -> Provider) {
        val subject = provider()
        assertEquals(subject.file("not_existed.json", optional = true).tree.children, mutableMapOf())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromString_itShouldCreateFromTheSpecifiedString(provider: () -> Provider) {
        val subject = provider()
        val content = "type = string"
        val source = subject.string(content)
        assertEquals(source.info["content"], "\"\n$content\n\"")
        assertEquals(source["type"].asValue<String>(), "string")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromByteArray_itShouldReturnASourceWhichContainsTheValueInTheByteArray(provider: () -> Provider) {
        val subject = provider()
        val source = subject.bytes("type = bytes".toByteArray())
        assertEquals(source["type"].asValue<String>(), "bytes")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromByteArraySlice_itShouldReturnASourceWhichContainsTheValueInTheByteArraySlice(provider: () -> Provider) {
        val subject = provider()
        val source = subject.bytes("|type = slice|".toByteArray(), 1, 12)
        assertEquals(source["type"].asValue<String>(), "slice")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromHTTPUrl_itShouldCreateFromTheSpecifiedURL(provider: () -> Provider) {
        val subject = provider()
        val service = Service.ignite()
        service.port(0)
        service.get("/source") { _, _ -> "type = http" }
        service.awaitInitialization()
        val urlPath = "http://localhost:${service.port()}/source"
        val source = subject.url(URI(urlPath).toURL())
        assertEquals(source.info["url"], urlPath)
        service.stop()
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromHTTPUrl_itShouldReturnASourceWhichContainsTheValueInURL(provider: () -> Provider) {
        val subject = provider()
        val service = Service.ignite()
        service.port(0)
        service.get("/source") { _, _ -> "type = http" }
        service.awaitInitialization()
        val urlPath = "http://localhost:${service.port()}/source"
        val source = subject.url(URI(urlPath).toURL())
        assertEquals(source["type"].asValue<String>(), "http")
        service.stop()
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentURL_itShouldThrow(provider: () -> Provider) {
        val subject = provider()
        assertThrows<IOException> { subject.url(URI("http://localhost/not_existed.json").toURL()) }
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentURL_itShouldReturnEmptySourceForOptional(provider: () -> Provider) {
        val subject = provider()
        assertEquals(
            subject.url(URI("http://localhost/not_existed.json").toURL(), optional = true).tree.children,
            mutableMapOf()
        )
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFileURL_itShouldCreateFromTheSpecifiedURL(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = fileUrl")
        val url = file.toURI().toURL()
        val source = subject.url(url)
        assertEquals(source.info["url"], url.toString())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFileURL_itShouldReturnASourceWhichContainsValueInURL(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = fileUrl")
        val url = file.toURI().toURL()
        val source = subject.url(url)
        assertEquals(source["type"].asValue<String>(), "fileUrl")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFileURL_itShouldNotLockTheFile(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = fileUrl")
        val url = file.toURI().toURL()
        subject.url(url)
        assertTrue(file.delete())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentFileUrl_itShouldThrow(provider: () -> Provider) {
        val subject = provider()
        assertThrows<FileNotFoundException> { subject.url(URI("file://localhost/not_existed.json").toURL()) }
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromNonExistentFileUrl_itShouldReturnEmptySourceIfOptional(provider: () -> Provider) {
        val subject = provider()
        assertEquals(
            subject.url(URI("file://localhost/not_existed.json").toURL(), optional = true).tree.children,
            mutableMapOf()
        )
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFileURLString_itShouldCreateFromTheSpecifiedURLString(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = fileUrl")
        val url = file.toURI().toURL().toString()
        val source = subject.url(url)
        assertEquals(source.info["url"], url)
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromFileURLString_itShouldReturnASourceWhichContainsValueInURL(provider: () -> Provider) {
        val subject = provider()
        val file = tempFileOf("type = fileUrl")
        val url = file.toURI().toURL().toString()
        val source = subject.url(url)
        assertEquals(source["type"].asValue<String>(), "fileUrl")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromMissingFileURLString_itShouldThrow(provider: () -> Provider) {
        val subject = provider()
        assertThrows<FileNotFoundException> { subject.url("file://localhost/not_existed.json") }
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromMissingFileURLString_itShouldReturnEmptySourceIfOptional(provider: () -> Provider) {
        val subject = provider()
        assertEquals(
            subject.url(URI("file://localhost/not_existed.json").toURL(), optional = true).tree.children,
            mutableMapOf()
        )
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromResource_itShouldCreateFromTheSpecifiedResource(provider: () -> Provider) {
        val subject = provider()
        val resource = "source/provider.properties"
        val source = subject.resource(resource)
        assertEquals(source.info["resource"], resource)
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromResource_itShouldReturnASourceWhichContainsValueInResource(provider: () -> Provider) {
        val subject = provider()
        val resource = "source/provider.properties"
        val source = subject.resource(resource)
        assertEquals(source["type"].asValue<String>(), "resource")
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromMissingResource_itShouldThrow(provider: () -> Provider) {
        val subject = provider()
        assertThrows<SourceNotFoundException> { subject.resource("source/no-provider.properties") }
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testProvider_onCreateSourceFromMissingResource_itShouldReturnAnEmptySourceIfOptional(provider: () -> Provider) {
        val subject = provider()
        assertEquals(
            subject.resource("source/no-provider.properties", optional = true).tree.children,
            mutableMapOf()
        )
    }
}