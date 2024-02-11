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

package io.github.nhubbard.konf.source.yaml

import io.github.nhubbard.konf.annotation.JavaApi
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.RegisterExtension
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.asSource
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.AbstractConstruct
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.Tag
import java.io.InputStream
import java.io.Reader

/**
 * Provider for YAML source.
 */
@RegisterExtension(["yml", "yaml"])
object YamlProvider : Provider {
    override fun reader(reader: Reader): Source {
        return load(reader)
    }

    override fun inputStream(inputStream: InputStream): Source {
        return load(inputStream)
    }

    private fun load(input: Any): Source {
        val yaml = Yaml(YamlConstructor())
        val value: Any = when (input) {
            is Reader -> yaml.load<Any>(input)
            is InputStream -> yaml.load(input)
            else -> error("This is an impossible condition.")
        }
        return if (value == "null") {
            mapOf<String, Any>().asSource("YAML")
        } else {
            value.asSource("YAML")
        }
    }

    @JavaApi
    @JvmStatic
    fun get() = this
}

private class YamlConstructor : SafeConstructor(LoaderOptions()) {
    init {
        yamlConstructors[Tag.NULL] = object : AbstractConstruct() {
            override fun construct(node: Node?): Any {
                if (node != null) {
                    constructScalar(node as ScalarNode)
                }
                return "null"
            }
        }
    }
}
