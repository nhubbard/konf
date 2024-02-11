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

package io.github.nhubbard.konf.snippet

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.base.toFlatMap
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import io.github.nhubbard.konf.source.json.toJson
import io.github.nhubbard.konf.tempFile

fun main() {
    val config = Config { addSpec(Server) }
    config[Server.tcpPort] = 1000
    var map: Map<String, Any>
    run {
        map = config.toMap()
    }
    run {
        map = config.toHierarchicalMap()
    }
    run {
        map = config.toFlatMap()
    }
    val file = tempFile(suffix = ".json")
    config.toJson.toFile(file)
    val newConfig = Config {
        addSpec(Server)
    }.from.json.file(file)
    check(config.toMap() == newConfig.toMap())
}
