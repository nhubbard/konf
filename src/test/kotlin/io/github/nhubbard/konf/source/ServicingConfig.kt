package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.ConfigSpec

object ServicingConfig : ConfigSpec("servicing") {
    val baseURL by required<String>()
    val url by required<String>()
}