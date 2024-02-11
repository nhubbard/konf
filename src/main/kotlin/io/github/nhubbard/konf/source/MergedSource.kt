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

import io.github.nhubbard.konf.Feature
import io.github.nhubbard.konf.MergedMap
import io.github.nhubbard.konf.Path
import io.github.nhubbard.konf.TreeNode
import java.util.*

class MergedSource(val facade: Source, val fallback: Source) : Source {
    override val info: SourceInfo by lazy {
        SourceInfo(
            "facade" to facade.description,
            "fallback" to fallback.description
        )
    }

    override val tree: TreeNode by lazy { facade.tree.withFallback(fallback.tree) }

    override val features: Map<Feature, Boolean> by lazy {
        MergedMap(
            Collections.unmodifiableMap(fallback.features),
            Collections.unmodifiableMap(facade.features)
        )
    }

    override fun disabled(feature: Feature): Source = MergedSource(facade.disabled(feature), fallback)

    override fun enabled(feature: Feature): Source = MergedSource(facade.enabled(feature), fallback)

    override fun substituted(root: Source, enabled: Boolean, errorWhenUndefined: Boolean): Source {
        val substitutedFacade = facade.substituted(root, enabled, errorWhenUndefined)
        val substitutedFallback = fallback.substituted(root, enabled, errorWhenUndefined)
        return if (substitutedFacade === facade && substitutedFallback === fallback) {
            this
        } else {
            MergedSource(substitutedFacade, substitutedFallback)
        }
    }

    override fun lowercased(enabled: Boolean): Source {
        val lowercasedFacade = facade.lowercased(enabled)
        val lowercasedFallback = fallback.lowercased(enabled)
        return if (lowercasedFacade === facade && lowercasedFallback === fallback) {
            this
        } else {
            MergedSource(lowercasedFacade, lowercasedFallback)
        }
    }

    override fun littleCamelCased(enabled: Boolean): Source {
        val littleCamelCasedFacade = facade.littleCamelCased(enabled)
        val littleCamelCasedFallback = fallback.littleCamelCased(enabled)
        return if (littleCamelCasedFacade === facade && littleCamelCasedFallback === fallback) {
            this
        } else {
            MergedSource(littleCamelCasedFacade, littleCamelCasedFallback)
        }
    }

    override fun normalized(lowercased: Boolean, littleCamelCased: Boolean): Source {
        val normalizedFacade = facade.normalized(lowercased, littleCamelCased)
        val normalizedFallback = fallback.normalized(lowercased, littleCamelCased)
        return if (normalizedFacade === facade && normalizedFallback === fallback) {
            this
        } else {
            MergedSource(normalizedFacade, normalizedFallback)
        }
    }

    override fun getNodeOrNull(path: Path, lowercased: Boolean, littleCamelCased: Boolean): TreeNode? {
        val facadeNode = facade.getNodeOrNull(path, lowercased, littleCamelCased)
        val fallbackNode = fallback.getNodeOrNull(path, lowercased, littleCamelCased)
        return if (facadeNode != null) {
            if (fallbackNode != null) {
                facadeNode.withFallback(fallbackNode)
            } else {
                facadeNode
            }
        } else {
            fallbackNode
        }
    }

    override fun getOrNull(path: Path): Source? {
        return if (path.isEmpty()) {
            this
        } else {
            val subFacade = facade.getOrNull(path)
            val subFallback = fallback.getOrNull(path)
            if (subFacade != null) {
                if (subFallback != null) {
                    MergedSource(subFacade, subFallback)
                } else {
                    subFacade
                }
            } else {
                subFallback
            }
        }
    }

    override fun withPrefix(prefix: Path): Source {
        return if (prefix.isEmpty()) {
            this
        } else {
            MergedSource(facade.withPrefix(prefix), fallback.withPrefix(prefix))
        }
    }
}
