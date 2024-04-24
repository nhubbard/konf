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

import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.SizeInBytes
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*

object ConfigForLoad : ConfigSpec("level1.level2") {
    val empty by required<Int?>()
    val literalEmpty by required<Int?>()
    val present by required<Int?>()

    val boolean by required<Boolean>()

    val int by required<Int>()
    val short by required<Short>()
    val byte by required<Byte>()
    val bigInteger by required<BigInteger>()
    val long by required<Long>()

    val double by required<Double>()
    val float by required<Float>()
    val bigDecimal by required<BigDecimal>()

    val char by required<Char>()

    val string by required<String>()
    val offsetTime by required<OffsetTime>()
    val offsetDateTime by required<OffsetDateTime>()
    val zonedDateTime by required<ZonedDateTime>()
    val localDate by required<LocalDate>()
    val localTime by required<LocalTime>()
    val localDateTime by required<LocalDateTime>()
    val date by required<Date>()
    val year by required<Year>()
    val yearMonth by required<YearMonth>()
    val instant by required<Instant>()
    val duration by required<Duration>()
    val simpleDuration by required<Duration>()
    val size by required<SizeInBytes>()

    val enum by required<EnumForLoad>()

    // array items
    val booleanArray by required<BooleanArray>("array.boolean")
    val byteArray by required<ByteArray>("array.byte")
    val shortArray by required<ShortArray>("array.short")
    val intArray by required<IntArray>("array.int")
    val longArray by required<LongArray>("array.long")
    val floatArray by required<FloatArray>("array.float")
    val doubleArray by required<DoubleArray>("array.double")
    val charArray by required<CharArray>("array.char")

    // object array item
    val booleanObjectArray by required<Array<Boolean>>("array.object.boolean")
    val intObjectArray by required<Array<Int>>("array.object.int")
    val stringArray by required<Array<String>>("array.object.string")
    val enumArray by required<Array<EnumForLoad>>("array.object.enum")

    val list by required<List<Int>>()
    val mutableList by required<List<Int>>()
    val listOfList by required<List<List<Int>>>()
    val set by required<Set<Int>>()
    val sortedSet by required<SortedSet<Int>>()

    val map by required<Map<String, Int>>()
    val intMap by required<Map<Int, String>>()
    val sortedMap by required<SortedMap<String, Int>>()
    val listOfMap by required<List<Map<String, Int>>>()

    val nested by required<Array<List<Set<Map<String, Int>>>>>()

    val pair by required<Pair<Int, Int>>()

    val clazz by required<ClassForLoad>()
}

