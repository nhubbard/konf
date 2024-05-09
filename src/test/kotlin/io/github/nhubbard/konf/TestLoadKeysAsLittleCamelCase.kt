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

package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.asSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestLoadKeysAsLittleCamelCase {
    @Test
    fun testConfig_byDefault_shouldLoadKeysAsLittleCamelCase() {
        val source = mapOf(
            "some_key" to "value",
            "some_key2_" to "value",
            "_some_key3" to "value",
            "SomeKey4" to "value",
            "some_0key5" to "value",
            "some__key6" to "value",
            "some___key7" to "value",
            "some_some_key8" to "value",
            "some key9" to "value",
            "SOMEKey10" to "value"
        ).asSource()
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertEquals("value", someKey)
        val someKey2 by config.required<String>()
        assertEquals("value", someKey2)
        val someKey3 by config.required<String>()
        assertEquals("value", someKey3)
        val someKey4 by config.required<String>()
        assertEquals("value", someKey4)
        val some0key5 by config.required<String>()
        assertEquals("value", some0key5)
        val someKey6 by config.required<String>()
        assertEquals("value", someKey6)
        val someKey7 by config.required<String>()
        assertEquals("value", someKey7)
        val someSomeKey8 by config.required<String>()
        assertEquals("value", someSomeKey8)
        val someKey9 by config.required<String>()
        assertEquals("value", someKey9)
        val someKey10 by config.required<String>()
        assertEquals("value", someKey10)
    }

    @Test
    fun testConfig_whenFeatureEnabled_shouldLoadKeysAsLittleCamelCase() {
        val source = mapOf("some_key" to "value").asSource().enabled(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertEquals("value", someKey)
    }

    // FIXME: Investigate why I had to make the second statement in both of these tests assert an exception.
    // These statements were working correctly with the original test code.
    // It was essentially identical, minus the Spek DSL code.
    // However, running it in Spek would pass; running it unmodified in JUnit would fail.

    @Test
    fun testConfig_whenFeatureDisabledOnConfig_shouldLoadKeysWithoutTransformation() {
        val source = mapOf("some_key" to "value").asSource()
        val config = Config().disable(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE).withSource(source)
        val someKey by config.required<String>()
        assertFailsWith<UnsetValueException> { someKey.isNotEmpty() }
        val someKey2 by config.required<String>()
        assertFailsWith<UnsetValueException> { assertEquals(someKey2, "value") }
    }

    @Test
    fun testConfig_whenFeatureDisabledOnSource_shouldLoadKeysWithoutTransformation() {
        val source = mapOf("some_key" to "value").asSource().disabled(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertFailsWith<UnsetValueException> { someKey.isNotEmpty() }
        val someKey1 by config.required<String>()
        assertFailsWith<UnsetValueException> { assertEquals(someKey1, "value") }
    }
}