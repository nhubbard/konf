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

package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.ValueNode
import io.github.nhubbard.konf.source.asValue
import io.github.nhubbard.konf.toPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestMapSource {
    private val provider = { MapSource(map = mapOf("1" to 1)) }

    @Test
    fun testMapSource_onGetUnderlyingMap_itShouldReturnTheSpecifiedMap() {
        val subject = provider()
        assertEquals(mapOf("1" to 1 as Any), subject.map)
    }

    @Test
    fun testMapSource_onCastToMap_itShouldSucceed() {
        val subject = provider()
        val map = subject.tree.children
        assertEquals(1 as Any, (map["1"] as ValueNode).value)
    }

    @Test
    fun testMapSource_onGetKey_itShouldContainTheKey() {
        val subject = provider()
        assertTrue("1".toPath() in subject)
    }

    @Test
    fun testMapSource_onGetKey_itShouldContainCorrespondingValue() {
        val subject = provider()
        assertEquals(1, subject.getOrNull("1".toPath())?.asValue<Int>())
    }

    @Test
    fun testMapSource_onGetInvalidKey_itShouldNotContainTheKey() {
        val subject = provider()
        assertFalse("2".toPath() in subject)
    }

    @Test
    fun testMapSource_onGetInvalidKey_itShouldNotContainCorrespondingValue() {
        val subject = provider()
        assertNull(subject.getOrNull("2".toPath()))
    }
}