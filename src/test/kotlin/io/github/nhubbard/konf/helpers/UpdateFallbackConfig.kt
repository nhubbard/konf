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

import io.github.nhubbard.konf.Item
import io.github.nhubbard.konf.LazyItem
import io.github.nhubbard.konf.MergedConfig
import io.github.nhubbard.konf.Spec

class UpdateFallbackConfig(val config: MergedConfig) : MergedConfig(config.facade, config.fallback) {

    override fun rawSet(item: Item<*>, value: Any?) {
        if (item is LazyItem) {
            facade.rawSet(item, value)
        } else {
            fallback.rawSet(item, value)
        }
    }

    override fun unset(item: Item<*>) {
        fallback.unset(item)
    }

    override fun addItem(item: Item<*>, prefix: String) {
        fallback.addItem(item, prefix)
    }

    override fun addSpec(spec: Spec) {
        fallback.addSpec(spec)
    }
}