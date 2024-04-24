package io.github.nhubbard.konf.source.serializer.helpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class TestSerializerWrappedStringStdDeserializer : StdDeserializer<TestSerializerWrappedString>(
    TestSerializerWrappedString::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TestSerializerWrappedString {
        return TestSerializerWrappedString(p.valueAsString)
    }
}