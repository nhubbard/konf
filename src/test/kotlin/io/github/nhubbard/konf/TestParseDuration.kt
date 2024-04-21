package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.ParseException
import io.github.nhubbard.konf.source.toDuration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestParseDuration {
    @Test
    fun testParseEmptyStringThrowsParseException() {
        assertThrows<ParseException> { "".toDuration() }
    }

    @Test
    fun testParseStringWithoutUnitParsedAsMilliseconds() {
        assertEquals("1".toDuration(), Duration.ofMillis(1))
    }

    @Test
    fun testParseStringWithMillisecondUnitParsedAsMilliseconds() {
        assertEquals("1ms".toDuration(), Duration.ofMillis(1))
        assertEquals("1 millis".toDuration(), Duration.ofMillis(1))
        assertEquals("1 milliseconds".toDuration(), Duration.ofMillis(1))
    }

    @Test
    fun testParseStringWithMicrosecondUnitParsedAsMicroseconds() {
        assertEquals("1us".toDuration(), Duration.ofNanos(1000))
        assertEquals("1 micros".toDuration(), Duration.ofNanos(1000))
        assertEquals("1 microseconds".toDuration(), Duration.ofNanos(1000))
    }

    @Test
    fun testParseStringWithNanosecondUnitParsedAsNanoseconds() {
        assertEquals("1ns".toDuration(), Duration.ofNanos(1))
        assertEquals("1 nanos".toDuration(), Duration.ofNanos(1))
        assertEquals("1 nanoseconds".toDuration(), Duration.ofNanos(1))
    }

    @Test
    fun testParseStringWithDayUnitParsedAsDays() {
        assertEquals("1d".toDuration(), Duration.ofDays(1))
        assertEquals("1 days".toDuration(), Duration.ofDays(1))
    }

    @Test
    fun testParseStringWithHourUnitParsedAsHours() {
        assertEquals("1h".toDuration(), Duration.ofHours(1))
        assertEquals("1 hours".toDuration(), Duration.ofHours(1))
    }

    @Test
    fun testParseStringWithSecondUnitAsSeconds() {
        assertEquals("1s".toDuration(), Duration.ofSeconds(1))
        assertEquals("1 seconds".toDuration(), Duration.ofSeconds(1))
    }

    @Test
    fun testParseStringWithMinuteUnitAsMinutes() {
        assertEquals("1m".toDuration(), Duration.ofMinutes(1))
        assertEquals("1 minutes".toDuration(), Duration.ofMinutes(1))
    }

    @Test
    fun testParseStringWithFloatNumberParsesAndConvertsFromDoubleToLong() {
        assertEquals("1.5ms".toDuration(), Duration.ofNanos(1_500_000))
    }

    @Test
    fun testParseStringWithInvalidUnitThrowsParseException() {
        assertThrows<ParseException> { "1x".toDuration() }
    }

    @Test
    fun testParseStringWithInvalidNumberThrowsParseException() {
        assertThrows<ParseException> { "*1s".toDuration() }
    }
}