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

package io.github.nhubbard.konf.source.toml

import io.github.nhubbard.konf.singleArgumentsOf
import io.github.nhubbard.konf.source.asValue
import io.github.nhubbard.konf.tempFileOf
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestTomlProvider {
    @ParameterizedTest
    @MethodSource("providerSource")
    fun testTomlProvider_onCreateSourceFromReader_itShouldHaveCorrectType(provider: () -> TomlProvider) {
        val subject = provider()
        val source = subject.reader("type = \"reader\"".reader())
        assertEquals("TOML", source.info["type"])
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testTomlProvider_onCreateSourceFromReader_itShouldReturnASourceWithValueFromReader(provider: () -> TomlProvider) {
        val subject = provider()
        val source = subject.reader("type = \"reader\"".reader())
        assertEquals("reader", source["type"].asValue<String>())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testTomlProvider_onCreateSourceFromInputStream_itShouldHaveCorrectType(provider: () -> TomlProvider) {
        val subject = provider()
        val source = subject.inputStream(tempFileOf("type = \"inputStream\"").inputStream())
        assertEquals("TOML", source.info["type"])
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testTomlProvider_onCreateSourceFromInputStream_itShouldReturnSourceWithCorrectValue(provider: () -> TomlProvider) {
        val subject = provider()
        val source = subject.inputStream(tempFileOf("type = \"inputStream\"").inputStream())
        assertEquals("inputStream", source["type"].asValue<String>())
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testTomlProvider_onCreateSourceFromEmptyFile_itShouldReturnEmptySource(provider: () -> TomlProvider) {
        val subject = provider()
        val file = tempFileOf("")
        assertEquals(mutableMapOf(), subject.file(file).tree.children)
    }

    companion object {
        @JvmStatic
        fun providerSource(): Stream<Arguments> = singleArgumentsOf(
            { TomlProvider },
            { TomlProvider.get() }
        )
    }
}