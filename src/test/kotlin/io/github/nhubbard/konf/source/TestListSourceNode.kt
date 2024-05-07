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

package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.EmptyNode
import io.github.nhubbard.konf.TreeNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestListSourceNode {
    @Test
    fun testListSourceNode_onGetChildren_itShouldReturnAMapIndexedByInteger() {
        val subject = ListSourceNode(listOf(EmptyNode, EmptyNode))
        assertEquals(
            subject.children,
            mutableMapOf<String, TreeNode>(
                "0" to EmptyNode,
                "1" to EmptyNode
            )
        )
    }
}