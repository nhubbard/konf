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

package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.*
import io.github.nhubbard.konf.source.ParseException
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.asValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestFlatSource {
    @Test
    fun testFlatMapSource_givenGetOperation_onGetUnderlyingMap_itShouldReturnTheSpecifiedMap() {
        val source = FlatSource(map = mapOf("level1.level2.key" to "value"))
        assertEquals(mapOf("level1.level2.key" to "value"), source.map)
    }

    @Test
    fun testFlatMapSource_givenGetOperation_onAccessWithEmptyPath_itShouldContainThePath() {
        val source = FlatSource(map = mapOf("level1.level2.key" to "value"))
        assertTrue("".toPath() in source)
    }

    @Test
    fun testFlatMapSource_givenGetOperation_onAccessWithEmptyPath_itShouldReturnItselfInGetOrNull() {
        val source = FlatSource(map = mapOf("level1.level2.key" to "value"))
        assertEquals(source as Source, source.getOrNull("".toPath()))
    }

    @Test
    fun testFlatMapSource_givenGetOpForListValue_onEmptyStringValue_itShouldReturnAnEmptyList() {
        val source = FlatSource(
            map = mapOf(
                "empty" to "",
                "single" to "a",
                "multiple" to "a,b"
            )
        )
        assertEquals(listOf(), (source["empty"].tree as ListNode).list)
    }

    @Test
    fun testFlatMapSource_givenGetOpForListValue_onStringValueWithoutCommas_itShouldReturnListWithSingleElement() {
        val source = FlatSource(
            map = mapOf(
                "empty" to "",
                "single" to "a",
                "multiple" to "a,b"
            )
        )
        assertEquals(
            listOf("a"),
            (source["single"].tree as ListNode).list.map { (it as ValueNode).value as String }
        )
    }

    @Test
    fun testFlatMapSource_givenGetOpForListValue_onStringValueWithCommas_itShouldReturnListContainingMultipleElements() {
        val source = FlatSource(
            map = mapOf(
                "empty" to "",
                "single" to "a",
                "multiple" to "a,b"
            )
        )
        assertEquals(
            listOf("a", "b"),
            (source["multiple"].tree as ListNode).list.map { (it as ValueNode).value as String }
        )
    }

    @Test
    fun testFlatMapSource_onInvalidKey_itShouldThrowInvalidPathException() {
        assertFailsWith<InvalidPathException> {
            FlatSource(map = mapOf("level1.level2.key." to "value"))
        }
    }

    @Test
    fun testFlatMapSource_givenCastOperation_onStringValue_itShouldSucceedInCastingToString() {
        val source = FlatSource(map = mapOf("level1.key" to "value"))["level1.key"]
        assertEquals("value", source.asValue<String>())
    }

    @Test
    fun testFlatMapSource_givenCastOperation_onNonBooleanValue_itShouldThrowParseExceptionWhenCastingToBoolean() {
        val source = FlatSource(map = mapOf("level1.key" to "value"))["level1.key"]
        assertFailsWith<ParseException> { source.asValue<Boolean>() }
    }

    @Test
    fun testFlatMapSource_givenCastOperation_onNonDoubleValue_itShouldThrowParseExceptionWhenCastingToDouble() {
        val source = FlatSource(map = mapOf("level1.key" to "value"))["level1.key"]
        assertFailsWith<ParseException> { source.asValue<Double>() }
    }

    @Test
    fun testFlatMapSource_givenCastOperation_onNonIntegerValue_itShouldThrowParseExceptionWhenCastingToInteger() {
        val source = FlatSource(map = mapOf("level1.key" to "value"))["level1.key"]
        assertFailsWith<ParseException> { source.asValue<Int>() }
    }

    @Test
    fun testFlatMapSource_givenCastOperation_onNonLongValue_itShouldThrowParseExceptionWhenCastingToLong() {
        val source = FlatSource(map = mapOf("level1.key" to "value"))["level1.key"]
        assertFailsWith<ParseException> { source.asValue<Long>() }
    }

    @Test
    fun testFlatMapSource_onConfigWithListOfStringsWithCommas_itShouldNotBeJoinedIntoAString() {
        val spec = object : ConfigSpec() {
            @Suppress("unused")
            val list by optional(listOf("a,b", "c, d"))
        }
        val config = Config {
            addSpec(spec)
        }
        val map = config.toFlatMap()
        assertEquals("a,b", map["list.0"])
        assertEquals("c, d", map["list.1"])
    }

    @Test
    fun testFlatMapSource_onConfigWithListOfStringsWithoutCommas_itShouldBeJoinedIntoStringWithCommas() {
        val spec = object : ConfigSpec() {
            @Suppress("unused")
            val list by optional(listOf("a", "b", "c", "d"))
        }
        val config = Config {
            addSpec(spec)
        }
        val map = config.toFlatMap()
        assertEquals("a,b,c,d", map["list"])
    }
}