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
import io.github.nhubbard.konf.source.deserializer.helpers.OffsetDateTimeWrapper
import io.github.nhubbard.konf.source.helpers.assertCausedBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.OffsetDateTime
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestOffsetDateTimeDeserializer {
    private val spec = object : ConfigSpec() {
        val item by required<OffsetDateTimeWrapper>()
    }
    private val config = Config {
        addSpec(spec)
    }

    @Test
    fun testOffsetDateTimeDeserializer_onDeserializeValidString_itShouldSucceed() {
        config.from.map.kv(mapOf("item" to mapOf("offsetDateTime" to "2007-12-03T10:15:30+01:00"))).apply {
            assertEquals(OffsetDateTime.parse("2007-12-03T10:15:30+01:00"), this@apply[spec.item].offsetDateTime)
        }
    }

    @Test
    fun testOffsetDateTimeDeserializer_onDeserializeEmptyString_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("offsetDateTime" to "  ")))
        }
    }

    @Test
    fun testOffsetDateTimeDeserializer_onDeserializeValueWithInvalidType_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("offsetDateTime" to 1)))
        }
    }

    @Test
    fun testOffsetDateTimeDeserializer_onDeserializeValueWithInvalidFormat_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("offsetDateTime" to "2007-12-03T10:15:30")))
        }
    }
}