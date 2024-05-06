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
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestFacadeSource {
    @Test
    fun testSourceWithFacade_itShouldContainFacadeAndFallbackInfo() {
        val facadeSource = 1.asSource()
        val fallbackSource = 2.asSource()
        val source = fallbackSource + facadeSource
        assertEquals(source.info["facade"], facadeSource.description)
        assertEquals(source.info["fallback"], fallbackSource.description)
    }

    @Test
    fun testSourceWithFacade_onPathKeyInFacadeSource_itGetsValueFromFacadeSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadeSource = mapOf(key to "facade").asKVSource()
        val source = fallbackSource + facadeSource
        assertTrue(path in source)
        assertTrue(key in source)
        assertEquals(source[path].asValue<String>(), facadeSource[path].asValue<String>())
        assertEquals(source[key].asValue<String>(), facadeSource[key].asValue<String>())
        assertEquals(source.getOrNull(path)?.asValue<String>(), facadeSource.getOrNull(path)?.asValue<String>())
        assertEquals(source.getOrNull(key)?.asValue<String>(), facadeSource.getOrNull(key)?.asValue<String>())
    }

    @Test
    fun testSourceWithFacade_onPathKeyInFallbackSource_itGetsValueFromFallbackSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadePath = listOf("a", "c")
        val facadeKey = facadePath.name
        val facadeSource = mapOf(facadeKey to "facade").asKVSource()
        val source = fallbackSource + facadeSource
        assertTrue(path in source)
        assertTrue(key in source)
        assertEquals(source[path].asValue<String>(), fallbackSource[path].asValue<String>())
        assertEquals(source[key].asValue<String>(), fallbackSource[key].asValue<String>())
        assertEquals(source.getOrNull(path)?.asValue<String>(), fallbackSource.getOrNull(path)?.asValue<String>())
        assertEquals(source.getOrNull(key)?.asValue<String>(), fallbackSource.getOrNull(key)?.asValue<String>())
    }

    @Test
    fun testSourceWithFacade_onPathKeyInFallbackSource_itContainsValueInFacadeSource() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadePath = listOf("a", "c")
        val facadeKey = facadePath.name
        val facadeSource = mapOf(facadeKey to "facade").asKVSource()
        val source = fallbackSource + facadeSource
        assertTrue(facadePath in source)
        assertTrue(facadeKey in source)
    }

    @Test
    fun testSourceWithFacade_onPathKeyInFallbackSource_itDoesNotContainValueWhichDoesNotExistInBothSources() {
        val path = listOf("a", "b")
        val key = path.name
        val fallbackSource = mapOf(key to "fallback").asKVSource()
        val facadePath = listOf("a", "c")
        val facadeKey = facadePath.name
        val facadeSource = mapOf(facadeKey to "facade").asKVSource()
        val source = fallbackSource + facadeSource
        assertFalse("a.d".toPath() in source)
        assertFalse("a.d" in source)
    }
}