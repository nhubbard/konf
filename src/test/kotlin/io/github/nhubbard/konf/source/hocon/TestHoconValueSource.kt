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

package io.github.nhubbard.konf.source.hocon

import io.github.nhubbard.konf.source.NoSuchPathException
import io.github.nhubbard.konf.source.asValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHoconValueSource {
    @Test
    fun testHoconValueSource_onTreatObjectValueSourceAsHoconSource_itShouldContainSpecifiedValue() {
        val source = "{key = 1}".toHoconValueSource()
        assertTrue("key" in source)
        assertEquals(1, source["key"].asValue<Int>())
    }

    @Test
    fun testHoconValueSource_onTreatNumberValueSourceAsHoconSource_itShouldThrowNoSuchPathException() {
        val source = "1".toHoconValueSource()
        assertThrows<NoSuchPathException> { source["key"] }
    }

    @Test
    fun testHoconValueSource_onGetIntegerFromIntegerValueSource_itShouldSucceed() {
        assertEquals(1, "1".toHoconValueSource().asValue<Int>())
    }

    @Test
    fun testHoconValueSource_onGetLongFromLongValueSource_itShouldSucceed() {
        val source = "123456789000".toHoconValueSource()
        assertEquals(123_456_789_000L, source.asValue<Long>())
    }

    @Test
    fun testHoconValueSource_onGetLongFromIntegerValueSource_itShouldSucceed() {
        val source = "1".toHoconValueSource()
        assertEquals(1L, source.asValue<Long>())
    }

    @Test
    fun testHoconValueSource_onGetDoubleFromDoubleValueSource_itShouldSucceed() {
        val source = "1.5".toHoconValueSource()
        assertEquals(1.5, source.asValue<Double>())
    }

    @Test
    fun testHoconValueSource_onGetDoubleFromIntValueSource_itShouldSucceed() {
        val source = "1".toHoconValueSource()
        assertEquals(1.0, source.asValue<Double>())
    }

    @Test
    fun testHoconValueSource_onGetDoubleFromLongValueSource_itShouldSucceed() {
        val source = "123456789000".toHoconValueSource()
        assertEquals(123456789000.0, source.asValue<Double>())
    }
}