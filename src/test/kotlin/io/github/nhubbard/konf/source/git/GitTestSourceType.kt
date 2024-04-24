package io.github.nhubbard.konf.source.git

import io.github.nhubbard.konf.ConfigSpec

object GitTestSourceType : ConfigSpec("") {
    val type by required<String>()
}