package io.github.nhubbard.konf.helpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class StringWrapperDeserializer : StdDeserializer<StringWrapper>(StringWrapper::class.java) {
    override fun deserialize(
        jp: JsonParser, ctxt: DeserializationContext
    ): StringWrapper {
        val node = jp.codec.readTree<JsonNode>(jp)
        return StringWrapper(node.textValue())
    }
}