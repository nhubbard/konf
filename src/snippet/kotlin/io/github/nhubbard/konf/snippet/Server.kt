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

@file:Suppress("unused", "EmptyMethod")

package io.github.nhubbard.konf.snippet

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec

data class Server(val host: String, val tcpPort: Int) {
    constructor(config: Config) : this(config[Companion.host], config[Companion.tcpPort])

    fun start() {}

    companion object : ConfigSpec("server") {
        val host by optional("0.0.0.0", description = "host IP of server")
        val tcpPort by required<Int>(description = "port of server")
        val nextPort by lazy { config -> config[tcpPort] + 1 }
    }
}
