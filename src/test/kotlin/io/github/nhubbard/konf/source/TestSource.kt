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

package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.*
import io.github.nhubbard.konf.helpers.NetworkBuffer
import io.github.nhubbard.konf.source.base.ValueSource
import io.github.nhubbard.konf.source.base.asKVSource
import io.github.nhubbard.konf.source.base.toHierarchical
import io.github.nhubbard.konf.source.helpers.Person
import io.github.nhubbard.konf.source.helpers.assertCausedBy
import io.github.nhubbard.konf.source.helpers.loadSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestSource {
    companion object {
        @JvmStatic val value: Source = ValueSource(Unit)
        @JvmStatic val tree = value.tree
        @JvmStatic val validPath = "a.b".toPath()
        @JvmStatic val invalidPath = "a.c".toPath()
        @JvmStatic val validKey = "a"
        @JvmStatic val invalidKey = "b"
        @JvmStatic val sourceForPath  = mapOf(validPath.name to value).asKVSource()
        @JvmStatic val sourceForKey = mapOf(validKey to value).asSource()
        @JvmStatic val prefixSource = Prefix("level1.level2") + mapOf("key" to "value").asSource()
    }

    @Test
    fun testGetOp_onFindValidPath_itShouldContainTheValue() {
        assertTrue(validPath in sourceForPath)
    }

    @Test
    fun testGetOp_onFindInvalidPath_itShouldNotContainTheValue() {
        assertTrue(invalidPath !in sourceForPath)
    }

    @Test
    fun testGetOp_onGetValidPathUsingGetOrNull_itShouldReturnTheCorrespondingValue() {
        assertEquals(tree, sourceForPath.getOrNull(validPath)?.tree)
    }

    @Test
    fun testGetOp_onGetInvalidPathUsingGetOrNull_itShouldReturnNull() {
        assertNull(sourceForPath.getOrNull(invalidPath))
    }

    @Test
    fun testGetOp_onGetValidPathUsingGet_itShouldReturnTheCorrespondingValue() {
        assertEquals(tree, sourceForPath[validPath].tree)
    }

    @Test
    fun testGetOp_onGetInvalidPathUsingGet_itShouldThrowNoSuchPathException() {
        val e = assertCheckedThrows<NoSuchPathException> { sourceForPath[invalidPath] }
        assertEquals(invalidPath, e.path)
    }

    @Test
    fun testGetOp_onFindValidKey_itShouldContainTheValue() {
        assertTrue(validKey in sourceForKey)
    }

    @Test
    fun testGetOp_onFindInvalidKey_itShouldNotContainTheValue() {
        assertTrue(invalidKey !in sourceForKey)
    }

    @Test
    fun testGetOp_onGetByValidKeyUsingGetOrNull_itShouldReturnTheCorrespondingValue() {
        assertEquals(tree, sourceForKey.getOrNull(validKey)?.tree)
    }

    @Test
    fun testGetOp_onGetByInvalidKeyUsingGetOrNull_itShouldReturnNull() {
        assertNull(sourceForKey.getOrNull(invalidKey))
    }

    @Test
    fun testGetOp_onGetByValidKeyUsingGet_itShouldReturnTheCorrespondingValue() {
        assertEquals(tree, sourceForKey[validKey].tree)
    }

    @Test
    fun testGetOp_onGetByInvalidKeyUsingGet_itShouldThrowNoSuchPathException() {
        val e = assertCheckedThrows<NoSuchPathException> { sourceForKey[invalidKey] }
        assertEquals(invalidKey.toPath(), e.path)
    }

    @Test
    fun testCastOp_onCastIntToLong_itShouldSucceed() {
        val source = 1.asSource()
        assertEquals(1L, source.asValue<Long>())
    }

    @Test
    fun testCastOp_onCastShortToInt_itShouldSucceed() {
        val source = 1.toShort().asSource()
        assertEquals(1, source.asValue<Int>())
    }

    @Test
    fun testCastOp_onCastByteToShort_itShouldSucceed() {
        val source = 1.toByte().asSource()
        assertEquals(1.toShort(), source.asValue<Short>())
    }

    @Test
    fun testCastOp_onCastLongToBigInteger_itShouldSucceed() {
        val source = 1L.asSource()
        assertEquals(BigInteger.valueOf(1), source.asValue<BigInteger>())
    }

    @Test
    fun testCastOp_onCastDoubleToBigDecimal_itShouldSucceed() {
        val source = 1.5.asSource()
        assertEquals(BigDecimal.valueOf(1.5), source.asValue<BigDecimal>())
    }

    @Test
    fun testCastOp_onCastLongInRangeOfIntToInt_itShouldSucceed() {
        val source = 1L.asSource()
        assertEquals(1, source.asValue<Int>())
    }

    @Test
    fun testCastOp_onCastLongOutOfRangeOfIntToInt_itShouldThrowParseException() {
        assertFailsWith<ParseException> { Long.MAX_VALUE.asSource().asValue<Int>() }
        assertFailsWith<ParseException> { Long.MIN_VALUE.asSource().asValue<Int>() }
    }

    @Test
    fun testCastOp_onCastIntInRangeOfShortToShort_itShouldSucceed() {
        val source = 1.asSource()
        assertEquals(1.toShort(), source.asValue<Short>())
    }

    @Test
    fun testCastOp_onCastIntOutOfRangeOfShortToShort_itShouldThrowParseException() {
        assertFailsWith<ParseException> { Int.MAX_VALUE.asSource().asValue<Short>() }
        assertFailsWith<ParseException> { Int.MIN_VALUE.asSource().asValue<Short>() }
    }

    @Test
    fun testCastOp_onCastShortInRangeOfByteToByte_itShouldSucceed() {
        val source = 1.toShort().asSource()
        assertEquals(1.toByte(), source.asValue<Byte>())
    }

    @Test
    fun testCastOp_onCastShortOutOfRangeOfByteToByte_itShouldThrowParseException() {
        assertFailsWith<ParseException> { Short.MAX_VALUE.asSource().asValue<Byte>() }
        assertFailsWith<ParseException> { Short.MIN_VALUE.asSource().asValue<Byte>() }
    }

    @Test
    fun testCastOp_onCastLongInRangeOfByteToByte_itShouldSucceed() {
        val source = 1L.asSource()
        assertEquals(1L.toByte(), source.asValue<Byte>())
    }

    @Test
    fun testCastOp_onCastLongOutOfRangeOfByteToByte_itShouldThrowParseException() {
        assertFailsWith<ParseException> { Long.MAX_VALUE.asSource().asValue<Byte>() }
        assertFailsWith<ParseException> { Long.MIN_VALUE.asSource().asValue<Byte>() }
    }

    @Test
    fun testCastOp_onCastDoubleToFloat_itShouldSucceed() {
        val source = 1.5.asSource()
        assertEquals(1.5f, source.asValue<Float>())
    }

    @Test
    fun testCastOp_onCastCharToString_itShouldSucceed() {
        val source = 'a'.asSource()
        assertEquals("a", source.asValue<String>())
    }

    @Test
    fun testCastOp_onCastStringContainingSingleCharToChar_itShouldSucceed() {
        val source = "a".asSource()
        assertEquals('a', source.asValue<Char>())
    }

    @Test
    fun testCastOp_onCastStringContainingMultipleCharsToChar_itShouldThrowParseException() {
        val source = "ab".asSource()
        assertFailsWith<ParseException> { source.asValue<Char>() }
    }

    @Test
    fun testCastOp_onCastStringTrueToBoolean_itShouldSucceed() {
        val source = "true".asSource()
        assertTrue(source.asValue<Boolean>())
    }

    @Test
    fun testCastOp_onCastStringFalseToBoolean_itShouldSucceed() {
        val source = "false".asSource()
        assertFalse(source.asValue<Boolean>())
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToBoolean_itShouldThrowParseException() {
        val source = "yes".asSource()
        assertFailsWith<ParseException> { source.asValue<Boolean>() }
    }

    @Test
    fun testCastOp_onCastStringToByte_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(1.toByte(), source.asValue<Byte>())
    }

    @Test
    fun testCastOp_onCastStringToShort_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(1.toShort(), source.asValue<Short>())
    }

    @Test
    fun testCastOp_onCastStringToInt_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(1, source.asValue<Int>())
    }

    @Test
    fun testCastOp_onCastStringToLong_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(1L, source.asValue<Long>())
    }

    @Test
    fun testCastOp_onCastStringToFloat_itShouldSucceed() {
        val source = "1.5".asSource()
        assertEquals(1.5F, source.asValue<Float>())
    }

    @Test
    fun testCastOp_onCastStringToDouble_itShouldSucceed() {
        val source = "1.5F".asSource()
        assertEquals(1.5, source.asValue<Double>())
    }

    @Test
    fun testCastOp_onCastStringToBigInteger_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(1.toBigInteger(), source.asValue<BigInteger>())
    }

    @Test
    fun testCastOp_onCastStringToBigDecimal_itShouldSucceed() {
        val source = "1.5".asSource()
        assertEquals(1.5.toBigDecimal(), source.asValue<BigDecimal>())
    }

    @Test
    fun testCastOp_onCastStringToOffsetTime_itShouldSucceed() {
        val text = "10:15:30+01:00"
        val source = text.asSource()
        assertEquals(OffsetTime.parse(text), source.asValue<OffsetTime>())
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToOffsetTime_itShouldThrowParseException() {
        val text = "10:15:30"
        val source = text.asSource()
        assertFailsWith<ParseException> { source.asValue<OffsetTime>() }
    }

    @Test
    fun testCastOp_onCastStringToOffsetDateTime_itShouldSucceed() {
        val text = "2007-12-03T10:15:30+01:00"
        val source = text.asSource()
        assertEquals(OffsetDateTime.parse(text), source.asValue<OffsetDateTime>())
    }

    @Test
    fun testCastOp_onCastStringToZonedDateTime_itShouldSucceed() {
        val text = "2007-12-03T10:15:30+01:00[Europe/Paris]"
        val source = text.asSource()
        assertEquals(ZonedDateTime.parse(text), source.asValue<ZonedDateTime>())
    }

    @Test
    fun testCastOp_onCastStringToLocalDate_itShouldSucceed() {
        val text = "2007-12-03"
        val source = text.asSource()
        assertEquals(LocalDate.parse(text), source.asValue<LocalDate>())
    }

    @Test
    fun testCastOp_onCastStringToLocalTime_itShouldSucceed() {
        val text = "10:15:30"
        val source = text.asSource()
        assertEquals(LocalTime.parse(text), source.asValue<LocalTime>())
    }

    @Test
    fun testCastOp_onCastStringToLocalDateTime_itShouldSucceed() {
        val text = "2007-12-03T10:15:30"
        val source = text.asSource()
        assertEquals(LocalDateTime.parse(text), source.asValue<LocalDateTime>())
    }

    @Test
    fun testCastOp_onCastStringToYear_itShouldSucceed() {
        val text = "2007"
        val source = text.asSource()
        assertEquals(Year.parse(text), source.asValue<Year>())
    }

    @Test
    fun testCastOp_onCastStringToYearMonth_itShouldSucceed() {
        val text = "2007-12"
        val source = text.asSource()
        assertEquals(YearMonth.parse(text), source.asValue<YearMonth>())
    }

    @Test
    fun testCastOp_onCastStringToInstant_itShouldSucceed() {
        val text = "2007-12-03T10:15:30.00Z"
        val source = text.asSource()
        assertEquals(Instant.parse(text), source.asValue<Instant>())
    }

    @Test
    fun testCastOp_onCastStringToDate_itShouldSucceed() {
        val text = "2007-12-03T10:15:30.00Z"
        val source = text.asSource()
        assertEquals(Date.from(Instant.parse(text)), source.asValue<Date>())
    }

    @Test
    fun testCastOp_onCastLocalDateTimeStringToDate_itShouldSucceed() {
        val text = "2007-12-03T10:15:30"
        val source = text.asSource()
        assertEquals(
            Date.from(LocalDateTime.parse(text).toInstant(ZoneOffset.UTC)),
            source.asValue<Date>()
        )
    }

    @Test
    fun testCastOp_onCastLocalDateStringToDate_itShouldSucceed() {
        val text = "2007-12-03"
        val source = text.asSource()
        assertEquals(
            Date.from(LocalDate.parse(text).atStartOfDay().toInstant(ZoneOffset.UTC)),
            source.asValue<Date>()
        )
    }

    @Test
    fun testCastOp_onCastStringToDuration_itShouldSucceed() {
        val text = "P2DT3H4M"
        val source = text.asSource()
        assertEquals(Duration.parse(text), source.asValue<Duration>())
    }

    @Test
    fun testCastOp_onCastStringWithSimpleUnitToDuration_itShouldSucceed() {
        val text = "200ms"
        val source = text.asSource()
        assertEquals(Duration.ofMillis(200), source.asValue<Duration>())
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToDuration_itShouldThrowParseException() {
        val text = "2 year"
        val source = text.asSource()
        assertFailsWith<ParseException> { source.asValue<Duration>() }
    }

    @Test
    fun testCastOp_onCastStringToSizeInBytes_itShouldSucceed() {
        val text = "10k"
        val source = text.asSource()
        assertEquals(10240L, source.asValue<SizeInBytes>().bytes)
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToSizeInBytes_itShouldSucceed() {
        val text = "10u"
        val source = text.asSource()
        assertFailsWith<ParseException> { source.asValue<SizeInBytes>() }
    }

    @Test
    fun testCastOp_onCastSetToList_itShouldSucceed() {
        val source = setOf(1).asSource()
        assertEquals(listOf(1), source.asValue<List<Int>>())
    }

    @Test
    fun testCastOp_onCastArrayToList_itShouldSucceed() {
        val source = arrayOf(1).asSource()
        assertEquals(listOf(1), source.asValue<List<Int>>())
    }

    @Test
    fun testCastOp_onCastArrayToSet_itShouldSucceed() {
        val source = arrayOf(1).asSource()
        assertEquals(setOf(1), source.asValue<Set<Int>>())
    }

    @Test
    fun testLoadOp_onLoadFromValidSource_itShouldLoadSuccessfully() {
        val config = loadSource<Int>(1)
        assertEquals(1, config("item"))
    }

    @Test
    fun testLoadOp_onLoadConcreteMapType_itShouldLoadSuccessfully() {
        val config = loadSource<ConcurrentHashMap<String, Int>>(mapOf("1" to 1))
        assertEquals(mapOf("1" to 1), config<ConcurrentHashMap<String, Int>>("item"))
    }

    @Test
    fun testLoadOp_onLoadInvalidEnumValue_itShouldThrowLoadExceptionCausedByParseException() {
        assertCausedBy<ParseException> {
            loadSource<NetworkBuffer.Type>("NO_HEAP")
        }
    }

    @Test
    fun testLoadOp_onLoadUnsupportedSimpleTypeValue_itShouldThrowLoadExceptionCausedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            loadSource<Person>(mapOf("invalid" to "anon"))
        }
    }

    @Test
    fun testLoadOp_onLoadMapWithUnsupportedKeyType_itShouldThrowLoadExceptionCausedByUnsupportedMapKeyException() {
        assertCausedBy<UnsupportedMapKeyException> {
            loadSource<Map<Pair<Int, Int>, String>>(mapOf((1 to 1) to "1"))
        }
    }

    @Test
    fun testLoadOp_onLoadInvalidPOJOValue_itShouldThrowLoadExceptionCausedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            loadSource<Person>(mapOf("name" to Source()))
        }
    }

    @Test
    fun testLoadOp_onLoadWhenSubstituteSourceWhenLoadedIsDisabledOnConfig_itShouldNotSubstitutePathVariablesBeforeLoaded() {
        val source = mapOf("item" to mapOf("key1" to "a", "key2" to "b\${item.key1}")).asSource()
        val config = Config {
            addSpec(
                object : ConfigSpec() {
                    @Suppress("unused")
                    val item by required<Map<String, String>>()
                }
            )
        }.disable(Feature.SUBSTITUTE_SOURCE_BEFORE_LOADED).withSource(source)
        assertEquals(mapOf("key1" to "a", "key2" to "b\${item.key1}"), config("item"))
    }

    @Test
    fun testLoadOp_onLoadWhenSubstituteSourceWhenLoadedIsDisabledOnSource_itShouldSubstitutePathVariablesBeforeLoaded() {
        val source = mapOf("item" to mapOf("key1" to "a", "key2" to "b\${item.key1}")).asSource()
            .disabled(Feature.SUBSTITUTE_SOURCE_BEFORE_LOADED)
        val config = Config {
            addSpec(
                object : ConfigSpec() {
                    @Suppress("unused")
                    val item by required<Map<String, String>>()
                }
            )
        }.withSource(source)
        assertEquals(mapOf("key1" to "a", "key2" to "b\${item.key1}"), config("item"))
    }

    @Test
    fun testLoadOp_onLoadWhenSubstituteSourceWhenLoadedIsEnabled_itShouldSubstitutePathVariables() {
        val source = mapOf("item" to mapOf("key1" to "a", "key2" to "b\${item.key1}")).asSource()
        val config = Config {
            addSpec(
                object : ConfigSpec() {
                    @Suppress("unused")
                    val item by required<Map<String, String>>()
                }
            )
        }.withSource(source)
        assertTrue(Feature.SUBSTITUTE_SOURCE_BEFORE_LOADED.enabledByDefault)
        assertEquals(mapOf("key1" to "a", "key2" to "ba"), config("item"))
    }

    @Test
    fun testSubOp_onNoPathVariable_itShouldRemainUnchanged() {
        val map = mapOf("key1" to "a", "key2" to "b")
        val source = map.asSource().substituted()
        assertEquals(map, source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onSinglePathVariable_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\${key1}")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "ba"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onIntPathVariable_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to 1, "key2" to "b\${key1}", "key3" to "\${key1}")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to 1, "key2" to "b1", "key3" to 1), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onStringListPathVariable_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a,b,c", "key2" to "a\${key1}")
        val source = Source.from.map.flat(map).substituted().substituted()
        assertEquals(mapOf("key1" to "a,b,c", "key2" to "aa,b,c"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onListPathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("top" to listOf(mapOf("key1" to "a", "key2" to "b\${top.0.key1}")))
        val source = map.asSource().substituted()
        assertEquals(mapOf("top" to listOf(mapOf("key1" to "a", "key2" to "ba"))), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onIncorrectTypePathVariables_itShouldThrowWrongTypeException() {
        val map = mapOf("key1" to 1.0, "key2" to "b\${key1}")
        assertFailsWith<WrongTypeException> { map.asSource().substituted() }
    }

    @Test
    fun testSubOp_onEscapedPathVariables_itShouldNotSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\$\${key1}")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "b\${key1}"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onNestedEscapedPathVariables_itShouldNotSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\$\$\$\${key1}")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "b\$\$\${key1}"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onNestedEscapedPathVariables_andMultipleSubstitutions_itShouldEscapeOnlyOnce() {
        val map = mapOf("key1" to "a", "key2" to "b\$\$\$\${key1}")
        val source = map.asSource().substituted().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "b\$\$\${key1}"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onContainsUndefinedPathVariable_itShouldThrowUndefinedPathVariableExceptionByDefault() {
        val map = mapOf("key2" to "b\${key1}")
        val e = assertCheckedThrows<UndefinedPathVariableException> { map.asSource().substituted() }
        assertEquals("b\${key1}", e.text)
    }

    @Test
    fun testSubOp_onContainsUndefinedPathVariable_itShouldKeepUnsubstitutedWhenErrorWhenUndefinedIsFalse() {
        val map = mapOf("key2" to "b\${key1}")
        val source = map.asSource().substituted(errorWhenUndefined = false)
        assertEquals(map, source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onUndefinedPathVariableInReferenceFormat_itShouldThrowUndefinedPathVariableExceptionByDefault() {
        val map = mapOf("key2" to "\${key1}")
        val e = assertCheckedThrows<UndefinedPathVariableException> { map.asSource().substituted() }
        assertEquals("\${key1}", e.text)
    }

    @Test
    fun testSubOp_onContainsUndefinedPathVariableInReferenceFormat_itShouldKeepUnsubstitutedWhenErrorWhenUndefinedIsFalse() {
        val map = mapOf("key2" to "\${key1}")
        val source = map.asSource().substituted(errorWhenUndefined = false)
        assertEquals(map, source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onContainsMultiplePathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${key1}b\${key3}", "key3" to "c")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "abc", "key3" to "c"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onChainedPathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${key1}b", "key3" to "\${key2}c")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "ab", "key3" to "abc"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onNestedPathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${\${key3}}b", "key3" to "key1")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "ab", "key3" to "key1"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onPathVariableWithDefaultValue_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\${key3:-c}")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "bc"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onPathVariableWithKey_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${key1}\${base64Decoder:SGVsbG9Xb3JsZCE=}")
        val source = map.asSource().substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "aHelloWorld!"), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onPathVariableInRefFormat_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to mapOf("key3" to "a", "key4" to "b"), "key2" to "\${key1}")
        val source = map.asSource().substituted()
        assertEquals(
            mapOf(
                "key1" to mapOf("key3" to "a", "key4" to "b"),
                "key2" to mapOf("key3" to "a", "key4" to "b")
            ),
            source.tree.toHierarchical()
        )
    }

    @Test
    fun testSubOp_onNestedPathVariableInRefFormat_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to mapOf("key3" to "a", "key4" to "b"), "key2" to "\${\${key3}}", "key3" to "key1")
        val source = map.asSource().substituted()
        assertEquals(mapOf(
            "key1" to mapOf("key3" to "a", "key4" to "b"),
            "key2" to mapOf("key3" to "a", "key4" to "b"),
            "key3" to "key1"
        ), source.tree.toHierarchical())
    }

    @Test
    fun testSubOp_onPathVariableInDifferentSources_itShouldSubstitutePathVariables() {
        val map1 = mapOf("key1" to "a")
        val map2 = mapOf("key2" to "b\${key1}")
        val source = (map2.asSource() + map1.asSource()).substituted()
        assertEquals(mapOf("key1" to "a", "key2" to "ba"), source.tree.toHierarchical())
    }

    @Test
    fun testFeatureOp_onEnableFeature_itShouldLetTheFeatureBeEnabled() {
        val source = Source().enabled(Feature.FAIL_ON_UNKNOWN_PATH)
        assertTrue(source.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @Test
    fun testFeatureOp_onDisableFeature_itShouldLetTheFeatureBeDisabled() {
        val source = Source().disabled(Feature.FAIL_ON_UNKNOWN_PATH)
        assertFalse(source.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @Test
    fun testFeatureOp_onEnableFeatureBeforeTransformingSource_itShouldLetTheFeatureBeEnabled() {
        val source = Source().enabled(Feature.FAIL_ON_UNKNOWN_PATH).withPrefix("prefix")
        assertTrue(source.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @Test
    fun testFeatureOp_onDisableFeatureBeforeTransformingSource_itShouldLetTheFeatureBeDisabled() {
        val source = Source().disabled(Feature.FAIL_ON_UNKNOWN_PATH).withPrefix("prefix")
        assertFalse(source.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @Test
    fun testFeatureOp_onDefault_itShouldUseTheFeatureDefaultSetting() {
        val source = Source()
        assertEquals(Feature.FAIL_ON_UNKNOWN_PATH.enabledByDefault, source.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @Test
    fun testPrefixOp_onEmptyPrefix_itShouldReturnItself() {
        assertSame(prefixSource, prefixSource.withPrefix(""))
    }

    @Test
    fun testPrefixOp_onFindValidPath_itShouldContainTheValue() {
        assertTrue("level1" in prefixSource)
        assertTrue("level1.level2" in prefixSource)
        assertTrue("level1.level2.key" in prefixSource)
    }

    @Test
    fun testPrefixOp_onFindInvalidPath_itShouldNotContainTheValue() {
        assertTrue("level3" !in prefixSource)
        assertTrue("level1.level3" !in prefixSource)
        assertTrue("level1.level2.level3" !in prefixSource)
        assertTrue("level1.level3.level2" !in prefixSource)
    }

    @Test
    fun testPrefixOp_onGetValidPathUsingGetOrNull_itShouldReturnTheCorrespondingValue() {
        assertEquals("value", (prefixSource.getOrNull("level1")?.get("level2.key")?.tree as ValueNode).value as String)
        assertEquals("value", (prefixSource.getOrNull("level1.level2")?.get("key")?.tree as ValueNode).value as String)
        assertEquals("value", (prefixSource.getOrNull("level1.level2.key")?.tree as ValueNode).value as String)
    }

    @Test
    fun testPrefixOp_onGetInvalidPathUsingGetOrNull_itShouldReturnNull() {
        assertNull(prefixSource.getOrNull("level3"))
        assertNull(prefixSource.getOrNull("level1.level3"))
        assertNull(prefixSource.getOrNull("level1.level2.level3"))
        assertNull(prefixSource.getOrNull("level1.level3.level2"))
    }
}