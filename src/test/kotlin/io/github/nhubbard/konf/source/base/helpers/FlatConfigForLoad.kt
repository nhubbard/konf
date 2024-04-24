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

package io.github.nhubbard.konf.source.base.helpers

import io.github.nhubbard.konf.ConfigSpec

object FlatConfigForLoad : ConfigSpec("level1.level2") {
    val emptyList by required<List<Int>>()
    val emptySet by required<Set<Int>>()
    val emptyArray by required<IntArray>()
    val emptyObjectArray by required<Array<Int>>()
    val singleElementList by required<List<Int>>()
    val multipleElementsList by required<List<Int>>()
    val flatClass by required<ClassForLoad>()
}

