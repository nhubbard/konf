package io.github.nhubbard.konf.source.env

import io.github.nhubbard.konf.ConfigSpec

object FlattenSourceSpec : ConfigSpec("") {
    val SOURCE_CAMELCASE by required<Boolean>()
    val SOURCE_TEST_TYPE by required<String>()
}