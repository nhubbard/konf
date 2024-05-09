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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestSourceInfo {
    companion object {
        @JvmStatic val provider = { SourceInfo("a" to "1") }
    }

    @Test
    fun testSourceInfo_onUseAsMap_itShouldBehaveLikeAMap() {
        val subject = provider()
        assertEquals(mapOf("a" to "1"), subject.toMap())
    }

    @Test
    fun testSourceInfo_withNewKVPairs_itShouldContainThem() {
        val subject = provider()
        assertEquals(mapOf("a" to "1", "b" to "2", "c" to "3"), subject.with("b" to "2", "c" to "3").toMap())
    }

    @Test
    fun testSourceInfo_withOtherSourceInfo_itShouldContainTheNewKVPairsFromOtherInstance() {
        val subject = provider()
        assertEquals(
            mapOf("a" to "1", "b" to "2", "c" to "3"),
            subject.with(SourceInfo("b" to "2", "c" to "3")).toMap()
        )
    }
}