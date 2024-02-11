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

package com.nhubbard.konf.source.base

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.sameInstance
import com.natpryce.hamkrest.throws
import com.nhubbard.konf.source.NoSuchPathException
import com.nhubbard.konf.source.asSource
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object ValueSourceSpec : Spek({
    given("a value source") {
        on("get with non-empty path") {
            it("should throw NoSuchPathException") {
                assertThat({ 1.asSource()["a"] }, throws<NoSuchPathException>())
            }
        }
        on("invoke `asSource`") {
            val source = 1.asSource()
            it("should return itself") {
                assertThat(source.asSource(), sameInstance(source))
            }
        }
    }
})
