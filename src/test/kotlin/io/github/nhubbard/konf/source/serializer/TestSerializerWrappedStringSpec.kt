package io.github.nhubbard.konf.source.serializer

import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.source.serializer.helpers.TestSerializerWrappedString

object TestSerializerWrappedStringSpec : ConfigSpec("") {
    val wrappedString by optional(name = "wrapped-string", default = TestSerializerWrappedString("value"))
}