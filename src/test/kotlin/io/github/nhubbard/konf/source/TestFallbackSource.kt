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

import io.github.nhubbard.konf.name
import io.github.nhubbard.konf.source.base.asKVSource
import io.github.nhubbard.konf.toPath
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestFallbackSource {
    @Test
    fun testSourceWithFallback_itContainsFacadeAndFallbackInfo() {
        val facadeSource = 1.asSource()
        val fallbackSource = 2.asSource()
        val source = facadeSource.withFallback(fallbackSource)
        assertEquals(source.info["facade"], facadeSource.description)
        assertEquals(source.info["fallback"], fallbackSource.description)
    }

    @Test
    fun testSourceWithFallback_onPathKeyInFacadeSource_itShouldGetValueFromFacadeSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadeSource = mapOf(key to "facade").asKVSource()
        val source = facadeSource.withFallback(fallbackSource)
        assertTrue(path in source)
        assertTrue(key in source)
        assertEquals(facadeSource[path].asValue<String>(), source[path].asValue<String>())
        assertEquals(facadeSource[key].asValue<String>(), source[key].asValue<String>())
        assertEquals(facadeSource.getOrNull(path)?.asValue<String>(), source.getOrNull(path)?.asValue<String>())
        assertEquals(facadeSource.getOrNull(key)?.asValue<String>(), source.getOrNull(key)?.asValue<String>())
    }

    @Test
    fun testSourceWithFallback_onPathKeyInFallbackSource_itShouldGetValueFromFallbackSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadePath = listOf("a", "c")
        val facadeKey = facadePath.name
        val facadeSource = mapOf(facadeKey to "facade").asKVSource()
        val source = facadeSource.withFallback(fallbackSource)
        assertTrue(path in source)
        assertTrue(key in source)
        assertEquals(fallbackSource[path].asValue<String>(), source[path].asValue<String>())
        assertEquals(fallbackSource[key].asValue<String>(), source[key].asValue<String>())
        assertEquals(fallbackSource.getOrNull(path)?.asValue<String>(), source.getOrNull(path)?.asValue<String>())
        assertEquals(fallbackSource.getOrNull(key)?.asValue<String>(), source.getOrNull(key)?.asValue<String>())
    }

    @Test
    fun testSourceWithFallback_onPathKeyInFallbackSource_itShouldContainValueInFacadeSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadePath = listOf("a", "c")
        val facadeKey = facadePath.name
        val facadeSource = mapOf(facadeKey to "facade").asKVSource()
        val source = facadeSource.withFallback(fallbackSource)
        assertTrue(facadePath in source)
        assertTrue(facadeKey in source)
    }

    @Test
    fun testSourceWithFallback_onPathKeyInFallbackSource_itShouldNotContainValueWhichIsNotInEitherSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadePath = listOf("a", "c")
        val facadeKey = facadePath.name
        val facadeSource = mapOf(facadeKey to "facade").asKVSource()
        val source = facadeSource.withFallback(fallbackSource)
        assertFalse("a.d".toPath() in source)
        assertFalse("a.d" in source)
    }
}