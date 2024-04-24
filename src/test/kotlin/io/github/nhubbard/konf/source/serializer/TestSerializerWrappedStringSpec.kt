package io.github.nhubbard.konf.source.serializer

import io.github.nhubbard.konf.ConfigSpec

object TestSerializerWrappedStringSpec : ConfigSpec("") {
    val wrappedString by optional(name = "wrapped-string", default = TestSerializerWrappedString("value"))
}