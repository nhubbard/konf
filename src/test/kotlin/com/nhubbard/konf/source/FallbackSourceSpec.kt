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

package com.nhubbard.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhubbard.konf.name
import com.nhubbard.konf.source.base.asKVSource
import com.nhubbard.konf.toPath
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object FallbackSourceSpec : Spek({
    given("a source with fallback") {
        it("contains facade & fallback info") {
            val facadeSource = 1.asSource()
            val fallbackSource = 2.asSource()
            val source = facadeSource.withFallback(fallbackSource)
            assertThat(source.info["facade"], equalTo(facadeSource.description))
            assertThat(source.info["fallback"], equalTo(fallbackSource.description))
        }
        on("path/key is in facade source") {
            val path = listOf("a", "b")
            val key = path.name
            val fallbackSource = mapOf(key to "fallback").asKVSource()
            val facadeSource = mapOf(key to "facade").asKVSource()
            val source = facadeSource.withFallback(fallbackSource)
            it("gets value from facade source") {
                assertTrue(path in source)
                assertTrue(key in source)
                assertThat(source[path].asValue<String>(), equalTo(facadeSource[path].asValue<String>()))
                assertThat(source[key].asValue<String>(), equalTo(facadeSource[key].asValue<String>()))
                assertThat(
                    source.getOrNull(path)?.asValue<String>(),
                    equalTo(facadeSource.getOrNull(path)?.asValue<String>())
                )
                assertThat(
                    source.getOrNull(key)?.asValue<String>(),
                    equalTo(facadeSource.getOrNull(key)?.asValue<String>())
                )
            }
        }
        on("path/key is in fallback source") {
            val path = listOf("a", "b")
            val key = path.name
            val fallbackSource = mapOf(key to "fallback").asKVSource()
            val facadePath = listOf("a", "c")
            val facadeKey = facadePath.name
            val facadeSource = mapOf(facadeKey to "facade").asKVSource()
            val source = facadeSource.withFallback(fallbackSource)
            it("gets value from fallback source") {
                assertTrue(path in source)
                assertTrue(key in source)
                assertThat(source[path].asValue<String>(), equalTo(fallbackSource[path].asValue<String>()))
                assertThat(source[key].asValue<String>(), equalTo(fallbackSource[key].asValue<String>()))
                assertThat(
                    source.getOrNull(path)?.asValue<String>(),
                    equalTo(fallbackSource.getOrNull(path)?.asValue<String>())
                )
                assertThat(
                    source.getOrNull(key)?.asValue<String>(),
                    equalTo(fallbackSource.getOrNull(key)?.asValue<String>())
                )
            }
            it("contains value in facade source") {
                assertTrue(facadePath in source)
                assertTrue(facadeKey in source)
            }
            it("does not contain value which is not existed in both facade source and fallback source") {
                assertFalse("a.d".toPath() in source)
                assertFalse("a.d" in source)
            }
        }
    }
})
