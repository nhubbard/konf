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

import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.throws
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec

object DefaultLoadersConfig : ConfigSpec("source.test") {
    val type by required<String>()
}

fun Source.toConfig(): Config = Config {
    addSpec(DefaultLoadersConfig)
}.withSource(this)

inline fun <reified T : Any> assertCausedBy(noinline block: () -> Unit) {
    @Suppress("UNCHECKED_CAST")
    assertThat(
        block,
        throws(
            has(
                LoadException::cause,
                isA<T>() as Matcher<Throwable?>
            )
        )
    )
}

const val propertiesContent = "source.test.type = properties"
