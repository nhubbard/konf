package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.ParseException
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSizeInBytes {
    @Test
    fun testValidStringParsesAsValidSizeInBytes() {
        assertEquals(SizeInBytes.parse("1k").bytes, 1024L)
    }

    @Test
    fun testInitWithNegativeNumberShouldThrowIllegalArgumentException() {
        assertThrows<IllegalArgumentException> {
            SizeInBytes(-1L)
        }
    }

    @Test
    fun testFloatNumberStringParsesAndConvertsFromDoubleToLong() {
        assertEquals(SizeInBytes.parse("1.5kB").bytes, 1500L)
    }

    @Test
    fun testParsingInvalidUnitThrowsParseException() {
        assertThrows<ParseException> {
            SizeInBytes.parse("1kb")
        }
    }

    @Test
    fun testParsingInvalidNumberThrowsParseException() {
        assertThrows<ParseException> {
            SizeInBytes.parse("*1k")
        }
    }

    @Test
    fun testParsingOutOfRangeNumberThrowsParseException() {
        assertThrows<ParseException> {
            SizeInBytes.parse("1z")
        }
    }
}