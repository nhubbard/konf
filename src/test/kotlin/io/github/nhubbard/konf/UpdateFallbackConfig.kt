package io.github.nhubbard.konf

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