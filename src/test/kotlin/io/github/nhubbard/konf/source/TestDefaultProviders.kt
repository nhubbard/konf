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

import io.github.nhubbard.konf.source.helpers.*
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import io.github.nhubbard.konf.tempFileOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import spark.Service
import java.net.URI
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestDefaultProviders {
    companion object {
        @JvmStatic
        val subject = Source.from

        @JvmStatic
        val item = DefaultLoadersConfig.type
    }

    @Test
    fun testProvider_onProviderSourceFromSystemEnvironment_itShouldReturnSourceThatContainsValueFromSystemEnvironment() {
        val config = subject.env().toConfig()
        assertEquals(config[item], "env")
    }

    @Test
    fun testProvider_onProvideFlattenSourceFromSystemEnvironment_itShouldReturnSourceThatContainsValue() {
        val config = subject.env(nested = false).toFlattenConfig()
        assertEquals(config[FlattenDefaultLoadersConfig.SOURCE_TEST_TYPE], "env")
    }

    @Test
    fun testProvider_onProvideSourceFromSystemProperties_itShouldReturnSourceThatContainsValue() {
        System.setProperty(DefaultLoadersConfig.qualify(DefaultLoadersConfig.type), "system")
        val config = subject.systemProperties().toConfig()
        assertEquals(config[item], "system")
    }

    @Test
    fun testProvider_onDispatchProviderBasedOnExtension_itShouldThrowWhenExtensionIsUnsupported() {
        assertThrows<UnsupportedExtensionException> { subject.dispatchExtension("txt") }
    }

    @Test
    fun testProvider_onDispatchProviderBasedOnExtension_itShouldReturnCorrespondingProviderOnRegistration() {
        val extension = UUID.randomUUID().toString()
        Provider.registerExtension(extension, PropertiesProvider)
        assertTrue(subject.dispatchExtension(extension) === (PropertiesProvider as Provider))
        Provider.unregisterExtension(extension)
    }

    @Test
    fun testProvider_onProvideSourceFromURL_itShouldProvideAsAutoDetectedURLFormat() {
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> propertiesContent }
        service.awaitInitialization()
        val config = subject.url(URI("http://localhost:${service.port()}/source.properties").toURL()).toConfig()
        assertEquals(config[item], "properties")
        service.stop()
    }

    @Test
    fun testProvider_onProvideSourceFromURLString_itShouldProvideAsAutoDetectedURLFormat() {
        val service = Service.ignite()
        service.port(0)
        service.get("/source.properties") { _, _ -> propertiesContent }
        service.awaitInitialization()
        val config = subject.url("http://localhost:${service.port()}/source.properties").toConfig()
        assertEquals(config[item], "properties")
        service.stop()
    }

    @Test
    fun testProvider_onProvideSourceFromFile_itShouldProvideAsAutoDetectedFileFormat() {
        val config = subject.file(tempFileOf(propertiesContent, suffix = ".properties")).toConfig()
        assertEquals(config[item], "properties")
    }

    @Test
    fun testProvider_onProvideSourceFromFilePath_itShouldProvideAsAutoDetectedFileFormat() {
        val file = tempFileOf(propertiesContent, suffix = ".properties")
        val config = subject.file(file.path).toConfig()
        assertEquals(config[item], "properties")
    }
}