package io.github.nhubbard.konf.source.helpers

import io.github.nhubbard.konf.ConfigSpec

object FlattenDefaultLoadersConfig : ConfigSpec("") {
    val SOURCE_TEST_TYPE by required<String>()
}