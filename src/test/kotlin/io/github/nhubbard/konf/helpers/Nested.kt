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

@file:Suppress("unused")

package io.github.nhubbard.konf.helpers

import io.github.nhubbard.konf.ConfigSpec

object Nested : ConfigSpec("a.bb") {
    val item by required<Int>("int", "description")

    object Inner : ConfigSpec() {
        val item by required<Int>()
    }

    object Inner2 : ConfigSpec("inner2.level2") {
        val item by required<Int>()
    }

    object Inner3a : ConfigSpec("inner3.a") {
        val item by required<Int>()
    }

    object Inner3b : ConfigSpec("inner3.b") {
        val item by required<Int>()
    }
}