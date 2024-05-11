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

package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.source.NoSuchPathException
import io.github.nhubbard.konf.source.asSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestValueSource {
    @Test
    fun testValueSource_onGetWithNonEmptyPath_itShouldThrowNoSuchPathException() {
        assertFailsWith<NoSuchPathException> { 1.asSource()["a"] }
    }

    @Test
    fun testValueSource_onInvokeAsSource_itShouldReturnItself() {
        val source = 1.asSource()
        assertSame(source, source.asSource())
    }
}