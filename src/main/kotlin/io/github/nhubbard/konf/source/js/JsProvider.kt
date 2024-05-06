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

package io.github.nhubbard.konf.source.js

import io.github.nhubbard.konf.annotation.JavaApi
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.RegisterExtension
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.json.JsonProvider
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Engine
import java.io.InputStream
import java.io.Reader
import java.util.stream.Collectors

/**
 * Provider for JavaScript source.
 */
@RegisterExtension(["js"])
object JsProvider : Provider {
    override fun reader(reader: Reader): Source {
        val sourceString = reader.buffered().lines().collect(Collectors.joining("\n"))
        val engine = Engine.newBuilder().option("engine.WarnInterpreterOnly", "false").build()
        Context.newBuilder("js").engine(engine).build().use { context ->
            val value = context.eval("js", sourceString)
            context.getBindings("js").putMember("source", value)
            val jsonString = context.eval("js", "JSON.stringify(source)").asString()
            return JsonProvider.string(jsonString).apply {
                this.info["type"] = "JavaScript"
            }
        }
    }

    override fun inputStream(inputStream: InputStream): Source {
        inputStream.reader().use {
            return reader(it)
        }
    }

    @JavaApi
    @JvmStatic
    fun get() = this
}
