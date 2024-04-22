package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.ParseException
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSizeInBytes {
    @Test
    fun testValidString_parsesAsValidSizeInBytes() {
        assertEquals(SizeInBytes.parse("1k").bytes, 1024L)
    }

    @Test
    fun testInitWithNegativeNumber_shouldThrowIllegalArgumentException() {
        assertThrows<IllegalArgumentException> { SizeInBytes(-1L) }
    }

    @Test
    fun testFloatNumberString_parsesAndConvertsFromDoubleToLong() {
        assertEquals(SizeInBytes.parse("1.5kB").bytes, 1500L)
    }

    @Test
    fun testParsingInvalidUnit_shouldThrowParseException() {
        assertThrows<ParseException> { SizeInBytes.parse("1kb") }
    }

    @Test
    fun testParsingInvalidNumber_shouldThrowParseException() {
        assertThrows<ParseException> { SizeInBytes.parse("*1k") }
    }

    @Test
    fun testParsingOutOfRangeNumber_shouldThrowParseException() {
        assertThrows<ParseException> { SizeInBytes.parse("1z") }
    }
}