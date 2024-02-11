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
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.util.Date
import java.util.SortedMap
import java.util.SortedSet

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

enum class EnumForLoad {
    LABEL1, LABEL2, LABEL3
}

data class ClassForLoad(
    val empty: Int?,
    val literalEmpty: Int?,
    val present: Int?,
    val boolean: Boolean,
    val int: Int,
    val short: Short,
    val byte: Byte,
    val bigInteger: BigInteger,
    val long: Long,
    val double: Double,
    val float: Float,
    val bigDecimal: BigDecimal,
    val char: Char,
    val string: String,
    val offsetTime: OffsetTime,
    val offsetDateTime: OffsetDateTime,
    val zonedDateTime: ZonedDateTime,
    val localDate: LocalDate,
    val localTime: LocalTime,
    val localDateTime: LocalDateTime,
    val date: Date,
    val year: Year,
    val yearMonth: YearMonth,
    val instant: Instant,
    val duration: Duration,
    val simpleDuration: Duration,
    val size: SizeInBytes,
    val enum: EnumForLoad,
    val booleanArray: BooleanArray,
    val nested: Array<List<Set<Map<String, Int>>>>
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassForLoad

        if (empty != other.empty) return false
        if (literalEmpty != other.literalEmpty) return false
        if (present != other.present) return false
        if (boolean != other.boolean) return false
        if (int != other.int) return false
        if (short != other.short) return false
        if (byte != other.byte) return false
        if (bigInteger != other.bigInteger) return false
        if (long != other.long) return false
        if (double != other.double) return false
        if (float != other.float) return false
        if (bigDecimal != other.bigDecimal) return false
        if (char != other.char) return false
        if (string != other.string) return false
        if (offsetTime != other.offsetTime) return false
        if (offsetDateTime != other.offsetDateTime) return false
        if (zonedDateTime != other.zonedDateTime) return false
        if (localDate != other.localDate) return false
        if (localTime != other.localTime) return false
        if (localDateTime != other.localDateTime) return false
        if (date != other.date) return false
        if (year != other.year) return false
        if (yearMonth != other.yearMonth) return false
        if (instant != other.instant) return false
        if (duration != other.duration) return false
        if (simpleDuration != other.simpleDuration) return false
        if (size != other.size) return false
        if (enum != other.enum) return false
        if (!booleanArray.contentEquals(other.booleanArray)) return false
        if (!nested.contentEquals(other.nested)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = empty ?: 0
        result = 31 * result + (literalEmpty ?: 0)
        result = 31 * result + (present ?: 0)
        result = 31 * result + boolean.hashCode()
        result = 31 * result + int
        result = 31 * result + short
        result = 31 * result + byte
        result = 31 * result + bigInteger.hashCode()
        result = 31 * result + long.hashCode()
        result = 31 * result + double.hashCode()
        result = 31 * result + float.hashCode()
        result = 31 * result + bigDecimal.hashCode()
        result = 31 * result + char.hashCode()
        result = 31 * result + string.hashCode()
        result = 31 * result + offsetTime.hashCode()
        result = 31 * result + offsetDateTime.hashCode()
        result = 31 * result + zonedDateTime.hashCode()
        result = 31 * result + localDate.hashCode()
        result = 31 * result + localTime.hashCode()
        result = 31 * result + localDateTime.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + year.hashCode()
        result = 31 * result + yearMonth.hashCode()
        result = 31 * result + instant.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + simpleDuration.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + enum.hashCode()
        result = 31 * result + booleanArray.contentHashCode()
        result = 31 * result + nested.contentHashCode()
        return result
    }
}