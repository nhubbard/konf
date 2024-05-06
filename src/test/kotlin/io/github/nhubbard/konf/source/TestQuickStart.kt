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

package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.snippet.Server
import io.github.nhubbard.konf.snippet.ServerSpec
import io.github.nhubbard.konf.source.helpers.useFile
import io.github.nhubbard.konf.source.yaml.yaml
import io.github.nhubbard.konf.toValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestQuickStart {
    @Test
    fun testQuickStart_onUseDefaultLoaders_itShouldLoadAllValues() {
        val config = useFile {
            Config { addSpec(ServerSpec) }
                .from.yaml.file("server.yml")
                .from.json.resource("server.json")
                .from.env()
                .from.systemProperties()
        }
        assertEquals(config.toMap(), mapOf(
            "server.host" to "127.0.0.1",
            "server.tcpPort" to 8080
        ))
    }

    @Test
    fun testQuickStart_onUseDefaultProviders_itShouldLoadAllValues() {
        val config = useFile {
            Config { addSpec(ServerSpec) }.withSource(
                Source.from.yaml.file("server.yml") +
                        Source.from.json.resource("server.json") +
                        Source.from.env() +
                        Source.from.systemProperties()
            )
        }
        assertEquals(config.toMap(), mapOf(
            "server.host" to "127.0.0.1",
            "server.tcpPort" to 8080
        ))
    }

    @Test
    fun testQuickStart_onCastConfigToValue_itShouldLoadAllValues() {
        val config = useFile {
            Config()
                .from.yaml.file("server.yml")
                .from.json.resource("server.json")
                .from.env()
                .from.systemProperties()
                .at("server")
        }
        val server = config.toValue<Server>()
        assertEquals(server, Server(host = "127.0.0.1", tcpPort = 8080))
    }

    @Test
    fun testQuickStart_onCastSourceToValue_itShouldLoadAllValues() {
        val source = useFile {
            (
                Source.from.yaml.file("server.yml") +
                    Source.from.json.resource("server.json") +
                    Source.from.env() +
                    Source.from.systemProperties()
            )["server"]
        }
        val server = source.toValue<Server>()
        assertEquals(server, Server(host = "127.0.0.1", tcpPort = 8080))
    }
}