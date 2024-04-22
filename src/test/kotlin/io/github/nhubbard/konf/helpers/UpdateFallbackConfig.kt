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