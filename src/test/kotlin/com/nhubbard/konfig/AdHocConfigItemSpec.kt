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

package com.nhubbard.konfig

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhubbard.konfig.source.Source
import com.nhubbard.konfig.source.toValue
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertNull

object AdHocConfigItemSpec : Spek({
    on("load config into ad-hoc config class with ad-hoc config items") {
        val config = Config().from.map.kv(
            mapOf(
                "network.buffer.size" to 1,
                "network.buffer.heap.type" to AdHocNetworkBuffer.Type.ON_HEAP,
                "network.buffer.offset" to 0
            )
        )
        val networkBuffer = AdHocNetworkBuffer(config)
        it("should load correct values") {
            assertThat(networkBuffer.size, equalTo(1))
            assertThat(networkBuffer.maxSize, equalTo(2))
            assertThat(networkBuffer.name, equalTo("buffer"))
            assertThat(networkBuffer.type, equalTo(AdHocNetworkBuffer.Type.ON_HEAP))
            assertThat(networkBuffer.offset, equalTo(0))
        }
    }
    val source = Source.from.map.hierarchical(
        mapOf(
            "size" to 1,
            "maxSize" to 2,
            "name" to "buffer",
            "type" to "ON_HEAP",
            "offset" to "null"
        )
    )
    on("cast config to config class property") {
        val networkBufferForCast: NetworkBufferForCast by Config().withSource(source).cast()
        it("should load correct values") {
            assertThat(networkBufferForCast.size, equalTo(1))
            assertThat(networkBufferForCast.maxSize, equalTo(2))
            assertThat(networkBufferForCast.name, equalTo("buffer"))
            assertThat(networkBufferForCast.type, equalTo(NetworkBufferForCast.Type.ON_HEAP))
            assertNull(networkBufferForCast.offset)
        }
    }
    on("cast config to config class") {
        val networkBufferForCast = Config().withSource(source).toValue<NetworkBufferForCast>()
        it("should load correct values") {
            assertThat(networkBufferForCast.size, equalTo(1))
            assertThat(networkBufferForCast.maxSize, equalTo(2))
            assertThat(networkBufferForCast.name, equalTo("buffer"))
            assertThat(networkBufferForCast.type, equalTo(NetworkBufferForCast.Type.ON_HEAP))
            assertNull(networkBufferForCast.offset)
        }
    }
    on("cast multi-layer config to config class") {
        val networkBufferForCast = Config().withSource(source).from.json.string("").toValue<NetworkBufferForCast>()
        it("should load correct values") {
            assertThat(networkBufferForCast.size, equalTo(1))
            assertThat(networkBufferForCast.maxSize, equalTo(2))
            assertThat(networkBufferForCast.name, equalTo("buffer"))
            assertThat(networkBufferForCast.type, equalTo(NetworkBufferForCast.Type.ON_HEAP))
            assertNull(networkBufferForCast.offset)
        }
    }
    on("cast config with merged source to config class") {
        val networkBufferForCast = Config().withSource(source + Source.from.json.string("")).toValue<NetworkBufferForCast>()
        it("should load correct values") {
            assertThat(networkBufferForCast.size, equalTo(1))
            assertThat(networkBufferForCast.maxSize, equalTo(2))
            assertThat(networkBufferForCast.name, equalTo("buffer"))
            assertThat(networkBufferForCast.type, equalTo(NetworkBufferForCast.Type.ON_HEAP))
            assertNull(networkBufferForCast.offset)
        }
    }
    on("cast source to config class") {
        val networkBufferForCast = source.toValue<NetworkBufferForCast>()
        it("should load correct values") {
            assertThat(networkBufferForCast.size, equalTo(1))
            assertThat(networkBufferForCast.maxSize, equalTo(2))
            assertThat(networkBufferForCast.name, equalTo("buffer"))
            assertThat(networkBufferForCast.type, equalTo(NetworkBufferForCast.Type.ON_HEAP))
            assertNull(networkBufferForCast.offset)
        }
    }
})

data class NetworkBufferForCast(
    val size: Int,
    val maxSize: Int,
    val name: String,
    val type: Type,
    val offset: Int?
) {

    enum class Type {
        ON_HEAP, OFF_HEAP
    }
}