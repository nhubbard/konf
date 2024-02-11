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

package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.TreeNode
import io.github.nhubbard.konf.notEmptyOr
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.SourceInfo
import io.github.nhubbard.konf.source.asTree

/**
 * Source from a single value.
 */
open class ValueSource(
    val value: Any,
    type: String = "",
    final override val info: SourceInfo = SourceInfo()
) : Source {
    init {
        info["type"] = type.notEmptyOr("value")
    }

    override val tree: TreeNode = value.asTree()
}
