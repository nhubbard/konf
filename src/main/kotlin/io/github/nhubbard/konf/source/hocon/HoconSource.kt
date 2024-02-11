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

package io.github.nhubbard.konf.source.hocon

import com.typesafe.config.Config
import com.typesafe.config.ConfigList
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigValue
import com.typesafe.config.ConfigValueType
import io.github.nhubbard.konf.ContainerNode
import io.github.nhubbard.konf.TreeNode
import io.github.nhubbard.konf.source.ListSourceNode
import io.github.nhubbard.konf.source.NullSourceNode
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.SourceInfo
import io.github.nhubbard.konf.source.ValueSourceNode

private fun ConfigValue.toTree(): TreeNode {
    return when (valueType()!!) {
        ConfigValueType.NULL -> NullSourceNode
        ConfigValueType.BOOLEAN, ConfigValueType.NUMBER, ConfigValueType.STRING -> ValueSourceNode(unwrapped())
        ConfigValueType.LIST -> ListSourceNode(
            mutableListOf<TreeNode>().apply {
                for (value in (this@toTree as ConfigList)) {
                    add(value.toTree())
                }
            }
        )
        ConfigValueType.OBJECT -> ContainerNode(
            mutableMapOf<String, TreeNode>().apply {
                for ((key, value) in (this@toTree as ConfigObject)) {
                    put(key, value.toTree())
                }
            }
        )
    }
}

/**
 * Source from a HOCON value.
 */
class HoconSource(
    val value: Config
) : Source {
    override val info: SourceInfo = SourceInfo("type" to "HOCON")

    override val tree: TreeNode = value.root().toTree()
}
