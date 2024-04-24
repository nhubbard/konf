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

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.toSizeInBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*
import java.util.concurrent.Executors

object DefaultLoadersConfig : ConfigSpec("source.test") {
    val type by required<String>()
}

fun Source.toConfig(): Config = Config {
    addSpec(DefaultLoadersConfig)
}.withSource(this)

inline fun <reified T : Any> assertCausedBy(noinline block: () -> Unit) {
    @Suppress("UNCHECKED_CAST")
    assertThat(
        block,
        throws(
            has(
                LoadException::cause,
                isA<T>() as Matcher<Throwable?>
            )
        )
    )
}

fun Source.toFlattenConfig(): Config = Config {
    addSpec(FlattenDefaultLoadersConfig)
}.withSource(this)

fun newSequentialDispatcher() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

private val dispatcher = newSequentialDispatcher()

val Dispatchers.Sequential: CoroutineDispatcher get() = dispatcher

const val propertiesContent = "source.test.type = properties"

val customDeserializerLoadContent = mapOf<String, Any>(
    "variantA" to mapOf("int" to 1),
    "variantB" to mapOf("double" to 2.0)
).mapKeys { (key, _) -> "level1.level2.$key" }

val mergedSourceFacadeContent = mapOf(
    "level1" to mapOf(
        "level2" to mapOf<String, Any>(
            "empty" to "null",
            "literalEmpty" to "null",
            "present" to 1,

            "boolean" to false,

            "double" to 1.5,
            "float" to -1.5f,
            "bigDecimal" to BigDecimal.valueOf(1.5),

            "char" to 'a',

            "enum" to "LABEL2",

            "array" to mapOf(
                "boolean" to listOf(true, false),
                "byte" to listOf<Byte>(1, 2, 3),
                "short" to listOf<Short>(1, 2, 3),
                "int" to listOf(1, 2, 3),

                "object" to mapOf(
                    "boolean" to listOf(true, false),
                    "int" to listOf(1, 2, 3),
                    "string" to listOf("one", "two", "three")
                )
            ),

            "list" to listOf(1, 2, 3),
            "mutableList" to listOf(1, 2, 3),
            "listOfList" to listOf(listOf(1, 2), listOf(3, 4)),

            "map" to mapOf(
                "a" to 1, "c" to 3
            ),
            "intMap" to mapOf(
                1 to "a", 3 to "c"
            ),
            "sortedMap" to mapOf(
                "c" to 3, "a" to 1
            ),
            "listOfMap" to listOf(
                mapOf("a" to 1, "b" to 2), mapOf("a" to 3, "b" to 4)
            ),

            "nested" to listOf(listOf(listOf(mapOf("a" to 1)))),

            "pair" to mapOf("first" to 1, "second" to 2),

            "clazz" to mapOf(
                "boolean" to false,

                "int" to 1,
                "short" to 2.toShort(),
                "byte" to 3.toByte(),
                "bigInteger" to BigInteger.valueOf(4),
                "long" to 4L,

                "char" to 'a',

                "string" to "string",
                "offsetTime" to OffsetTime.parse("10:15:30+01:00"),
                "offsetDateTime" to OffsetDateTime.parse("2007-12-03T10:15:30+01:00"),
                "zonedDateTime" to ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"),
                "localDate" to LocalDate.parse("2007-12-03"),
                "localTime" to LocalTime.parse("10:15:30"),
                "localDateTime" to LocalDateTime.parse("2007-12-03T10:15:30"),
                "date" to Date.from(Instant.parse("2007-12-03T10:15:30Z")),
                "year" to Year.parse("2007"),
                "yearMonth" to YearMonth.parse("2007-12"),
                "instant" to Instant.parse("2007-12-03T10:15:30.00Z"),
                "duration" to "P2DT3H4M".toDuration(),
                "simpleDuration" to "200millis".toDuration(),
                "size" to "10k".toSizeInBytes(),

                "enum" to "LABEL2",

                "booleanArray" to listOf(true, false),

                "nested" to listOf(listOf(listOf(mapOf("a" to 1))))
            )
        )
    )
)

val mergedSourceFallbackContent = mapOf(
    "level1" to mapOf(
        "level2" to mapOf<String, Any>(
            "boolean" to true,

            "int" to 1,
            "short" to 2.toShort(),
            "byte" to 3.toByte(),
            "bigInteger" to BigInteger.valueOf(4),
            "long" to 4L,

            "char" to 'b',

            "string" to "string",
            "offsetTime" to OffsetTime.parse("10:15:30+01:00"),
            "offsetDateTime" to OffsetDateTime.parse("2007-12-03T10:15:30+01:00"),
            "zonedDateTime" to ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"),
            "localDate" to LocalDate.parse("2007-12-03"),
            "localTime" to LocalTime.parse("10:15:30"),
            "localDateTime" to LocalDateTime.parse("2007-12-03T10:15:30"),
            "date" to Date.from(Instant.parse("2007-12-03T10:15:30Z")),
            "year" to Year.parse("2007"),
            "yearMonth" to YearMonth.parse("2007-12"),
            "instant" to Instant.parse("2007-12-03T10:15:30.00Z"),
            "duration" to "P2DT3H4M".toDuration(),
            "simpleDuration" to "200millis".toDuration(),
            "size" to "10k".toSizeInBytes(),

            "enum" to "LABEL3",

            "array" to mapOf(
                "int" to listOf(3, 2, 1),
                "long" to listOf(4L, 5L, 6L),
                "float" to listOf(-1.0F, 0.0F, 1.0F),
                "double" to listOf(-1.0, 0.0, 1.0),
                "char" to listOf('a', 'b', 'c'),

                "object" to mapOf(
                    "string" to listOf("three", "two", "one"), "enum" to listOf("LABEL1", "LABEL2", "LABEL3")
                )
            ),

            "listOfList" to listOf(listOf(1, 2)),
            "set" to listOf(1, 2, 1),
            "sortedSet" to listOf(2, 1, 1, 3),

            "map" to mapOf(
                "b" to 2, "c" to 3
            ),
            "intMap" to mapOf(
                2 to "b", 3 to "c"
            ),
            "sortedMap" to mapOf(
                "b" to 2, "a" to 1
            ),
            "listOfMap" to listOf(
                mapOf("a" to 1, "b" to 2), mapOf("a" to 3, "b" to 4)
            ),

            "nested" to listOf(listOf(listOf(mapOf("a" to 1)))),

            "pair" to mapOf("first" to 1, "second" to 2),

            "clazz" to mapOf(
                "empty" to "null", "literalEmpty" to "null", "present" to 1,

                "boolean" to true,

                "double" to 1.5, "float" to -1.5f, "bigDecimal" to BigDecimal.valueOf(1.5),

                "char" to 'c',

                "enum" to "LABEL1",

                "booleanArray" to listOf(true, false),

                "nested" to listOf(listOf(listOf(mapOf("a" to 1))))
            )
        )
    )
)

val mergeSourcesWithDifferentFeaturesContent = """
    servicing {
      baseURL = "https://service/api"
      url = "${'$'}{servicing.baseURL}/index.html"
    }
""".trimIndent()

//language=Json
const val multipleDefaultLoadersJsonContent =
    """
{
  "source": {
    "test": {
      "type": "json"
    }
  }
}
"""
