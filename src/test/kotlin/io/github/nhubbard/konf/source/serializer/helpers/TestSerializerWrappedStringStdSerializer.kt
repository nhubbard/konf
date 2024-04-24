package io.github.nhubbard.konf.source.serializer.helpers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class TestSerializerWrappedStringStdSerializer : StdSerializer<TestSerializerWrappedString>(TestSerializerWrappedString::class.java) {

    override fun serialize(value: TestSerializerWrappedString, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.string)
    }
}