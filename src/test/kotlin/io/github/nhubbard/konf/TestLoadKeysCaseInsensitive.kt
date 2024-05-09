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
class TestLoadKeysCaseInsensitive {
    @Test
    fun testConfig_byDefault_shouldLoadKeysCaseSensitive() {
        val source = mapOf("somekey" to "value").asSource()
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertFailsWith<UnsetValueException> { someKey.isNotEmpty() }
        val somekey by config.required<String>()
        assertEquals("value", somekey)
    }

    @Test
    fun testConfig_whenFeatureDisabled_shouldLoadKeysCaseSensitive() {
        val source = mapOf("somekey" to "value").asSource().disabled(Feature.LOAD_KEYS_CASE_INSENSITIVELY)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertFailsWith<UnsetValueException> { someKey.isNotEmpty() }
        val somekey by config.required<String>()
        assertEquals("value", somekey)
    }

    @Test
    fun testConfig_whenFeatureEnabledByConfig_shouldLoadKeysCaseInsensitive() {
        val source = mapOf("somekey" to "value").asSource()
        val config = Config().enable(Feature.LOAD_KEYS_CASE_INSENSITIVELY).withSource(source)
        val someKey by config.required<String>()
        assertEquals("value", someKey)
    }

    @Test
    fun testConfig_whenFeatureEnabledBySource_shouldLoadKeysCaseInsensitive() {
        val source = mapOf("somekey" to "value").asSource().enabled(Feature.LOAD_KEYS_CASE_INSENSITIVELY)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertEquals("value", someKey)
    }
}