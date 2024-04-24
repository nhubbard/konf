package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.source.toDuration
import io.github.nhubbard.konf.toSizeInBytes
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*

val flatSourceLoadContent = mapOf(
    "empty" to "null",
    "literalEmpty" to "null",
    "present" to "1",
    "boolean" to "false",
    "int" to "1",
    "short" to "2",
    "byte" to "3",
    "bigInteger" to "4",
    "long" to "4",
    "double" to "1.5",
    "float" to "-1.5",
    "bigDecimal" to "1.5",
    "char" to "a",
    "string" to "string",
    "offsetTime" to "10:15:30+01:00",
    "offsetDateTime" to "2007-12-03T10:15:30+01:00",
    "zonedDateTime" to "2007-12-03T10:15:30+01:00[Europe/Paris]",
    "localDate" to "2007-12-03",
    "localTime" to "10:15:30",
    "localDateTime" to "2007-12-03T10:15:30",
    "date" to "2007-12-03T10:15:30Z",
    "year" to "2007",
    "yearMonth" to "2007-12",
    "instant" to "2007-12-03T10:15:30.00Z",
    "duration" to "P2DT3H4M",
    "simpleDuration" to "200millis",
    "size" to "10k",
    "enum" to "LABEL2",
    "list" to "1,2,3",
    "mutableList" to "1,2,3",
    "listOfList.0" to "1,2",
    "listOfList.1" to "3,4",
    "set" to "1,2,1",
    "sortedSet" to "2,1,1,3",
    "map.a" to "1",
    "map.b" to "2",
    "map.c" to "3",
    "intMap.1" to "a",
    "intMap.2" to "b",
    "intMap.3" to "c",
    "sortedMap.c" to "3",
    "sortedMap.b" to "2",
    "sortedMap.a" to "1",
    "nested.0.0.0.a" to "1",
    "listOfMap.0.a" to "1",
    "listOfMap.0.b" to "2",
    "listOfMap.1.a" to "3",
    "listOfMap.1.b" to "4",
    "array.boolean" to "true,false",
    "array.byte" to "1,2,3",
    "array.short" to "1,2,3",
    "array.int" to "1,2,3",
    "array.long" to "4,5,6",
    "array.float" to "-1, 0.0, 1",
    "array.double" to "-1, 0.0, 1",
    "array.char" to "a,b,c",
    "array.object.boolean" to "true,false",
    "array.object.int" to "1,2,3",
    "array.object.string" to "one,two,three",
    "array.object.enum" to "LABEL1,LABEL2,LABEL3",
    "pair.first" to "1",
    "pair.second" to "2",
    "clazz.empty" to "null",
    "clazz.literalEmpty" to "null",
    "clazz.present" to "1",
    "clazz.boolean" to "false",
    "clazz.int" to "1",
    "clazz.short" to "2",
    "clazz.byte" to "3",
    "clazz.bigInteger" to "4",
    "clazz.long" to "4",
    "clazz.double" to "1.5",
    "clazz.float" to "-1.5",
    "clazz.bigDecimal" to "1.5",
    "clazz.char" to "a",
    "clazz.string" to "string",
    "clazz.offsetTime" to "10:15:30+01:00",
    "clazz.offsetDateTime" to "2007-12-03T10:15:30+01:00",
    "clazz.zonedDateTime" to "2007-12-03T10:15:30+01:00[Europe/Paris]",
    "clazz.localDate" to "2007-12-03",
    "clazz.localTime" to "10:15:30",
    "clazz.localDateTime" to "2007-12-03T10:15:30",
    "clazz.date" to "2007-12-03T10:15:30Z",
    "clazz.year" to "2007",
    "clazz.yearMonth" to "2007-12",
    "clazz.instant" to "2007-12-03T10:15:30.00Z",
    "clazz.duration" to "P2DT3H4M",
    "clazz.simpleDuration" to "200millis",
    "clazz.size" to "10k",
    "clazz.enum" to "LABEL2",
    "clazz.booleanArray" to "true,false",
    "clazz.nested.0.0.0.a" to "1",
    "emptyList" to "",
    "emptySet" to "",
    "emptyArray" to "",
    "emptyObjectArray" to "",
    "singleElementList" to "1",
    "multipleElementsList" to "1,2",
    "flatClass.stringWithComma" to "string,with,comma",
    "flatClass.emptyList" to "",
    "flatClass.emptySet" to "",
    "flatClass.emptyArray" to "",
    "flatClass.emptyObjectArray" to "",
    "flatClass.singleElementList" to "1",
    "flatClass.multipleElementsList" to "1,2"
).mapKeys { (key, _) -> "level1.level2.$key" }

val mapSourceLoadContent = mapOf(
    "level1" to mapOf(
        "level2" to mapOf<String, Any>(
            "empty" to "null",
            "literalEmpty" to "null",
            "present" to 1,

            "boolean" to false,

            "int" to 1,
            "short" to 2.toShort(),
            "byte" to 3.toByte(),
            "bigInteger" to BigInteger.valueOf(4),
            "long" to 4L,

            "double" to 1.5,
            "float" to -1.5f,
            "bigDecimal" to BigDecimal.valueOf(1.5),

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

            "array" to mapOf(
                "boolean" to listOf(true, false),
                "byte" to listOf<Byte>(1, 2, 3),
                "short" to listOf<Short>(1, 2, 3),
                "int" to listOf(1, 2, 3),
                "long" to listOf(4L, 5L, 6L),
                "float" to listOf(-1.0F, 0.0F, 1.0F),
                "double" to listOf(-1.0, 0.0, 1.0),
                "char" to listOf('a', 'b', 'c'),

                "object" to mapOf(
                    "boolean" to listOf(true, false),
                    "int" to listOf(1, 2, 3),
                    "string" to listOf("one", "two", "three"),
                    "enum" to listOf("LABEL1", "LABEL2", "LABEL3")
                )
            ),

            "list" to listOf(1, 2, 3),
            "mutableList" to listOf(1, 2, 3),
            "listOfList" to listOf(listOf(1, 2), listOf(3, 4)),
            "set" to listOf(1, 2, 1),
            "sortedSet" to listOf(2, 1, 1, 3),

            "map" to mapOf(
                "a" to 1, "b" to 2, "c" to 3
            ),
            "intMap" to mapOf(
                1 to "a", 2 to "b", 3 to "c"
            ),
            "sortedMap" to mapOf(
                "c" to 3, "b" to 2, "a" to 1
            ),
            "listOfMap" to listOf(
                mapOf("a" to 1, "b" to 2), mapOf("a" to 3, "b" to 4)
            ),

            "nested" to listOf(listOf(listOf(mapOf("a" to 1)))),

            "pair" to mapOf("first" to 1, "second" to 2),

            "clazz" to mapOf(
                "empty" to "null",
                "literalEmpty" to "null",
                "present" to 1,

                "boolean" to false,

                "int" to 1,
                "short" to 2.toShort(),
                "byte" to 3.toByte(),
                "bigInteger" to BigInteger.valueOf(4),
                "long" to 4L,

                "double" to 1.5,
                "float" to -1.5f,
                "bigDecimal" to BigDecimal.valueOf(1.5),

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