package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.toValue
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestAdHocConfigItem {
    private val source = Source.from.map.hierarchical(
        mapOf(
            "size" to 1,
            "maxSize" to 2,
            "name" to "buffer",
            "type" to "ON_HEAP",
            "offset" to "null"
        )
    )

    @Test
    fun testAdHocConfigItemShouldLoadCorrectValues() {
        val config = Config().from.map.kv(
            mapOf(
                "network.buffer.size" to 1,
                "network.buffer.heap.type" to AdHocNetworkBuffer.Type.ON_HEAP,
                "network.buffer.offset" to 0
            )
        )
        val networkBuffer = AdHocNetworkBuffer(config)
        assertEquals(networkBuffer.size, 1)
        assertEquals(networkBuffer.maxSize, 2)
        assertEquals(networkBuffer.name, "buffer")
        assertEquals(networkBuffer.type, AdHocNetworkBuffer.Type.ON_HEAP)
        assertEquals(networkBuffer.offset, 0)
    }

    @Test
    fun testCastConfigWithComputedPropertyShouldLoadCorrectValues() {
        val networkBufferForCast: NetworkBufferForCast by Config().withSource(source).cast()
        assertEquals(networkBufferForCast.size, 1)
        assertEquals(networkBufferForCast.maxSize, 2)
        assertEquals(networkBufferForCast.name, "buffer")
        assertEquals(networkBufferForCast.type, NetworkBufferForCast.Type.ON_HEAP)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastConfigToClassShouldLoadCorrectValues() {
        val networkBufferForCast = Config().withSource(source).toValue<NetworkBufferForCast>()
        assertEquals(networkBufferForCast.size, 1)
        assertEquals(networkBufferForCast.maxSize, 2)
        assertEquals(networkBufferForCast.name, "buffer")
        assertEquals(networkBufferForCast.type, NetworkBufferForCast.Type.ON_HEAP)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastMultiLayerConfigToClassShouldLoadCorrectValues() {
        val networkBufferForCast = Config().withSource(source).from.json.string("").toValue<NetworkBufferForCast>()
        assertEquals(networkBufferForCast.size, 1)
        assertEquals(networkBufferForCast.maxSize, 2)
        assertEquals(networkBufferForCast.name, "buffer")
        assertEquals(networkBufferForCast.type, NetworkBufferForCast.Type.ON_HEAP)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastConfigWithMergedSourceToConfigClassShouldLoadCorrectValues() {
        val networkBufferForCast = Config().withSource(source + Source.from.json.string("")).toValue<NetworkBufferForCast>()
        assertEquals(networkBufferForCast.size, 1)
        assertEquals(networkBufferForCast.maxSize, 2)
        assertEquals(networkBufferForCast.name, "buffer")
        assertEquals(networkBufferForCast.type, NetworkBufferForCast.Type.ON_HEAP)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastSourceToConfigClassShouldLoadCorrectValues() {
        val networkBufferForCast = source.toValue<NetworkBufferForCast>()
        assertEquals(networkBufferForCast.size, 1)
        assertEquals(networkBufferForCast.maxSize, 2)
        assertEquals(networkBufferForCast.name, "buffer")
        assertEquals(networkBufferForCast.type, NetworkBufferForCast.Type.ON_HEAP)
        assertNull(networkBufferForCast.offset)
    }
}