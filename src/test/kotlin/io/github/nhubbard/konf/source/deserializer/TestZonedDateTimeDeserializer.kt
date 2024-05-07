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

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.source.ObjectMappingException
import io.github.nhubbard.konf.source.deserializer.helpers.BaseTestZonedDateTimeWrapper
import io.github.nhubbard.konf.source.helpers.assertCausedBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.ZonedDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestZonedDateTimeDeserializer {
    private val spec = object : ConfigSpec() {
        val item by required<BaseTestZonedDateTimeWrapper>()
    }
    private val config = Config {
        addSpec(spec)
    }

    @Test
    fun testZonedDateTimeDeserializer_onDeserializeValidString_itShouldSucceed() {
        config.from.map.kv(mapOf("item" to mapOf("zonedDateTime" to "2007-12-03T10:15:30+01:00[Europe/Paris]"))).apply {
            assertEquals(
                ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]"),
                this@apply[spec.item].zonedDateTime
            )
        }
    }

    @Test
    fun testZonedDateTimeDeserializer_onDeserializeEmptyString_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("zonedDateTime" to "  ")))
        }
    }

    @Test
    fun testZonedDateTimeDeserializer_onDeserializeValueWithValidType_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("zonedDateTime" to 1)))
        }
    }

    @Test
    fun testZonedDateTimeDeserializer_onDeserializeValueWithInvalidFormat_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("zonedDateTime" to "2007-12-03T10:15:30")))
        }
    }
}