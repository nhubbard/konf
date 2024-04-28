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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        assertEquals(sourceForPath.getOrNull(validPath)?.tree, tree)
    }

    @Test
    fun testGetOp_onGetInvalidPathUsingGetOrNull_itShouldReturnNull() {
        assertNull(sourceForPath.getOrNull(invalidPath))
    }

    @Test
    fun testGetOp_onGetValidPathUsingGet_itShouldReturnTheCorrespondingValue() {
        assertEquals(sourceForPath[validPath].tree, tree)
    }

    @Test
    fun testGetOp_onGetInvalidPathUsingGet_itShouldThrowNoSuchPathException() {
        val e = assertCheckedThrows<NoSuchPathException> { sourceForPath[invalidPath] }
        assertEquals(e.path, invalidPath)
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
        assertEquals(sourceForKey.getOrNull(validKey)?.tree, tree)
    }

    @Test
    fun testGetOp_onGetByInvalidKeyUsingGetOrNull_itShouldReturnNull() {
        assertNull(sourceForKey.getOrNull(invalidKey))
    }

    @Test
    fun testGetOp_onGetByValidKeyUsingGet_itShouldReturnTheCorrespondingValue() {
        assertEquals(sourceForKey[validKey].tree, tree)
    }

    @Test
    fun testGetOp_onGetByInvalidKeyUsingGet_itShouldThrowNoSuchPathException() {
        val e = assertCheckedThrows<NoSuchPathException> { sourceForKey[invalidKey] }
        assertEquals(e.path, invalidKey.toPath())
    }

    @Test
    fun testCastOp_onCastIntToLong_itShouldSucceed() {
        val source = 1.asSource()
        assertEquals(source.asValue<Long>(), 1L)
    }

    @Test
    fun testCastOp_onCastShortToInt_itShouldSucceed() {
        val source = 1.toShort().asSource()
        assertEquals(source.asValue<Int>(), 1)
    }

    @Test
    fun testCastOp_onCastByteToShort_itShouldSucceed() {
        val source = 1.toByte().asSource()
        assertEquals(source.asValue<Short>(), 1.toShort())
    }

    @Test
    fun testCastOp_onCastLongToBigInteger_itShouldSucceed() {
        val source = 1L.asSource()
        assertEquals(source.asValue<BigInteger>(), BigInteger.valueOf(1))
    }

    @Test
    fun testCastOp_onCastDoubleToBigDecimal_itShouldSucceed() {
        val source = 1.5.asSource()
        assertEquals(source.asValue<BigDecimal>(), BigDecimal.valueOf(1.5))
    }

    @Test
    fun testCastOp_onCastLongInRangeOfIntToInt_itShouldSucceed() {
        val source = 1L.asSource()
        assertEquals(source.asValue<Int>(), 1)
    }

    @Test
    fun testCastOp_onCastLongOutOfRangeOfIntToInt_itShouldThrowParseException() {
        assertThrows<ParseException> { Long.MAX_VALUE.asSource().asValue<Int>() }
        assertThrows<ParseException> { Long.MIN_VALUE.asSource().asValue<Int>() }
    }

    @Test
    fun testCastOp_onCastIntInRangeOfShortToShort_itShouldSucceed() {
        val source = 1.asSource()
        assertEquals(source.asValue<Short>(), 1.toShort())
    }

    @Test
    fun testCastOp_onCastIntOutOfRangeOfShortToShort_itShouldThrowParseException() {
        assertThrows<ParseException> { Int.MAX_VALUE.asSource().asValue<Short>() }
        assertThrows<ParseException> { Int.MIN_VALUE.asSource().asValue<Short>() }
    }

    @Test
    fun testCastOp_onCastShortInRangeOfByteToByte_itShouldSucceed() {
        val source = 1.toShort().asSource()
        assertEquals(source.asValue<Byte>(), 1.toByte())
    }

    @Test
    fun testCastOp_onCastShortOutOfRangeOfByteToByte_itShouldThrowParseException() {
        assertThrows<ParseException> { Short.MAX_VALUE.asSource().asValue<Byte>() }
        assertThrows<ParseException> { Short.MIN_VALUE.asSource().asValue<Byte>() }
    }

    @Test
    fun testCastOp_onCastLongInRangeOfByteToByte_itShouldSucceed() {
        val source = 1L.asSource()
        assertEquals(source.asValue<Byte>(), 1L.toByte())
    }

    @Test
    fun testCastOp_onCastLongOutOfRangeOfByteToByte_itShouldThrowParseException() {
        assertThrows<ParseException> { Long.MAX_VALUE.asSource().asValue<Byte>() }
        assertThrows<ParseException> { Long.MIN_VALUE.asSource().asValue<Byte>() }
    }

    @Test
    fun testCastOp_onCastDoubleToFloat_itShouldSucceed() {
        val source = 1.5.asSource()
        assertEquals(source.asValue<Float>(), 1.5f)
    }

    @Test
    fun testCastOp_onCastCharToString_itShouldSucceed() {
        val source = 'a'.asSource()
        assertEquals(source.asValue<String>(), "a")
    }

    @Test
    fun testCastOp_onCastStringContainingSingleCharToChar_itShouldSucceed() {
        val source = "a".asSource()
        assertEquals(source.asValue<Char>(), 'a')
    }

    @Test
    fun testCastOp_onCastStringContainingMultipleCharsToChar_itShouldThrowParseException() {
        val source = "ab".asSource()
        assertThrows<ParseException> { source.asValue<Char>() }
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
        assertThrows<ParseException> { source.asValue<Boolean>() }
    }

    @Test
    fun testCastOp_onCastStringToByte_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(source.asValue<Byte>(), 1.toByte())
    }

    @Test
    fun testCastOp_onCastStringToShort_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(source.asValue<Short>(), 1.toShort())
    }

    @Test
    fun testCastOp_onCastStringToInt_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(source.asValue<Int>(), 1)
    }

    @Test
    fun testCastOp_onCastStringToLong_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(source.asValue<Long>(), 1L)
    }

    @Test
    fun testCastOp_onCastStringToFloat_itShouldSucceed() {
        val source = "1.5".asSource()
        assertEquals(source.asValue<Float>(), 1.5F)
    }

    @Test
    fun testCastOp_onCastStringToDouble_itShouldSucceed() {
        val source = "1.5F".asSource()
        assertEquals(source.asValue<Double>(), 1.5)
    }

    @Test
    fun testCastOp_onCastStringToBigInteger_itShouldSucceed() {
        val source = "1".asSource()
        assertEquals(source.asValue<BigInteger>(), 1.toBigInteger())
    }

    @Test
    fun testCastOp_onCastStringToBigDecimal_itShouldSucceed() {
        val source = "1.5".asSource()
        assertEquals(source.asValue<BigDecimal>(), 1.5.toBigDecimal())
    }

    @Test
    fun testCastOp_onCastStringToOffsetTime_itShouldSucceed() {
        val text = "10:15:30+01:00"
        val source = text.asSource()
        assertEquals(source.asValue<OffsetTime>(), OffsetTime.parse(text))
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToOffsetTime_itShouldThrowParseException() {
        val text = "10:15:30"
        val source = text.asSource()
        assertThrows<ParseException> { source.asValue<OffsetTime>() }
    }

    @Test
    fun testCastOp_onCastStringToOffsetDateTime_itShouldSucceed() {
        val text = "2007-12-03T10:15:30+01:00"
        val source = text.asSource()
        assertEquals(source.asValue<OffsetDateTime>(), OffsetDateTime.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToZonedDateTime_itShouldSucceed() {
        val text = "2007-12-03T10:15:30+01:00[Europe/Paris]"
        val source = text.asSource()
        assertEquals(source.asValue<ZonedDateTime>(), ZonedDateTime.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToLocalDate_itShouldSucceed() {
        val text = "2007-12-03"
        val source = text.asSource()
        assertEquals(source.asValue<LocalDate>(), LocalDate.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToLocalTime_itShouldSucceed() {
        val text = "10:15:30"
        val source = text.asSource()
        assertEquals(source.asValue<LocalTime>(), LocalTime.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToLocalDateTime_itShouldSucceed() {
        val text = "2007-12-03T10:15:30"
        val source = text.asSource()
        assertEquals(source.asValue<LocalDateTime>(), LocalDateTime.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToYear_itShouldSucceed() {
        val text = "2007"
        val source = text.asSource()
        assertEquals(source.asValue<Year>(), Year.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToYearMonth_itShouldSucceed() {
        val text = "2007-12"
        val source = text.asSource()
        assertEquals(source.asValue<YearMonth>(), YearMonth.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToInstant_itShouldSucceed() {
        val text = "2007-12-03T10:15:30.00Z"
        val source = text.asSource()
        assertEquals(source.asValue<Instant>(), Instant.parse(text))
    }

    @Test
    fun testCastOp_onCastStringToDate_itShouldSucceed() {
        val text = "2007-12-03T10:15:30.00Z"
        val source = text.asSource()
        assertEquals(source.asValue<Date>(), Date.from(Instant.parse(text)))
    }

    @Test
    fun testCastOp_onCastLocalDateTimeStringToDate_itShouldSucceed() {
        val text = "2007-12-03T10:15:30"
        val source = text.asSource()
        assertEquals(
            source.asValue<Date>(),
            Date.from(LocalDateTime.parse(text).toInstant(ZoneOffset.UTC))
        )
    }

    @Test
    fun testCastOp_onCastLocalDateStringToDate_itShouldSucceed() {
        val text = "2007-12-03"
        val source = text.asSource()
        assertEquals(
            source.asValue<Date>(),
            Date.from(LocalDate.parse(text).atStartOfDay().toInstant(ZoneOffset.UTC))
        )
    }

    @Test
    fun testCastOp_onCastStringToDuration_itShouldSucceed() {
        val text = "P2DT3H4M"
        val source = text.asSource()
        assertEquals(source.asValue<Duration>(), Duration.parse(text))
    }

    @Test
    fun testCastOp_onCastStringWithSimpleUnitToDuration_itShouldSucceed() {
        val text = "200ms"
        val source = text.asSource()
        assertEquals(source.asValue<Duration>(), Duration.ofMillis(200))
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToDuration_itShouldThrowParseException() {
        val text = "2 year"
        val source = text.asSource()
        assertThrows<ParseException> { source.asValue<Duration>() }
    }

    @Test
    fun testCastOp_onCastStringToSizeInBytes_itShouldSucceed() {
        val text = "10k"
        val source = text.asSource()
        assertEquals(source.asValue<SizeInBytes>().bytes, 10240L)
    }

    @Test
    fun testCastOp_onCastStringWithInvalidFormatToSizeInBytes_itShouldSucceed() {
        val text = "10u"
        val source = text.asSource()
        assertThrows<ParseException> { source.asValue<SizeInBytes>() }
    }

    @Test
    fun testCastOp_onCastSetToList_itShouldSucceed() {
        val source = setOf(1).asSource()
        assertEquals(source.asValue<List<Int>>(), listOf(1))
    }

    @Test
    fun testCastOp_onCastArrayToList_itShouldSucceed() {
        val source = arrayOf(1).asSource()
        assertEquals(source.asValue<List<Int>>(), listOf(1))
    }

    @Test
    fun testCastOp_onCastArrayToSet_itShouldSucceed() {
        val source = arrayOf(1).asSource()
        assertEquals(source.asValue<Set<Int>>(), setOf(1))
    }

    @Test
    fun testLoadOp_onLoadFromValidSource_itShouldLoadSuccessfully() {
        val config = loadSource<Int>(1)
        assertEquals(config("item"), 1)
    }

    @Test
    fun testLoadOp_onLoadConcreteMapType_itShouldLoadSuccessfully() {
        val config = loadSource<ConcurrentHashMap<String, Int>>(mapOf("1" to 1))
        assertEquals(config<ConcurrentHashMap<String, Int>>("item"), mapOf("1" to 1))
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
        assertEquals(config<Map<String, String>>("item"), mapOf("key1" to "a", "key2" to "b\${item.key1}"))
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
        assertEquals(config<Map<String, String>>("item"), mapOf("key1" to "a", "key2" to "b\${item.key1}"))
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
        assertEquals(config<Map<String, String>>("item"), mapOf("key1" to "a", "key2" to "ba"))
    }

    @Test
    fun testSubOp_onNoPathVariable_itShouldRemainUnchanged() {
        val map = mapOf("key1" to "a", "key2" to "b")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), map)
    }

    @Test
    fun testSubOp_onSinglePathVariable_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\${key1}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "ba"))
    }

    @Test
    fun testSubOp_onIntPathVariable_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to 1, "key2" to "b\${key1}", "key3" to "\${key1}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to 1, "key2" to "b1", "key3" to 1))
    }

    @Test
    fun testSubOp_onStringListPathVariable_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a,b,c", "key2" to "a\${key1}")
        val source = Source.from.map.flat(map).substituted().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a,b,c", "key2" to "aa,b,c"))
    }

    @Test
    fun testSubOp_onListPathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("top" to listOf(mapOf("key1" to "a", "key2" to "b\${top.0.key1}")))
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("top" to listOf(mapOf("key1" to "a", "key2" to "ba"))))
    }

    @Test
    fun testSubOp_onIncorrectTypePathVariables_itShouldThrowWrongTypeException() {
        val map = mapOf("key1" to 1.0, "key2" to "b\${key1}")
        assertThrows<WrongTypeException> { map.asSource().substituted() }
    }

    @Test
    fun testSubOp_onEscapedPathVariables_itShouldNotSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\$\${key1}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "b\${key1}"))
    }

    @Test
    fun testSubOp_onNestedEscapedPathVariables_itShouldNotSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\$\$\$\${key1}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "b\$\$\${key1}"))
    }

    @Test
    fun testSubOp_onNestedEscapedPathVariables_andMultipleSubstitutions_itShouldEscapeOnlyOnce() {
        val map = mapOf("key1" to "a", "key2" to "b\$\$\$\${key1}")
        val source = map.asSource().substituted().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "b\$\$\${key1}"))
    }

    @Test
    fun testSubOp_onContainsUndefinedPathVariable_itShouldThrowUndefinedPathVariableExceptionByDefault() {
        val map = mapOf("key2" to "b\${key1}")
        val e = assertCheckedThrows<UndefinedPathVariableException> { map.asSource().substituted() }
        assertEquals(e.text, "b\${key1}")
    }

    @Test
    fun testSubOp_onContainsUndefinedPathVariable_itShouldKeepUnsubstitutedWhenErrorWhenUndefinedIsFalse() {
        val map = mapOf("key2" to "b\${key1}")
        val source = map.asSource().substituted(errorWhenUndefined = false)
        assertEquals(source.tree.toHierarchical(), map)
    }

    @Test
    fun testSubOp_onUndefinedPathVariableInReferenceFormat_itShouldThrowUndefinedPathVariableExceptionByDefault() {
        val map = mapOf("key2" to "\${key1}")
        val e = assertCheckedThrows<UndefinedPathVariableException> { map.asSource().substituted() }
        assertEquals(e.text, "\${key1}")
    }

    @Test
    fun testSubOp_onContainsUndefinedPathVariableInReferenceFormat_itShouldKeepUnsubstitutedWhenErrorWhenUndefinedIsFalse() {
        val map = mapOf("key2" to "\${key1}")
        val source = map.asSource().substituted(errorWhenUndefined = false)
        assertEquals(source.tree.toHierarchical(), map)
    }

    @Test
    fun testSubOp_onContainsMultiplePathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${key1}b\${key3}", "key3" to "c")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "abc", "key3" to "c"))
    }

    @Test
    fun testSubOp_onChainedPathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${key1}b", "key3" to "\${key2}c")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "ab", "key3" to "abc"))
    }

    @Test
    fun testSubOp_onNestedPathVariables_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${\${key3}}b", "key3" to "key1")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "ab", "key3" to "key1"))
    }

    @Test
    fun testSubOp_onPathVariableWithDefaultValue_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "b\${key3:-c}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "bc"))
    }

    @Test
    fun testSubOp_onPathVariableWithKey_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to "a", "key2" to "\${key1}\${base64Decoder:SGVsbG9Xb3JsZCE=}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "aHelloWorld!"))
    }

    @Test
    fun testSubOp_onPathVariableInRefFormat_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to mapOf("key3" to "a", "key4" to "b"), "key2" to "\${key1}")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf(
            "key1" to mapOf("key3" to "a", "key4" to "b"),
            "key2" to mapOf("key3" to "a", "key4" to "b")
        ))
    }

    @Test
    fun testSubOp_onNestedPathVariableInRefFormat_itShouldSubstitutePathVariables() {
        val map = mapOf("key1" to mapOf("key3" to "a", "key4" to "b"), "key2" to "\${\${key3}}", "key3" to "key1")
        val source = map.asSource().substituted()
        assertEquals(source.tree.toHierarchical(), mapOf(
            "key1" to mapOf("key3" to "a", "key4" to "b"),
            "key2" to mapOf("key3" to "a", "key4" to "b"),
            "key3" to "key1"
        ))
    }

    @Test
    fun testSubOp_onPathVariableInDifferentSources_itShouldSubstitutePathVariables() {
        val map1 = mapOf("key1" to "a")
        val map2 = mapOf("key2" to "b\${key1}")
        val source = (map2.asSource() + map1.asSource()).substituted()
        assertEquals(source.tree.toHierarchical(), mapOf("key1" to "a", "key2" to "ba"))
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
        assertEquals(source.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH), Feature.FAIL_ON_UNKNOWN_PATH.enabledByDefault)
    }

    @Test
    fun testPrefixOp_onEmptyPrefix_itShouldReturnItself() {
        assertTrue(prefixSource.withPrefix("") === prefixSource)
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
        assertEquals((prefixSource.getOrNull("level1")?.get("level2.key")?.tree as ValueNode).value as String, "value")
        assertEquals((prefixSource.getOrNull("level1.level2")?.get("key")?.tree as ValueNode).value as String, "value")
        assertEquals((prefixSource.getOrNull("level1.level2.key")?.tree as ValueNode).value as String, "value")
    }

    @Test
    fun testPrefixOp_onGetInvalidPathUsingGetOrNull_itShouldReturnNull() {
        assertNull(prefixSource.getOrNull("level3"))
        assertNull(prefixSource.getOrNull("level1.level3"))
        assertNull(prefixSource.getOrNull("level1.level2.level3"))
        assertNull(prefixSource.getOrNull("level1.level3.level2"))
    }
}