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

package io.github.nhubbard.konf.helpers

import io.github.nhubbard.konf.ConfigSpec

class NetworkBuffer {
    companion object : ConfigSpec("network.buffer") {
        val size by required<Int>(description = "size of buffer in KB")

        val maxSize by lazy(description = "max size of buffer in KB") { it[size] * 2 }

        val name by optional("buffer", description = "name of buffer")

        val type by optional(
            Type.OFF_HEAP,
            description =
            """
                | type of network buffer.
                | two type:
                | - on-heap
                | - off-heap
                | buffer is off-heap by default.
                """.trimMargin("| ")
        )

        val offset by optional<Int?>(null, description = "initial offset of buffer")
    }

    enum class Type {
        ON_HEAP, OFF_HEAP
    }
}
