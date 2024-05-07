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
import io.github.nhubbard.konf.source.deserializer.helpers.BaseTestDurationWrapper
import io.github.nhubbard.konf.source.helpers.assertCausedBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.time.Duration
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestDurationDeserializer {
    private val spec = object : ConfigSpec() {
        val item by required<BaseTestDurationWrapper>()
    }
    private val config = Config {
        addSpec(spec)
    }

    @Test
    fun testDurationDeserializer_onDeserializeValidString_itShouldSucceed() {
        config.from.map.kv(mapOf("item" to mapOf("duration" to "P2DT3H4M"))).apply {
            assertEquals(Duration.parse("P2DT3H4M"), this@apply[spec.item].duration)
        }
    }

    @Test
    fun testDurationDeserializer_onDeserializeEmptyString_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("duration" to "  ")))
        }
    }

    @Test
    fun testDurationDeserializer_onDeserializeValueWithInvalidType_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("duration" to 1)))
        }
    }

    @Test
    fun testDurationDeserializer_onDeserializeValueWithInvalidFormat_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.from.map.kv(mapOf("item" to mapOf("duration" to "*1s")))
        }
    }
}