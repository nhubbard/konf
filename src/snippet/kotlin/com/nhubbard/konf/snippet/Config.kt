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

@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")

package com.nhubbard.konf.snippet

import com.nhubbard.konf.Config
import com.nhubbard.konf.ConfigSpec

fun main() {
    val config = Config()
    config.addSpec(Server)
    var host: String
    run {
        host = config[Server.host]
    }
    run {
        host = config["server.host"]
    }
    run {
        host = config("server.host")
    }
    config.contains(Server.host)
    // or
    Server.host in config
    config.contains("server.host")
    // or
    "server.host" in config
    config[Server.tcpPort] = 80
    config["server.tcpPort"] = 80
    config.containsRequired()
    config.validateRequired()
    config.unset(Server.tcpPort)
    config.unset("server.tcpPort")
    val basePort by ConfigSpec("server").required<Int>()
    config.lazySet(Server.tcpPort) { it[basePort] + 1 }
    config.lazySet("server.tcpPort") { it[basePort] + 1 }
    run {
        val handler = Server.host.onSet { value -> println("the host has changed to $value") }
        handler.cancel()
    }
    run {
        val handler = Server.host.beforeSet { _, value -> println("the host will change to $value") }
        handler.cancel()
    }
    run {
        val handler = config.beforeSet { item, value -> println("${item.name} will change to $value") }
        handler.cancel()
    }
    run {
        val handler = Server.host.afterSet { _, value -> println("the host has changed to $value") }
        handler.cancel()
    }
    run {
        val handler = config.afterSet { item, value -> println("${item.name} has changed to $value") }
        handler.cancel()
    }
    run {
        var port by config.property(Server.tcpPort)
        port = 9090
        check(port == 9090)
    }
    run {
        val port by config.property(Server.tcpPort)
        check(port == 9090)
    }
}
