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

import io.github.nhubbard.konf.helpers.InvalidWatchKey
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestInvalidWatchKey {
    // language=JSON
    private val input = """
    |{
    |  "first": "test_data",
    |  "second": "test_data"
    |}
    """.trimMargin()
    private val inputFile = tempFileOf(input, suffix = ".json").toPath()

    @BeforeAll
    fun preTestSetup() {
        inputFile.writeText(input)
    }

    @Test
    fun testWatchKey_onBecomingInvalid_itThrows() {
        // This test does not cover the correct branches on macOS
        if ("mac" !in System.getProperty("os.name").lowercase()) {
            assertFailsWith<UnsetValueException> {
                // Write valid test data to input file
                inputFile.writeText(input)
                // Open valid data
                val config = Config {
                    addSpec(InvalidWatchKey)
                }.from.json.watchFile(inputFile.toFile(), delayTime = 50, unit = TimeUnit.MILLISECONDS)
                // Make watch key invalid???
                inputFile.deleteIfExists()
                // Wait for several seconds...
                Thread.sleep(1000)
                println(config[InvalidWatchKey.first])
                println(config[InvalidWatchKey.second])
            }
        }
    }
}