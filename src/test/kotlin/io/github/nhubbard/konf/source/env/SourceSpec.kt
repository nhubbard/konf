package io.github.nhubbard.konf.source.env

import io.github.nhubbard.konf.ConfigSpec

object SourceSpec : ConfigSpec() {
    object Test : ConfigSpec() {
        val type by required<String>()
    }

    val camelCase by required<Boolean>()
}