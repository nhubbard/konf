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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.FileNotFoundException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestOptionalSourceByDefault {
    @Test
    fun testAConfig_whenTheFeatureIsDisabled_shouldThrowExceptionWhenFileDoesNotExist() {
        val config = Config().disable(Feature.OPTIONAL_SOURCE_BY_DEFAULT)
        assertThrows<FileNotFoundException> { config.from.file("not_existed.json") }
    }

    @Test
    fun testAConfig_whenTheFeatureIsEnabled_shouldLoadEmptySource() {
        val config = Config().enable(Feature.OPTIONAL_SOURCE_BY_DEFAULT)
        config.from.mapped {
            assertEquals(it.tree.children, mutableMapOf<String, TreeNode>())
            it
        }.file("not_existed.json")
        config.from.mapped {
            assertEquals(it.tree.children, mutableMapOf<String, TreeNode>())
            it
        }.json.file("not_existed.json")
    }
}