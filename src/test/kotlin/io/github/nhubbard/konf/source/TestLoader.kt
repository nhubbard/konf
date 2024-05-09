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

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.Sequential
import io.github.nhubbard.konf.source.helpers.SourceType
import io.github.nhubbard.konf.tempFileOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import spark.Service
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestLoader {
    companion object {
        @JvmStatic
        val parentConfig = Config { addSpec(SourceType) }

        @JvmStatic
        val provider = { parentConfig.from.properties }
    }

    @Test
    fun testLoader_itShouldForkFromParentConfig() {
        val subject = provider()
        assertEquals(parentConfig, subject.config)
    }

    @Test
    fun testLoader_onLoadFromReader_itShouldReturnCorrectValues() {
        val subject = provider()
        val config = subject.reader("type = reader".reader())
        assertEquals("reader", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromInputStream_itShouldReturnCorrectValues() {
        val subject = provider()
        val config = subject.inputStream(tempFileOf("type = inputStream").inputStream())
        assertEquals("inputStream", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromFile_itShouldReturnCorrectValues() {
        val subject = provider()
        val config = subject.file(tempFileOf("type = file"))
        assertEquals("file", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromFilePath_itShouldReturnCorrectValues() {
        val subject = provider()
        val config = subject.file(tempFileOf("type = file").toString())
        assertEquals("file", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromWatchedFile_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val config = subject.watchFile(file, delayTime = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(1)) }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileOnMacOS_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val os = System.getProperty("os.name")
        System.setProperty("os.name", "mac")
        val file = tempFileOf("type = originalValue")
        val config = subject.watchFile(file, delayTime = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(1)) }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
        System.setProperty("os.name", os)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileWithDefaultDelayTime_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val config = subject.watchFile(file, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(5)) }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileWithListener_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        var newValue = ""
        subject.watchFile(
            file,
            delayTime = 1,
            unit = TimeUnit.SECONDS,
            context = Dispatchers.Sequential
        ) { config, _ -> newValue = config[SourceType.type] }
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(1)) }
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFilePath_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val config = subject.watchFile(file.toString(), delayTime = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFilePathWithDefaultDelayTime_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val config = subject.watchFile(file.toString(), context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(5)) }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromString_itShouldHaveCorrectValue() {
        val subject = provider()
        val config = subject.string("type = string")
        assertEquals("string", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromByteArray_itShouldHaveCorrectValue() {
        val subject = provider()
        val config = subject.bytes("type = bytes".toByteArray())
        assertEquals("bytes", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromByteArraySlice_itShouldHaveCorrectValue() {
        val subject = provider()
        val config = subject.bytes("|type = slice|".toByteArray(), 1, 12)
        assertEquals("slice", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromHTTPUrl_itShouldHaveCorrectValue() {
        val subject = provider()
        val service = Service.ignite()
        service.port(0)
        service.get("/source") { _, _ -> "type = http" }
        service.awaitInitialization()
        val config = subject.url("http://localhost:${service.port()}/source")
        assertEquals("http", config[SourceType.type])
        service.stop()
    }

    @Test
    fun testLoader_onLoadFromFileURL_itShouldHaveCorrectValue() {
        val subject = provider()
        val file = tempFileOf("type = fileUrl")
        val config = subject.url(file.toURI().toURL())
        assertEquals("fileUrl", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromFileURLString_itShouldHaveCorrectValue() {
        val subject = provider()
        val url = tempFileOf("type = fileUrl").toURI().toURL().toString()
        val config = subject.url(url)
        assertEquals("fileUrl", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromWatchedHTTPUrl_itShouldHaveOldAndNewValues() {
        val subject = provider()
        var content = "type = originalValue"
        val service = Service.ignite()
        service.port(0)
        service.get("/source") { _, _ -> content }
        service.awaitInitialization()
        val url = "http://localhost:${service.port()}/source"
        val config = subject.watchUrl(url, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        content = "type = newValue"
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(5)) }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
        service.stop()
    }

    @Test
    fun testLoader_onLoadFromWatchedFileURL_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val config = subject.watchUrl(file.toURI().toURL(), period = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileURLWithDefaultDelayTime_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val config = subject.watchUrl(file.toURI().toURL(), context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(5))
        }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileURLWithListener_itShouldHaveOldAndNewValue() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        var newValue = ""
        val config = subject.watchUrl(
            file.toURI().toURL(),
            period = 1,
            unit = TimeUnit.SECONDS,
            context = Dispatchers.Sequential
        ) { config, _ -> newValue = config[SourceType.type] }
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(1)) }
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileURLString_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val url = file.toURI().toURL()
        val config = subject.watchUrl(url.toString(), period = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromWatchedFileURLStringWithDefaultDelayTime_itShouldHaveOldAndNewValues() {
        val subject = provider()
        val file = tempFileOf("type = originalValue")
        val url = file.toURI().toURL()
        val config = subject.watchUrl(url.toString(), context = Dispatchers.Sequential)
        val originalValue = config[SourceType.type]
        file.writeText("type = newValue")
        runBlocking(Dispatchers.Sequential) { delay(TimeUnit.SECONDS.toMillis(5)) }
        val newValue = config[SourceType.type]
        assertEquals("originalValue", originalValue)
        assertEquals("newValue", newValue)
    }

    @Test
    fun testLoader_onLoadFromResource_itShouldHaveCorrectValue() {
        val subject = provider()
        val config = subject.resource("source/provider.properties")
        assertEquals("resource", config[SourceType.type])
    }

    @Test
    fun testLoader_onLoadFromMissingResource_itShouldThrow() {
        val subject = provider()
        assertFailsWith<SourceNotFoundException> {
            subject.resource("source/no-provider.properties")
        }
    }
}