/*
 * Copyright (c) 2017-2024 Uchuhimo
 * Copyright (c) 2024-present Nicholas Hubbard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for specific language governing permissions and
 * limitations under the License.
 */

package io.github.nhubbard.konf.source.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider
import com.fasterxml.jackson.databind.deser.std.StringDeserializer as JacksonStringDeserializer

object StringDeserializer : JacksonStringDeserializer() {
    private fun readResolve(): Any = StringDeserializer

    override fun _deserializeFromArray(p: JsonParser, ctxt: DeserializationContext): String? {
        val t = p.nextToken()
        if (t == JsonToken.END_ARRAY && ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)) {
            return getNullValue(ctxt)
        }
        if (ctxt.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
            val parsed = deserialize(p, ctxt)
            val token = p.nextToken()
            if (token != JsonToken.END_ARRAY) {
                return parsed + "," + deserializeFromRestOfArray(token, p, ctxt)
            }
            return parsed
        }
        return deserializeFromRestOfArray(t, p, ctxt)
    }

    private fun deserializeFromRestOfArray(
        token: JsonToken,
        p: JsonParser,
        ctxt: DeserializationContext
    ): String {
        var t = token
        val sb = StringBuilder(64)
        while (t != JsonToken.END_ARRAY) {
            val str = if (t == JsonToken.VALUE_STRING) {
                p.text
            } else {
                _parseString(p, ctxt, NullsConstantProvider.nuller())
            }
            if (sb.isEmpty()) {
                sb.append(str)
            } else {
                sb.append(',').append(str)
            }
            t = p.nextToken()
        }
        return sb.toString()
    }
}
