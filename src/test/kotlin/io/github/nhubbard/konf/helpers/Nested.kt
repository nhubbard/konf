package io.github.nhubbard.konf.helpers

import io.github.nhubbard.konf.ConfigSpec

object Nested : ConfigSpec("a.bb") {
    val item by required<Int>("int", "description")

    object Inner : ConfigSpec() {
        val item by required<Int>()
    }

    object Inner2 : ConfigSpec("inner2.level2") {
        val item by required<Int>()
    }

    object Inner3a : ConfigSpec("inner3.a") {
        val item by required<Int>()
    }

    object Inner3b : ConfigSpec("inner3.b") {
        val item by required<Int>()
    }
}