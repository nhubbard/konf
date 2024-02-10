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
import com.nhubbard.konfig.source.base.toFlatMap
import com.nhubbard.konfig.source.base.toHierarchicalMap
import com.nhubbard.konfig.source.json.toJson
import com.nhubbard.konfig.tempFile

fun main(args: Array<String>) {
    val config = Config { addSpec(Server) }
    config[Server.tcpPort] = 1000
    run {
        val map = config.toMap()
    }
    run {
        val map = config.toHierarchicalMap()
    }
    run {
        val map = config.toFlatMap()
    }
    val file = tempFile(suffix = ".json")
    config.toJson.toFile(file)
    val newConfig = Config {
        addSpec(Server)
    }.from.json.file(file)
    check(config.toMap() == newConfig.toMap())
}
