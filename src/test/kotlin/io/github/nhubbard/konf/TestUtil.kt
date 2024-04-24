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

/**
 * Creates a temporary file with the given content.
 *
 * @param content The content to be written to the temporary file.
 * @param prefix The prefix to be used in the name of the temporary file. The default value is "tmp".
 * @param suffix The suffix to be used in the name of the temporary file. The default value is ".tmp".
 * @return A [File] object representing the created temporary file.
 */
fun tempFileOf(content: String, prefix: String = "tmp", suffix: String = ".tmp"): File {
    return tempFile(prefix, suffix).apply {
        writeText(content)
    }
}

/**
 * Asserts that a specific checked exception of type [T] is thrown when executing the provided [block].
 *
 * @param T The type of the checked exception that is expected to be thrown.
 * @param block The block of code to be executed.
 * @return Returns the thrown exception of type [T].
 * @throws AssertionError If no exception is thrown or if the thrown exception is not of type [T].
 */
inline fun <reified T : Throwable?> assertCheckedThrows(block: () -> Unit): T {
    val result = runCatching { block() }
    if (result.isSuccess) fail<Nothing> { "No exception was thrown!" }
    val e = result.exceptionOrNull()
    if (e == null || e !is T) fail<Nothing> { "The requested exception ${T::class.java.simpleName} was not thrown!" }
    return e as T
}

/**
 * Returns a stream of Arguments from the given array of Arguments.
 *
 * @param arguments the array of Arguments to be converted into a Stream
 * @return a stream of Arguments
 */
fun argumentsOf(vararg arguments: Arguments): Stream<Arguments> =
    Stream.of(*arguments)

/**
 * Creates a stream of arguments, each containing a single argument.
 *
 * @param arguments the arguments to be included in the stream
 * @return a stream of arguments, each containing a single argument
 */
fun <A> singleArgumentsOf(vararg arguments: A): Stream<Arguments> =
    Stream.of(*arguments.map { Arguments.of(it) }.toTypedArray())

/**
 * Creates an instance of [Arguments] by combining two input arguments.
 *
 * @param left The left input argument.
 * @param right The right input argument.
 * @return An instance of [Arguments] that contains the combined input arguments.
 */
fun <A, B> twoArgumentsOf(left: A, right: B): Arguments =
    Arguments.of(left, right)

/**
 * Creates a configuration specification for the given prefix and provider. Used by [TestCompleteConfigSpec].
 *
 * @param prefix The prefix used in the configuration specification. The default value is "network.buffer".
 * @param provider The provider function that returns the configuration.
 *
 * @return A configuration specification for the given prefix and provider.
 */
fun configSpecOf(prefix: String = "network.buffer", provider: () -> Config) =
    twoArgumentsOf(prefix, provider)

/**
 * Creates an instance of [Arguments] with three arguments.
 *
 * @param first the first argument of type [A]
 * @param second the second argument of type [B]
 * @param third the third argument of type [C]
 * @return an instance of [Arguments] with the three specified arguments
 */
fun <A, B, C> threeArgumentsOf(first: A, second: B, third: C): Arguments =
    Arguments.of(first, second, third)