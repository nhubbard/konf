package io.github.nhubbard.konf

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestMergedMap {
    private val facadeMap: () -> MutableMap<String, Int> = { mutableMapOf("a" to 1, "b" to 2) }
    private val fallbackMap: () -> MutableMap<String, Int> = { mutableMapOf("b" to 3, "c" to 4) }
    private val mergedMap = mapOf("a" to 1, "b" to 2, "c" to 4)
    private val provider: () -> MergedMap<String, Int> = { MergedMap(fallback = fallbackMap(), facade = facadeMap()) }

    @Test
    fun testMergedMap_onGetSize_itShouldReturnTheMergedSize() {
        val subject = provider()
        assertEquals(subject.size, 3)
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
        assertEquals(subject["a"], 1)
        assertEquals(subject["b"], 2)
        assertEquals(subject["c"], 4)
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
        assertEquals(subject.entries, mergedMap.entries)
    }

    @Test
    fun testMergedMap_onGetKeys_itShouldReturnKeysFromBothMaps() {
        val subject = provider()
        assertEquals(subject.keys, mergedMap.keys)
    }

    @Test
    fun testMergedMap_onGetValues_itShouldReturnValuesFromBothMaps() {
        val subject = provider()
        assertEquals(subject.values.toList(), mergedMap.values.toList())
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
        assertEquals(subject["d"], 5)
        assertEquals(subject.facade["d"], 5)
        assertNull(subject.fallback["d"])
    }

    @Test
    fun testMergedMap_onPutNewPairs_itShouldPutThemInTheFacadeMap() {
        val subject = provider()
        subject.putAll(mapOf("d" to 5, "e" to 6))
        assertEquals(subject["d"], 5)
        assertEquals(subject["e"], 6)
        assertEquals(subject.facade["d"], 5)
        assertEquals(subject.facade["e"], 6)
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