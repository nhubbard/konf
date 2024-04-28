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

package io.github.nhubbard.konf.source.helpers

import io.github.nhubbard.konf.SizeInBytes
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.util.*

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