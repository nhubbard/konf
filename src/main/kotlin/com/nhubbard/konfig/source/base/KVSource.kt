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

package com.nhubbard.konfig.source.base

import com.nhubbard.konfig.ContainerNode
import com.nhubbard.konfig.TreeNode
import com.nhubbard.konfig.notEmptyOr
import com.nhubbard.konfig.source.SourceInfo
import com.nhubbard.konfig.source.asTree

/**
 * Source from a map in key-value format.
 */
open class KVSource(
    val map: Map<String, Any>,
    type: String = "",
    info: SourceInfo = SourceInfo()
) : ValueSource(map, type.notEmptyOr("KV"), info) {
    override val tree: TreeNode = map.kvToTree()
}

fun Map<String, Any>.kvToTree(): TreeNode {
    return ContainerNode(mutableMapOf()).apply {
        this@kvToTree.forEach { (path, value) ->
            set(path, value.asTree())
        }
    }
}

fun Map<String, Any>.asKVSource() = KVSource(this)
