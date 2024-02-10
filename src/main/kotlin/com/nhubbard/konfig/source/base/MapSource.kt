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

import com.nhubbard.konfig.*
import com.nhubbard.konfig.source.SourceInfo

/**
 * Source from a hierarchical map.
 */
open class MapSource(
    val map: Map<String, Any>,
    type: String = "",
    info: SourceInfo = SourceInfo()
) : ValueSource(map, type.notEmptyOr("map"), info)

/**
 * Returns a hierarchical map for this config.
 *
 * The returned map contains all items in this config.
 * This map can be loaded into config as [com.nhubbard.konfig.source.base.MapSource] using
 * `config.from.map.hierarchical(map)`.
 */
@Suppress("UNCHECKED_CAST")
fun Config.toHierarchicalMap(): Map<String, Any> {
    return toTree().toHierarchical() as Map<String, Any>
}

/**
 * Returns a hierarchical value for this tree node.
 *
 * The returned value contains all items in this tree node.
 */
fun TreeNode.toHierarchical(): Any = withoutPlaceHolder().toHierarchicalInternal()

private fun TreeNode.toHierarchicalInternal(): Any {
    return when (this) {
        is ValueNode -> value
        is ListNode -> list.map { it.toHierarchicalInternal() }
        else -> children.mapValues { (_, child) -> child.toHierarchicalInternal() }
    }
}

/**
 * Source from an empty map.
 */
class EmptyMapSource : MapSource(emptyMap(), "empty map")
