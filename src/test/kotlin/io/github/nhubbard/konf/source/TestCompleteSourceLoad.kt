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
import io.github.nhubbard.konf.source.base.helpers.BaseClassForLoad
import io.github.nhubbard.konf.source.base.helpers.FlatConfigForLoad
import io.github.nhubbard.konf.source.base.helpers.flatSourceLoadContent
import io.github.nhubbard.konf.source.base.helpers.mapSourceLoadContent
import io.github.nhubbard.konf.source.base.toFlatMap
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import io.github.nhubbard.konf.source.helpers.*
import io.github.nhubbard.konf.source.hocon.hocon
import io.github.nhubbard.konf.source.hocon.toHocon
import io.github.nhubbard.konf.source.js.js
import io.github.nhubbard.konf.source.js.toJs
import io.github.nhubbard.konf.source.json.toJson
import io.github.nhubbard.konf.source.properties.toProperties
import io.github.nhubbard.konf.source.toml.toToml
import io.github.nhubbard.konf.source.toml.toml
import io.github.nhubbard.konf.source.xml.toXml
import io.github.nhubbard.konf.source.xml.xml
import io.github.nhubbard.konf.source.yaml.toYaml
import io.github.nhubbard.konf.source.yaml.yaml
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import java.util.stream.Stream
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestCompleteSourceLoad {
    @ParameterizedTest
    @MethodSource("sourceLoadProvider")
    fun testSource_onLoadSourceIntoConfig_itShouldContainEveryValueSpecifiedInTheSource(provider: () -> Config) {
        val subject = provider()
        assertNull(subject[ConfigForLoad.empty])
        assertNull(subject[ConfigForLoad.literalEmpty])
        assertEquals(1, subject[ConfigForLoad.present])
        assertEquals(false, subject[ConfigForLoad.boolean])

        assertEquals(1, subject[ConfigForLoad.int])
        assertEquals(2.toShort(), subject[ConfigForLoad.short])
        assertEquals(3.toByte(), subject[ConfigForLoad.byte])
        assertEquals(4.toBigInteger(), subject[ConfigForLoad.bigInteger])
        assertEquals(4L, subject[ConfigForLoad.long])

        assertEquals(1.5, subject[ConfigForLoad.double])
        assertEquals(-1.5f, subject[ConfigForLoad.float])
        assertEquals(1.5.toBigDecimal(), subject[ConfigForLoad.bigDecimal])

        assertEquals('a', subject[ConfigForLoad.char])

        assertEquals("string", subject[ConfigForLoad.string])
        assertEquals(OffsetTime.parse("10:15:30+01:00"), subject[ConfigForLoad.offsetTime])
        assertEquals(OffsetDateTime.parse("2007-12-03T10:15:30+01:00"), subject[ConfigForLoad.offsetDateTime])
        assertEquals(
            ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"),
            subject[ConfigForLoad.zonedDateTime]
        )
        assertEquals(LocalDate.parse("2007-12-03"), subject[ConfigForLoad.localDate])
        assertEquals(LocalTime.parse("10:15:30"), subject[ConfigForLoad.localTime])
        assertEquals(LocalDateTime.parse("2007-12-03T10:15:30"), subject[ConfigForLoad.localDateTime])
        assertEquals(Date.from(Instant.parse("2007-12-03T10:15:30Z")), subject[ConfigForLoad.date])
        assertEquals(Year.parse("2007"), subject[ConfigForLoad.year])
        assertEquals(YearMonth.parse("2007-12"), subject[ConfigForLoad.yearMonth])
        assertEquals(Instant.parse("2007-12-03T10:15:30.00Z"), subject[ConfigForLoad.instant])
        assertEquals(Duration.parse("P2DT3H4M"), subject[ConfigForLoad.duration])
        assertEquals(Duration.ofMillis(200), subject[ConfigForLoad.simpleDuration])
        assertEquals(10240L, subject[ConfigForLoad.size].bytes)

        assertEquals(EnumForLoad.LABEL2, subject[ConfigForLoad.enum])

        assertContentEquals(booleanArrayOf(true, false), subject[ConfigForLoad.booleanArray])
        assertContentEquals(byteArrayOf(1, 2, 3), subject[ConfigForLoad.byteArray])
        assertContentEquals(shortArrayOf(1, 2, 3), subject[ConfigForLoad.shortArray])
        assertContentEquals(intArrayOf(1, 2, 3), subject[ConfigForLoad.intArray])
        assertContentEquals(longArrayOf(4, 5, 6), subject[ConfigForLoad.longArray])
        assertContentEquals(floatArrayOf(-1.0F, 0.0F, 1.0F), subject[ConfigForLoad.floatArray])
        assertContentEquals(doubleArrayOf(-1.0, 0.0, 1.0), subject[ConfigForLoad.doubleArray])
        assertContentEquals(charArrayOf('a', 'b', 'c'), subject[ConfigForLoad.charArray])

        assertContentEquals(arrayOf(true, false), subject[ConfigForLoad.booleanObjectArray])
        assertContentEquals(arrayOf(1, 2, 3), subject[ConfigForLoad.intObjectArray])
        assertContentEquals(arrayOf("one", "two", "three"), subject[ConfigForLoad.stringArray])
        assertContentEquals(
            arrayOf(EnumForLoad.LABEL1, EnumForLoad.LABEL2, EnumForLoad.LABEL3),
            subject[ConfigForLoad.enumArray]
        )

        assertEquals(listOf(1, 2, 3), subject[ConfigForLoad.list])

        assertContentEquals(arrayOf(1, 2, 3), subject[ConfigForLoad.mutableList].toTypedArray())

        assertEquals(listOf(listOf(1, 2), listOf(3, 4)), subject[ConfigForLoad.listOfList])

        assertEquals(setOf(1, 2), subject[ConfigForLoad.set])

        assertEquals(sortedSetOf(1, 2, 3), subject[ConfigForLoad.sortedSet])

        assertEquals(mapOf("a" to 1, "b" to 2, "c" to 3), subject[ConfigForLoad.map])
        assertEquals(mapOf(1 to "a", 2 to "b", 3 to "c"), subject[ConfigForLoad.intMap])
        assertEquals(sortedMapOf("a" to 1, "b" to 2, "c" to 3), subject[ConfigForLoad.sortedMap])
        assertEquals("a", subject[ConfigForLoad.sortedMap].firstKey())
        assertEquals("c", subject[ConfigForLoad.sortedMap].lastKey())
        assertEquals(listOf(mapOf("a" to 1, "b" to 2), mapOf("a" to 3, "b" to 4)), subject[ConfigForLoad.listOfMap])

        assertContentEquals(arrayOf(listOf(setOf(mapOf("a" to 1)))), subject[ConfigForLoad.nested])

        assertEquals(1 to 2, subject[ConfigForLoad.pair])

        val classForLoad = ClassForLoad(
            empty = null,
            literalEmpty = null,
            present = 1,
            boolean = false,
            int = 1,
            short = 2.toShort(),
            byte = 3.toByte(),
            bigInteger = BigInteger.valueOf(4),
            long = 4L,
            double = 1.5,
            float = -1.5f,
            bigDecimal = BigDecimal.valueOf(1.5),
            char = 'a',
            string = "string",
            offsetTime = OffsetTime.parse("10:15:30+01:00"),
            offsetDateTime = OffsetDateTime.parse("2007-12-03T10:15:30+01:00"),
            zonedDateTime = ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"),
            localDate = LocalDate.parse("2007-12-03"),
            localTime = LocalTime.parse("10:15:30"),
            localDateTime = LocalDateTime.parse("2007-12-03T10:15:30"),
            date = Date.from(Instant.parse("2007-12-03T10:15:30Z")),
            year = Year.parse("2007"),
            yearMonth = YearMonth.parse("2007-12"),
            instant = Instant.parse("2007-12-03T10:15:30.00Z"),
            duration = "P2DT3H4M".toDuration(),
            simpleDuration = Duration.ofMillis(200),
            size = SizeInBytes.parse("10k"),
            enum = EnumForLoad.LABEL2,
            booleanArray = booleanArrayOf(true, false),
            nested = arrayOf(listOf(setOf(mapOf("a" to 1))))
        )
        assertEquals(classForLoad.empty, subject[ConfigForLoad.clazz].empty)
        assertEquals(classForLoad.literalEmpty, subject[ConfigForLoad.clazz].literalEmpty)
        assertEquals(classForLoad.present, subject[ConfigForLoad.clazz].present)
        assertEquals(classForLoad.boolean, subject[ConfigForLoad.clazz].boolean)
        assertEquals(classForLoad.int, subject[ConfigForLoad.clazz].int)
        assertEquals(classForLoad.short, subject[ConfigForLoad.clazz].short)
        assertEquals(classForLoad.byte, subject[ConfigForLoad.clazz].byte)
        assertEquals(classForLoad.bigInteger, subject[ConfigForLoad.clazz].bigInteger)
        assertEquals(classForLoad.long, subject[ConfigForLoad.clazz].long)
        assertEquals(classForLoad.double, subject[ConfigForLoad.clazz].double)
        assertEquals(classForLoad.float, subject[ConfigForLoad.clazz].float)
        assertEquals(classForLoad.bigDecimal, subject[ConfigForLoad.clazz].bigDecimal)
        assertEquals(classForLoad.char, subject[ConfigForLoad.clazz].char)
        assertEquals(classForLoad.string, subject[ConfigForLoad.clazz].string)
        assertEquals(classForLoad.offsetTime, subject[ConfigForLoad.clazz].offsetTime)
        assertEquals(classForLoad.offsetDateTime, subject[ConfigForLoad.clazz].offsetDateTime)
        assertEquals(classForLoad.zonedDateTime, subject[ConfigForLoad.clazz].zonedDateTime)
        assertEquals(classForLoad.localDate, subject[ConfigForLoad.clazz].localDate)
        assertEquals(classForLoad.localTime, subject[ConfigForLoad.clazz].localTime)
        assertEquals(classForLoad.localDateTime, subject[ConfigForLoad.clazz].localDateTime)
        assertEquals(classForLoad.date, subject[ConfigForLoad.clazz].date)
        assertEquals(classForLoad.year, subject[ConfigForLoad.clazz].year)
        assertEquals(classForLoad.yearMonth, subject[ConfigForLoad.clazz].yearMonth)
        assertEquals(classForLoad.instant, subject[ConfigForLoad.clazz].instant)
        assertEquals(classForLoad.duration, subject[ConfigForLoad.clazz].duration)
        assertEquals(classForLoad.simpleDuration, subject[ConfigForLoad.clazz].simpleDuration)
        assertEquals(classForLoad.size, subject[ConfigForLoad.clazz].size)
        assertEquals(classForLoad.enum, subject[ConfigForLoad.clazz].enum)
        assertContentEquals(classForLoad.booleanArray, subject[ConfigForLoad.clazz].booleanArray)
        assertTrue(subject[ConfigForLoad.clazz].nested.contentDeepEquals(classForLoad.nested))
    }

    // Extra tests from YamlSourceLoadSpec
    @Test
    fun testYamlSourceLoad_givenConfig_onLoadYamlWithIntKey_itShouldTreatItAsStringKey() {
        val config = Config().from.yaml.string(
            """
            tree:
              1:
                myVal: true
            """.trimIndent()
        )
        assertTrue(config.at("tree.1.myVal").toValue<Boolean>())
    }

    @Test
    fun testYamlSourceLoad_givenConfig_onLoadYamlWithLongKey_itShouldTreatItAsAStringKey() {
        val config = Config().from.yaml.string(
            """
            tree:
              2147483648:
                myVal: true
            """.trimIndent()
        )
        assertTrue(config.at("tree.2147483648.myVal").toValue<Boolean>())
    }

    @Test
    fun testYamlSourceLoad_givenConfig_onLoadYamlWithBigIntegerKey_itShouldTreatItAsAStringKey() {
        val config = Config().from.yaml.string(
            """
            tree:
              9223372036854775808:
                myVal: true
            """.trimIndent()
        )
        assertTrue(config.at("tree.9223372036854775808.myVal").toValue<Boolean>())
    }

    @Test
    fun testYamlSourceLoad_givenConfig_onLoadYamlWithTopLevelList_itShouldTreatItAsAList() {
        val config = Config().from.yaml.string(
            """
            - a
            - b
            """.trimIndent()
        )
        assertEquals(listOf("a", "b"), config.toValue())
    }

    @ParameterizedTest
    @MethodSource("flatSourceLoadProvider")
    fun testFlatSource_onLoadSourceIntoConfig_itShouldContainEveryValueSpecifiedInTheSource(provider: () -> Config) {
        val subject = provider()
        val classForLoad = BaseClassForLoad(
            stringWithComma = "string,with,comma",
            emptyList = listOf(),
            emptySet = setOf(),
            emptyArray = intArrayOf(),
            emptyObjectArray = arrayOf(),
            singleElementList = listOf(1),
            multipleElementsList = listOf(1, 2)
        )
        assertEquals(emptyList(), subject[FlatConfigForLoad.emptyList])
        assertEquals(emptySet(), subject[FlatConfigForLoad.emptySet])
        assertContentEquals(intArrayOf(), subject[FlatConfigForLoad.emptyArray])
        assertContentEquals(arrayOf(), subject[FlatConfigForLoad.emptyObjectArray])
        assertEquals(listOf(1), subject[FlatConfigForLoad.singleElementList])
        assertEquals(listOf(1, 2), subject[FlatConfigForLoad.multipleElementsList])
        assertEquals(classForLoad.stringWithComma, subject[FlatConfigForLoad.flatClass].stringWithComma)
    }

    companion object {
        @JvmStatic
        fun sourceLoadProvider(): Stream<Arguments> = Stream.concat(
            singleArgumentsOf(
                // KVSourceFromDefaultProvidersSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.map.kv(kvLoadContent)
                },
                // MergedSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.withSource(mergedSourceFallbackContent.asSource() + mergedSourceFacadeContent.asSource())
                },
                // MergedSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.withSource(mergedSourceFallbackContent.asSource() + mergedSourceFacadeContent.asSource())
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.map.hierarchical(config.toHierarchicalMap())
                },
                // SourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.map.kv(kvLoadContent)
                },
                // SourceReloadFromDiskSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.map.kv(kvLoadContent)
                    val map = config.toMap()
                    val newMap = tempFile().run {
                        ObjectOutputStream(outputStream()).use {
                            it.writeObject(map)
                        }
                        ObjectInputStream(inputStream()).use {
                            @Suppress("UNCHECKED_CAST")
                            it.readObject() as Map<String, Any>
                        }
                    }
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.map.kv(newMap)
                },
                // SourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.map.kv(kvLoadContent)
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.map.kv(config.toMap())
                },
                // MapSourceFromDefaultProvidersSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.withSource(Source.from.map.hierarchical(mapSourceLoadContent))
                },
                // MapSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.from.map.hierarchical(mapSourceLoadContent)
                },
                // MapSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.map.hierarchical(mapSourceLoadContent)
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.map.hierarchical(config.toHierarchicalMap())
                },
                // HoconSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.from.hocon.resource("source/source.conf")
                },
                // HoconSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.hocon.resource("source/source.conf")
                    val hocon = config.toHocon.toText()
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.hocon.string(hocon)
                },
                // JsSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.from.js.resource("source/source.js")
                },
                // JsSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.js.resource("source/source.js")
                    val js = config.toJs.toText()
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.js.string(js)
                },
                // JsonSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.from.json.resource("source/source.json")
                },
                // JsonSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.json.resource("source/source.json")
                    val json = config.toJson.toText()
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.json.string(json)
                },
                // TomlSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.from.toml.resource("source/source.toml")
                },
                // TomlSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.toml.resource("source/source.toml")
                    val toml = config.toToml.toText()
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.toml.string(toml)
                },
                // YamlSourceLoadSpec
                {
                    Config {
                        addSpec(ConfigForLoad)
                        enable(Feature.FAIL_ON_UNKNOWN_PATH)
                    }.from.yaml.resource("source/source.yaml")
                },
                // YamlSourceReloadSpec
                {
                    val config = Config {
                        addSpec(ConfigForLoad)
                    }.from.yaml.resource("source/source.yaml")
                    val yaml = config.toYaml.toText()
                    Config {
                        addSpec(ConfigForLoad)
                    }.from.yaml.string(yaml)
                }
            ),
            flatSourceLoadProvider()
        )

        @JvmStatic
        fun flatSourceLoadProvider(): Stream<Arguments> = singleArgumentsOf(
            // FlatSourceFromDefaultProvidersSpec
            {
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.withSource(Source.from.map.flat(flatSourceLoadContent))
            },
            // FlatSourceLoadSpec
            {
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                    enable(Feature.FAIL_ON_UNKNOWN_PATH)
                }.from.map.flat(flatSourceLoadContent)
            },
            // FlatSourceReloadSpec
            {
                val config = Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.map.flat(flatSourceLoadContent)
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.map.flat(config.toFlatMap())
            },
            // PropertiesSourceLoadSpec
            {
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.properties.resource("source/source.properties")
            },
            // PropertiesSourceReloadSpec
            {
                val config = Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.properties.resource("source/source.properties")
                val properties = config.toProperties.toText()
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.properties.string(properties)
            },
            // XmlSourceLoadSpec
            {
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.xml.resource("source/source.xml")
            },
            // XmlSourceReloadSpec
            {
                val config = Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.xml.resource("source/source.xml")
                val xml = config.toXml.toText()
                Config {
                    addSpec(ConfigForLoad)
                    addSpec(FlatConfigForLoad)
                }.from.xml.string(xml)
            }
        )
    }
}