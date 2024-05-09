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

import io.github.nhubbard.konf.helpers.AdHocNetworkBuffer
import io.github.nhubbard.konf.helpers.NetworkBufferForCast
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.toValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
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
    fun testAdHocConfigItem_shouldLoadCorrectValues() {
        val config = Config().from.map.kv(
            mapOf(
                "network.buffer.size" to 1,
                "network.buffer.heap.type" to AdHocNetworkBuffer.Type.ON_HEAP,
                "network.buffer.offset" to 0
            )
        )
        val networkBuffer = AdHocNetworkBuffer(config)
        assertEquals(1, networkBuffer.size)
        assertEquals(2, networkBuffer.maxSize)
        assertEquals("buffer", networkBuffer.name)
        assertEquals(AdHocNetworkBuffer.Type.ON_HEAP, networkBuffer.type)
        assertEquals(0, networkBuffer.offset)
    }

    @Test
    fun testCastConfigWithComputedProperty_shouldLoadCorrectValues() {
        val networkBufferForCast: NetworkBufferForCast by Config().withSource(source).cast()
        assertEquals(1, networkBufferForCast.size)
        assertEquals(2, networkBufferForCast.maxSize)
        assertEquals("buffer", networkBufferForCast.name)
        assertEquals(NetworkBufferForCast.Type.ON_HEAP, networkBufferForCast.type)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastConfigToClass_shouldLoadCorrectValues() {
        val networkBufferForCast = Config().withSource(source).toValue<NetworkBufferForCast>()
        assertEquals(1, networkBufferForCast.size)
        assertEquals(2, networkBufferForCast.maxSize)
        assertEquals("buffer", networkBufferForCast.name)
        assertEquals(NetworkBufferForCast.Type.ON_HEAP, networkBufferForCast.type)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastMultiLayerConfigToClass_shouldLoadCorrectValues() {
        val networkBufferForCast = Config().withSource(source).from.json.string("").toValue<NetworkBufferForCast>()
        assertEquals(1, networkBufferForCast.size)
        assertEquals(2, networkBufferForCast.maxSize)
        assertEquals("buffer", networkBufferForCast.name)
        assertEquals(NetworkBufferForCast.Type.ON_HEAP, networkBufferForCast.type)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastConfigWithMergedSourceToConfigClass_shouldLoadCorrectValues() {
        val networkBufferForCast = Config().withSource(source + Source.from.json.string("")).toValue<NetworkBufferForCast>()
        assertEquals(1, networkBufferForCast.size)
        assertEquals(2, networkBufferForCast.maxSize)
        assertEquals("buffer", networkBufferForCast.name)
        assertEquals(NetworkBufferForCast.Type.ON_HEAP, networkBufferForCast.type)
        assertNull(networkBufferForCast.offset)
    }

    @Test
    fun testCastSourceToConfigClass_shouldLoadCorrectValues() {
        val networkBufferForCast = source.toValue<NetworkBufferForCast>()
        assertEquals(1, networkBufferForCast.size)
        assertEquals(2, networkBufferForCast.maxSize)
        assertEquals("buffer", networkBufferForCast.name)
        assertEquals(NetworkBufferForCast.Type.ON_HEAP, networkBufferForCast.type)
        assertNull(networkBufferForCast.offset)
    }
}