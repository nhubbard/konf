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
import io.github.nhubbard.konf.source.toDuration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestParseDuration {
    @Test
    fun testParseEmptyString_shouldThrowParseException() {
        assertFailsWith<ParseException> { "".toDuration() }
    }

    @Test
    fun testParseStringWithoutUnit_shouldParseAsMilliseconds() {
        assertEquals(Duration.ofMillis(1), "1".toDuration())
    }

    @Test
    fun testParseStringWithMillisecondUnit_shouldParseAsMilliseconds() {
        assertEquals(Duration.ofMillis(1), "1ms".toDuration())
        assertEquals(Duration.ofMillis(1), "1 millis".toDuration())
        assertEquals(Duration.ofMillis(1), "1 milliseconds".toDuration())
    }

    @Test
    fun testParseStringWithMicrosecondUnit_shouldParseAsMicroseconds() {
        assertEquals(Duration.ofNanos(1000), "1us".toDuration())
        assertEquals(Duration.ofNanos(1000), "1 micros".toDuration())
        assertEquals(Duration.ofNanos(1000), "1 microseconds".toDuration())
    }

    @Test
    fun testParseStringWithNanosecondUnit_shouldParseAsNanoseconds() {
        assertEquals(Duration.ofNanos(1), "1ns".toDuration())
        assertEquals(Duration.ofNanos(1), "1 nanos".toDuration())
        assertEquals(Duration.ofNanos(1), "1 nanoseconds".toDuration())
    }

    @Test
    fun testParseStringWithDayUnit_shouldParseAsDays() {
        assertEquals(Duration.ofDays(1), "1d".toDuration())
        assertEquals(Duration.ofDays(1), "1 days".toDuration())
    }

    @Test
    fun testParseStringWithHourUnit_shouldParseAsHours() {
        assertEquals(Duration.ofHours(1), "1h".toDuration())
        assertEquals(Duration.ofHours(1), "1 hours".toDuration())
    }

    @Test
    fun testParseStringWithSecondUnit_shouldParseAsSeconds() {
        assertEquals(Duration.ofSeconds(1), "1s".toDuration())
        assertEquals(Duration.ofSeconds(1), "1 seconds".toDuration())
    }

    @Test
    fun testParseStringWithMinuteUnit_shouldParseAsMinutes() {
        assertEquals(Duration.ofMinutes(1), "1m".toDuration())
        assertEquals(Duration.ofMinutes(1), "1 minutes".toDuration())
    }

    @Test
    fun testParseStringWithFloatNumber_shouldParseAndConvertFromDoubleToLong() {
        assertEquals(Duration.ofNanos(1_500_000), "1.5ms".toDuration())
    }

    @Test
    fun testParseStringWithInvalidUnit_shouldThrowParseException() {
        assertFailsWith<ParseException> { "1x".toDuration() }
    }

    @Test
    fun testParseStringWithInvalidNumber_shouldThrowParseException() {
        assertFailsWith<ParseException> { "*1s".toDuration() }
    }
}