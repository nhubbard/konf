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

import io.github.nhubbard.konf.helpers.Valid
import io.github.nhubbard.konf.source.UnknownPathsException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestFailOnUnknownPath {
    //language=Json
    private val source =
        """
        {
            "level1": {
              "level2": {
                "valid": "value1",
                "invalid": "value2"
              }
            }
        }
        """.trimIndent()

    @Test
    fun testConfig_whenFeatureDisabled_shouldIgnoreUnknownPaths() {
        val config = Config {
            addSpec(Valid)
        }
        val conf = config.from.disabled(Feature.FAIL_ON_UNKNOWN_PATH).json.string(source)
        assertEquals("value1", conf[Valid.valid])
    }

    @Test
    fun testConfig_whenFeatureEnabledOnConfig_shouldThrowUnknownPathsException() {
        val config = Config {
            addSpec(Valid)
        }.enable(Feature.FAIL_ON_UNKNOWN_PATH)
        val e = assertCheckedThrows<UnknownPathsException> { config.from.json.string(source) }
        assertEquals(listOf("level1.level2.invalid"), e.paths)
    }

    @Test
    fun testConfig_whenFeatureEnabledOnSource_shouldThrowUnknownPathsException() {
        val config = Config {
            addSpec(Valid)
        }
        val e = assertCheckedThrows<UnknownPathsException> {
            config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).json.string(source)
        }
        assertEquals(listOf("level1.level2.invalid"), e.paths)
    }
}