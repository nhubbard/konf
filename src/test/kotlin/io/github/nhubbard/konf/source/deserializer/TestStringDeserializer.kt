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

import com.fasterxml.jackson.databind.DeserializationFeature
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.source.ObjectMappingException
import io.github.nhubbard.konf.source.deserializer.helpers.BaseTestStringWrapper
import io.github.nhubbard.konf.source.helpers.assertCausedBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestStringDeserializer {
    private val spec = object : ConfigSpec() {
        val item by required<BaseTestStringWrapper>()
    }
    private val config = Config {
        addSpec(spec)
    }

    @Test
    fun testStringDeserializer_onDeserializeStringContainingCommas_itShouldSucceed() {
        config.from.map.kv(mapOf("item" to mapOf("string" to "a,b,c"))).apply {
            assertEquals("a,b,c", this@apply[spec.item].string)
        }
    }

    @Test
    fun testStringDeserializer_onDeserializeStringWithCommasWhenUnwrapSingleValueArraysIsEnabled_itShouldSucceed() {
        config.apply {
            mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
        }.from.map.kv(mapOf("item" to mapOf("string" to "a,b,c"))).apply {
            assertEquals("a,b,c", this@apply[spec.item].string)
        }
    }

    @Test
    fun testStringDeserializer_onDeserializeStringFromNumber_itShouldSucceed() {
        config.from.map.kv(mapOf("item" to mapOf("string" to 1))).apply {
            assertEquals("1", this@apply[spec.item].string)
        }
    }

    @Test
    fun testStringDeserializer_onDeserializeStringFromListOfNumbers_itShouldSucceed() {
        config.from.map.kv(mapOf("item" to mapOf("string" to listOf(1, 2)))).apply {
            assertEquals("1,2", this@apply[spec.item].string)
        }
    }

    @Test
    fun testStringDeserializer_onDeserializeStringFromSingleValueArray_itShouldSucceed() {
        config.apply {
            mapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
        }.from.map.kv(mapOf("item" to mapOf("string" to listOf("a")))).apply {
            assertEquals("a", this@apply[spec.item].string)
        }
    }

    @Test
    fun testStringDeserializer_onDeserializeStringFromEmptyArray_itShouldThrowLoadException_causedByObjectMappingException() {
        assertCausedBy<ObjectMappingException> {
            config.apply {
                mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            }.from.map.kv(mapOf("item" to mapOf("string" to listOf<String>())))
        }
    }
}