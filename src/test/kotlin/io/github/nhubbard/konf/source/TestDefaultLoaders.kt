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
import io.github.nhubbard.konf.singleArgumentsOf
import io.github.nhubbard.konf.source.helpers.DefaultLoadersConfig
import io.github.nhubbard.konf.source.helpers.Sequential
import io.github.nhubbard.konf.source.helpers.propertiesContent
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import io.github.nhubbard.konf.tempFileOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import spark.Service
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertSame

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestDefaultLoaders {
    companion object {
        @JvmStatic
        val item = DefaultLoadersConfig.type

        @JvmStatic
        fun defaultLoadersSource(): Stream<Arguments> = singleArgumentsOf(
            // DefaultLoadersSpec
            {
                Config {
                    addSpec(DefaultLoadersConfig)
                }.from
            },
            // MappedDefaultLoadersSpec
            {
                Config {
                    addSpec(DefaultLoadersConfig["source"])
                }.from.mapped { it["source"] }
            },
            // PrefixedDefaultLoadersSpec
            {
                Config {
                    addSpec(DefaultLoadersConfig.withPrefix("prefix"))
                }.from.prefixed("prefix")
            },
            // ScopedDefaultLoadersSpec
            {
                Config {
                    addSpec(DefaultLoadersConfig["source"])
                }.from.scoped("source")
            }
        )
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromEnvMap_itShouldHaveCorrectValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        val config = subject.envMap(mapOf("SOURCE_TEST_TYPE" to "env"))
        assertEquals(config[item], "env")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromSysEnv_itShouldHaveCorrectValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        val config = subject.env()
        assertEquals(config[item], "env")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromSystemProps_itShouldHaveCorrectValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        System.setProperty(DefaultLoadersConfig.qualify(DefaultLoadersConfig.type), "system")
        val config = subject.systemProperties()
        assertEquals(config[item], "system")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onDispatchLoaderBasedOnExtension_itShouldThrowUnsupportedExtensionExceptionOnUnsupported(provider: () -> DefaultLoaders) {
        val subject = provider()
        assertThrows<UnsupportedExtensionException> {
            subject.dispatchExtension("txt")
        }
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onDispatchLoaderBasedOnExtension_itShouldReturnCorrectLoaderForExtension(provider: () -> DefaultLoaders) {
        val subject = provider()
        val extension = UUID.randomUUID().toString()
        Provider.registerExtension(extension, PropertiesProvider)
        subject.dispatchExtension(extension)
        Provider.unregisterExtension(extension)
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromProvider_itShouldLoadWithTheProvider(provider: () -> DefaultLoaders) {
        val subject = provider()
        val config = subject.source(PropertiesProvider).file(tempFileOf(propertiesContent, suffix = ".properties"))
        assertEquals(config[item], "properties")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromProvider_itShouldBuildANewLayerOnTheParentConfig(provider: () -> DefaultLoaders) {
        val subject = provider()
        val config = subject.source(PropertiesProvider).file(tempFileOf(propertiesContent, suffix = ".properties"))
        assertSame(config.parent!!, subject.config)
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromURL_itShouldLoadAsAutoDetectedURLFormat(provider: () -> DefaultLoaders) {
        val subject = provider()
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> propertiesContent }
        service.awaitInitialization()
        val config = subject.url(URI("http://localhost:${service.port()}/source.properties").toURL())
        assertEquals(config[item], "properties")
        service.stop()
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromURLString_isShouldLoadAsAutoDetectedURLFormat(provider: () -> DefaultLoaders) {
        val subject = provider()
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> propertiesContent }
        service.awaitInitialization()
        val config = subject.url("http://localhost:${service.port()}/source.properties")
        assertEquals(config[item], "properties")
        service.stop()
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromFile_itShouldLoadAsAutoDetectedFileFormat(provider: () -> DefaultLoaders) {
        val subject = provider()
        val config = subject.file(tempFileOf(propertiesContent, suffix = ".properties"))
        assertEquals(config[item], "properties")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromFilePath_itShouldLoadAsAutoDetectedFileFormat(provider: () -> DefaultLoaders) {
        val subject = provider()
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        val config = subject.file(file.path)
        assertEquals(config[item], "properties")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedFile_itShouldHaveOldAndNewValue(provider: () -> DefaultLoaders) {
        val subject = provider()
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        val config = subject.watchFile(file, 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        file.writeText(propertiesContent.replace("properties", "newValue"))
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(5))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedFileWithDefaultDelayTime_itShouldHaveOldAndNewValue(provider: () -> DefaultLoaders) {
        val subject = provider()
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        val config = subject.watchFile(file, delayTime = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        file.writeText(propertiesContent.replace("properties", "newValue"))
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(5))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedFileWithListener_itShouldLoadNewValue(provider: () -> DefaultLoaders) {
        val subject = provider()
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        var newValue = ""
        subject.watchFile(
            file,
            1,
            unit = TimeUnit.SECONDS,
            context = Dispatchers.Sequential
        ) { config, _ ->
            newValue = config[item]
        }
        file.writeText(propertiesContent.replace("properties", "newValue"))
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(5))
        }
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedFilePath_itShouldHaveOldAndNewValue(provider: () -> DefaultLoaders) {
        val subject = provider()
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        val config = subject.watchFile(file.path, 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        file.writeText(propertiesContent.replace("properties", "newValue"))
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(2))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedFilePathWithDefaultDelayTime_itShouldHaveOldAndNewValue(provider: () -> DefaultLoaders) {
        val subject = provider()
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        val config = subject.watchFile(file.path, delayTime = 1, context = Dispatchers.Sequential)
        val originalValue = config[item]
        file.writeText(propertiesContent.replace("properties", "newValue"))
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(2))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedURL_itShouldHaveOldAndNewValue(provider: () -> DefaultLoaders) {
        val subject = provider()
        var content = propertiesContent
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> content }
        service.awaitInitialization()
        val url = "http://localhost:${service.port()}/source.properties"
        val config = subject.watchUrl(URI(url).toURL(), period = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        content = propertiesContent.replace("properties", "newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(2))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedURLWithDefaultDelayTime_itShouldHaveBothValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        var content = propertiesContent
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> content }
        service.awaitInitialization()
        val url = "http://localhost:${service.port()}/source.properties"
        val config = subject.watchUrl(URI(url).toURL(), period = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        content = propertiesContent.replace("properties", "newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(2))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedURLString_itShouldHaveBothValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        var content = propertiesContent
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> content }
        service.awaitInitialization()
        val url = "http://localhost:${service.port()}/source.properties"
        val config = subject.watchUrl(url, period = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        content = propertiesContent.replace("properties", "newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(2))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedURLStringWithDefaultDelayTime_itShouldHaveBothValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        var content = propertiesContent
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> content }
        service.awaitInitialization()
        val url = URI("http://localhost:${service.port()}/source.properties").toURL()
        val config = subject.watchUrl(url, period = 1, unit = TimeUnit.SECONDS, context = Dispatchers.Sequential)
        val originalValue = config[item]
        content = propertiesContent.replace("properties", "newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(2))
        }
        val newValue = config[item]
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromWatchedURLStringWithListener_itShouldHaveBothValues(provider: () -> DefaultLoaders) {
        val subject = provider()
        var content = propertiesContent
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> content }
        service.awaitInitialization()
        val url = "http://localhost:${service.port()}/source.properties"
        var newValue = ""
        val config = subject.watchUrl(
            url,
            period = 1,
            unit = TimeUnit.SECONDS,
            context = Dispatchers.Sequential
        ) { config, _ ->
            newValue = config[item]
        }
        val originalValue = config[item]
        content = propertiesContent.replace("properties", "newValue")
        runBlocking(Dispatchers.Sequential) {
            delay(TimeUnit.SECONDS.toMillis(1))
        }
        assertEquals(originalValue, "properties")
        assertEquals(newValue, "newValue")
    }

    @ParameterizedTest
    @MethodSource("defaultLoadersSource")
    fun testLoader_onLoadFromMap_itShouldUseTheSameConfig(provider: () -> DefaultLoaders) {
        val subject = provider()
        assertSame(subject.config, subject.map.config)
    }
}