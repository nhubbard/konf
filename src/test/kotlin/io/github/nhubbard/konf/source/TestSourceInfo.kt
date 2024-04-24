package io.github.nhubbard.konf.source

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSourceInfo {
    companion object {
        @JvmStatic val provider = { SourceInfo("a" to "1") }
    }

    @Test
    fun testSourceInfo_onUseAsMap_itShouldBehaveLikeAMap() {
        val subject = provider()
        assertEquals(mapOf("a" to "1"), subject.toMap())
    }

    @Test
    fun testSourceInfo_withNewKVPairs_itShouldContainThem() {
        val subject = provider()
        assertEquals(mapOf("a" to "1", "b" to "2", "c" to "3"), subject.with("b" to "2", "c" to "3").toMap())
    }

    @Test
    fun testSourceInfo_withOtherSourceInfo_itShouldContainTheNewKVPairsFromOtherInstance() {
        val subject = provider()
        assertEquals(
            mapOf("a" to "1", "b" to "2", "c" to "3"),
            subject.with(SourceInfo("b" to "2", "c" to "3")).toMap()
        )
    }
}