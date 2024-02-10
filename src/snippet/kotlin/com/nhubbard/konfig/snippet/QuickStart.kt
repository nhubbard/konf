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

package com.nhubbard.konfig.snippet

import com.nhubbard.konfig.Config
import com.nhubbard.konfig.ConfigSpec
import com.nhubbard.konfig.source.Source
import com.nhubbard.konfig.source.toValue
import com.nhubbard.konfig.source.yaml.yaml
import com.nhubbard.konfig.toValue
import java.io.File

object ServerSpec : ConfigSpec() {
    val host by optional("0.0.0.0")
    val tcpPort by required<Int>()
}

fun main(args: Array<String>) {
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
    val config = Config { addSpec(ServerSpec) }
        .from.yaml.file("server.yml")
        .from.json.resource("server.json")
        .from.env()
        .from.systemProperties()
    run {
        val config = Config { addSpec(ServerSpec) }.withSource(
            Source.from.yaml.file("server.yml") +
                    Source.from.json.resource("server.json") +
                    Source.from.env() +
                    Source.from.systemProperties()
        )
    }
    run {
        val config = Config { addSpec(ServerSpec) }
            .from.yaml.watchFile("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
    }
    val server = Server(config[ServerSpec.host], config[ServerSpec.tcpPort])
    server.start()
    run {
        val server = Config()
            .from.yaml.file("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
            .at("server")
            .toValue<Server>()
        server.start()
    }
    run {
        val server = (
                Source.from.yaml.file("server.yml") +
                        Source.from.json.resource("server.json") +
                        Source.from.env() +
                        Source.from.systemProperties()
                )["server"]
            .toValue<Server>()
        server.start()
    }
}
