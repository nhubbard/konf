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

package io.github.nhubbard.konf

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
class TestMergedMap {
    private val facadeMap: () -> MutableMap<String, Int> = { mutableMapOf("a" to 1, "b" to 2) }
    private val fallbackMap: () -> MutableMap<String, Int> = { mutableMapOf("b" to 3, "c" to 4) }
    private val mergedMap = mapOf("a" to 1, "b" to 2, "c" to 4)
    private val provider: () -> MergedMap<String, Int> = { MergedMap(fallback = fallbackMap(), facade = facadeMap()) }

    @Test
    fun testMergedMap_onGetSize_itShouldReturnTheMergedSize() {
        val subject = provider()
        assertEquals(3, subject.size)
    }

    @Test
    fun testMergedMap_onQueryWhetherItContainsAKey_itShouldQueryBothMaps() {
        val subject = provider()
        assertTrue("a" in subject)
        assertTrue("c" in subject)
        assertFalse("d" in subject)
    }

    @Test
    fun testMergedMap_onQueryWhetherItContainsAValue_itShouldQueryInBothMaps() {
        val subject = provider()
        assertTrue(subject.containsValue(1))
        assertTrue(subject.containsValue(4))
        assertFalse(subject.containsValue(5))
    }

    @Test
    fun testMergedMap_onGetValue_itShouldQueryBothMaps() {
        val subject = provider()
        assertEquals(1, subject["a"])
        assertEquals(2, subject["b"])
        assertEquals(4, subject["c"])
        assertNull(subject["d"])
    }

    @Test
    fun testMergedMap_onQueryIsEmpty_itShouldQueryBothMaps() {
        val subject = provider()
        assertFalse(subject.isEmpty())
        assertFalse(MergedMap(mutableMapOf("a" to 1), mutableMapOf()).isEmpty())
        assertFalse(MergedMap(mutableMapOf(), mutableMapOf("a" to 1)).isEmpty())
        assertTrue(MergedMap<String, Int>(mutableMapOf(), mutableMapOf()).isEmpty())
    }

    @Test
    fun testMergedMap_onGetEntries_itShouldReturnEntriesInBothMaps() {
        val subject = provider()
        assertEquals(mergedMap.entries, subject.entries)
    }

    @Test
    fun testMergedMap_onGetKeys_itShouldReturnKeysFromBothMaps() {
        val subject = provider()
        assertEquals(mergedMap.keys, subject.keys)
    }

    @Test
    fun testMergedMap_onGetValues_itShouldReturnValuesFromBothMaps() {
        val subject = provider()
        assertEquals(mergedMap.values.toList(), subject.values.toList())
    }

    @Test
    fun testMergedMap_onClear_itShouldClearBothMaps() {
        val subject = provider()
        subject.clear()
        assertTrue(subject.isEmpty())
        assertTrue(subject.facade.isEmpty())
        assertTrue(subject.fallback.isEmpty())
    }

    @Test
    fun testMergedMap_onAddNewPair_itShouldBePlacedInTheFacadeMap() {
        val subject = provider()
        subject["d"] = 5
        assertEquals(5, subject["d"])
        assertEquals(5, subject.facade["d"])
        assertNull(subject.fallback["d"])
    }

    @Test
    fun testMergedMap_onPutNewPairs_itShouldPutThemInTheFacadeMap() {
        val subject = provider()
        subject.putAll(mapOf("d" to 5, "e" to 6))
        assertEquals(5, subject["d"])
        assertEquals(6, subject["e"])
        assertEquals(5, subject.facade["d"])
        assertEquals(6, subject.facade["e"])
        assertNull(subject.fallback["d"])
        assertNull(subject.fallback["e"])
    }

    @Test
    fun testMergedMap_onRemoveKey_shouldRemoveKeyFromFacadeIfPresent() {
        val subject = provider()
        subject.remove("a")
        assertFalse("a" in subject)
        assertFalse("a" in subject.facade)
    }

    @Test
    fun testMergedMap_onRemoveKey_shouldRemoveKeyFromFallbackMapIfPresent() {
        val subject = provider()
        subject.remove("c")
        assertFalse("c" in subject)
        assertFalse("c" in subject.fallback)
    }

    @Test
    fun testMergedMap_onRemoveKey_shouldRemoveKeyFromBothMapsIfPresent() {
        val subject = provider()
        subject.remove("b")
        assertFalse("b" in subject)
        assertFalse("b" in subject.facade)
        assertFalse("b" in subject.fallback)
    }
}