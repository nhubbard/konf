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

import io.github.nhubbard.konf.source.ParseException
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestSizeInBytes {
    @Test
    fun testValidString_parsesAsValidSizeInBytes() {
        assertEquals(1024L, SizeInBytes.parse("1k").bytes)
    }

    @Test
    fun testInitWithNegativeNumber_shouldThrowIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> { SizeInBytes(-1L) }
    }

    @Test
    fun testFloatNumberString_parsesAndConvertsFromDoubleToLong() {
        assertEquals(1500L, SizeInBytes.parse("1.5kB").bytes)
    }

    @Test
    fun testParsingInvalidUnit_shouldThrowParseException() {
        assertFailsWith<ParseException> { SizeInBytes.parse("1kb") }
    }

    @Test
    fun testParsingInvalidNumber_shouldThrowParseException() {
        assertFailsWith<ParseException> { SizeInBytes.parse("*1k") }
    }

    @Test
    fun testParsingOutOfRangeNumber_shouldThrowParseException() {
        assertFailsWith<ParseException> { SizeInBytes.parse("1z") }
    }
}