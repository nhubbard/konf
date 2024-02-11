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

package io.github.nhubbard.konf.snippet

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.toValue
import io.github.nhubbard.konf.source.yaml.yaml
import io.github.nhubbard.konf.toValue
import java.io.File

object ServerSpec : ConfigSpec() {
    val host by optional("0.0.0.0")
    val tcpPort by required<Int>()
}

fun main() {
    val file = File("server.yml")
    //language=YAML
    file.writeText(
        """
        server:
            host: 127.0.0.1
            tcp_port: 8080
        """.trimIndent()
    )
    file.deleteOnExit()
    var config = Config { addSpec(ServerSpec) }
        .from.yaml.file("server.yml")
        .from.json.resource("server.json")
        .from.env()
        .from.systemProperties()
    run {
        config = Config { addSpec(ServerSpec) }.withSource(
            Source.from.yaml.file("server.yml") +
                    Source.from.json.resource("server.json") +
                    Source.from.env() +
                    Source.from.systemProperties()
        )
    }
    run {
        config = Config { addSpec(ServerSpec) }
            .from.yaml.watchFile("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
    }
    var server = Server(config[ServerSpec.host], config[ServerSpec.tcpPort])
    server.start()
    run {
        server = Config()
            .from.yaml.file("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
            .at("server")
            .toValue<Server>()
        server.start()
    }
    run {
        server = (
                Source.from.yaml.file("server.yml") +
                        Source.from.json.resource("server.json") +
                        Source.from.env() +
                        Source.from.systemProperties()
                )["server"]
            .toValue<Server>()
        server.start()
    }
}
