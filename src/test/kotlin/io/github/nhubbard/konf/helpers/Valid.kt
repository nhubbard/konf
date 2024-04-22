package io.github.nhubbard.konf.helpers

import io.github.nhubbard.konf.ConfigSpec

object Valid : ConfigSpec("level1.level2") {
    val valid by required<String>()
}