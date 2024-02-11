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

package io.github.nhubbard.konf.source.deserializer

import io.github.nhubbard.konf.source.SourceException
import io.github.nhubbard.konf.source.toDuration
import java.time.format.DateTimeParseException
import java.time.Duration

/**
 * Deserializer for [Duration].
 */
object DurationDeserializer : JSR310Deserializer<Duration>(Duration::class.java) {
    private fun readResolve(): Any = DurationDeserializer

    override fun parse(string: String): Duration {
        return try {
            Duration.parse(string)
        } catch (exception: DateTimeParseException) {
            try {
                string.toDuration()
            } catch (_: SourceException) {
                throw exception
            }
        }
    }
}
