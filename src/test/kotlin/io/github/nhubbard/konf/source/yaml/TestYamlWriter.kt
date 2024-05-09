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

package io.github.nhubbard.konf.source.yaml

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.debugLineEndings
import io.github.nhubbard.konf.isWindows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestYamlWriter {
    companion object {
        @JvmStatic
        val provider = {
            val config = Config {
                addSpec(
                    object : ConfigSpec() {
                        @Suppress("unused")
                        val key by optional("value")
                    }
                )
            }
            config.toYaml
        }

        @JvmStatic
        val expectedString = "key: value\n".replace("\n", System.lineSeparator())
    }

    @Test
    fun testWriter_onSaveToWriter_itShouldReturnAWriterWhichContainsContentFromConfig() {
        val subject = provider()
        val writer = StringWriter()
        subject.toWriter(writer)
        if (isWindows()) {
            println("Expected: ${expectedString.debugLineEndings()}")
            println("Actual:   ${writer.toString().debugLineEndings()}")
        }
        assertEquals(writer.toString(), expectedString)
    }

    @Test
    fun testWriter_onSaveToOutputStream_itShouldReturnAnOutputStreamWhichContainsContentFromConfig() {
        val subject = provider()
        val outputStream = ByteArrayOutputStream()
        subject.toOutputStream(outputStream)
        if (isWindows()) {
            println("Expected: ${expectedString.debugLineEndings()}")
            println("Actual:   ${outputStream.toString().debugLineEndings()}")
        }
        assertEquals(outputStream.toString(), expectedString)
    }
}