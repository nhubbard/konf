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

package io.github.nhubbard.konf

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.params.provider.Arguments
import java.io.File
import java.util.stream.Stream

fun tempFileOf(content: String, prefix: String = "tmp", suffix: String = ".tmp"): File {
    return tempFile(prefix, suffix).apply {
        writeText(content)
    }
}

inline fun <reified T : Throwable?> assertCheckedThrows(block: () -> Unit): T {
    val result = runCatching { block() }
    if (result.isSuccess) fail<Nothing> { "No exception was thrown!" }
    val e = result.exceptionOrNull()
    if (e == null || e !is T) fail<Nothing> { "The requested exception ${T::class.java.simpleName} was not thrown!" }
    return e as T
}

fun argumentsOf(vararg arguments: Arguments): Stream<Arguments> =
    Stream.of(*arguments)

fun <A, B> twoArgsOf(left: A, right: B): Arguments =
    Arguments.of(left, right)

fun configSpecOf(prefix: String = "network.buffer", provider: () -> Config) =
    twoArgsOf(prefix, provider)