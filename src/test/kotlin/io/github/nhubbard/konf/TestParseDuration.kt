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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.Duration
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestParseDuration {
    @Test
    fun testParseEmptyString_shouldThrowParseException() {
        assertThrows<ParseException> { "".toDuration() }
    }

    @Test
    fun testParseStringWithoutUnit_shouldParseAsMilliseconds() {
        assertEquals("1".toDuration(), Duration.ofMillis(1))
    }

    @Test
    fun testParseStringWithMillisecondUnit_shouldParseAsMilliseconds() {
        assertEquals("1ms".toDuration(), Duration.ofMillis(1))
        assertEquals("1 millis".toDuration(), Duration.ofMillis(1))
        assertEquals("1 milliseconds".toDuration(), Duration.ofMillis(1))
    }

    @Test
    fun testParseStringWithMicrosecondUnit_shouldParseAsMicroseconds() {
        assertEquals("1us".toDuration(), Duration.ofNanos(1000))
        assertEquals("1 micros".toDuration(), Duration.ofNanos(1000))
        assertEquals("1 microseconds".toDuration(), Duration.ofNanos(1000))
    }

    @Test
    fun testParseStringWithNanosecondUnit_shouldParseAsNanoseconds() {
        assertEquals("1ns".toDuration(), Duration.ofNanos(1))
        assertEquals("1 nanos".toDuration(), Duration.ofNanos(1))
        assertEquals("1 nanoseconds".toDuration(), Duration.ofNanos(1))
    }

    @Test
    fun testParseStringWithDayUnit_shouldParseAsDays() {
        assertEquals("1d".toDuration(), Duration.ofDays(1))
        assertEquals("1 days".toDuration(), Duration.ofDays(1))
    }

    @Test
    fun testParseStringWithHourUnit_shouldParseAsHours() {
        assertEquals("1h".toDuration(), Duration.ofHours(1))
        assertEquals("1 hours".toDuration(), Duration.ofHours(1))
    }

    @Test
    fun testParseStringWithSecondUnit_shouldParseAsSeconds() {
        assertEquals("1s".toDuration(), Duration.ofSeconds(1))
        assertEquals("1 seconds".toDuration(), Duration.ofSeconds(1))
    }

    @Test
    fun testParseStringWithMinuteUnit_shouldParseAsMinutes() {
        assertEquals("1m".toDuration(), Duration.ofMinutes(1))
        assertEquals("1 minutes".toDuration(), Duration.ofMinutes(1))
    }

    @Test
    fun testParseStringWithFloatNumber_shouldParseAndConvertFromDoubleToLong() {
        assertEquals("1.5ms".toDuration(), Duration.ofNanos(1_500_000))
    }

    @Test
    fun testParseStringWithInvalidUnit_shouldThrowParseException() {
        assertThrows<ParseException> { "1x".toDuration() }
    }

    @Test
    fun testParseStringWithInvalidNumber_shouldThrowParseException() {
        assertThrows<ParseException> { "*1s".toDuration() }
    }
}