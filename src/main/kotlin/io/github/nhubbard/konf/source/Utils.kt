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

import io.github.nhubbard.konf.getUnits
import java.time.Duration
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

/**
 * Parses specified string to duration.
 *
 * @receiver specified string
 * @return duration
 */
fun String.toDuration(): Duration {
    return try {
        Duration.parse(this)
    } catch (e: DateTimeParseException) {
        Duration.ofNanos(parseDuration(this))
    }
}

/**
 * Parses a duration string. If no units are specified in the string, it is
 * assumed to be in milliseconds. The returned duration is in nanoseconds.
 *
 * @param input the string to parse
 * @return duration in nanoseconds
 */
internal fun parseDuration(input: String): Long {
    val s = input.trim()
    val originalUnitString = getUnits(s)
    var unitString = originalUnitString
    val numberString = s.substring(0, s.length - unitString.length).trim()

    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.isEmpty())
        throw ParseException("No number in duration value '$input'")

    if (unitString.length > 2 && !unitString.endsWith("s"))
        unitString += "s"

    // note that this is deliberately case-sensitive
    val units = when (unitString) {
        "", "ms", "millis", "milliseconds" -> TimeUnit.MILLISECONDS
        "us", "micros", "microseconds" -> TimeUnit.MICROSECONDS
        "ns", "nanos", "nanoseconds" -> TimeUnit.NANOSECONDS
        "d", "days" -> TimeUnit.DAYS
        "h", "hours" -> TimeUnit.HOURS
        "s", "seconds" -> TimeUnit.SECONDS
        "m", "minutes" -> TimeUnit.MINUTES
        else -> throw ParseException("Could not parse time unit '$originalUnitString' (try ns, us, ms, s, m, h, d)")
    }

    return try {
        // if the string is purely digits, parse as an integer to avoid
        // possible precision loss;
        // otherwise as a double.
        if (numberString.matches("[+-]?[0-9]+".toRegex())) {
            units.toNanos(java.lang.Long.parseLong(numberString))
        } else {
            val nanosInUnit = units.toNanos(1)
            (java.lang.Double.parseDouble(numberString) * nanosInUnit).toLong()
        }
    } catch (e: NumberFormatException) {
        throw ParseException("Could not parse duration number '$numberString'")
    }
}
