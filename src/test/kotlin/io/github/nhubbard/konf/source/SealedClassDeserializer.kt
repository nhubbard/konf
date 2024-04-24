package io.github.nhubbard.konf.source

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class SealedClassDeserializer : StdDeserializer<SealedClass>(SealedClass::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SealedClass {
        val node: JsonNode = p.codec.readTree(p)
        return if (node.has("int")) {
            VariantA(node.get("int").asInt())
        } else {
            VariantB(node.get("double").asDouble())
        }
    }
}