package io.github.nhubbard.konf.source.helpers

import io.github.nhubbard.konf.ConfigSpec

object CustomDeserializerConfig : ConfigSpec("level1.level2") {
    val variantA by required<SealedClass>()
    val variantB by required<SealedClass>()
}