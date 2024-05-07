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

package io.github.nhubbard.konf.source.hocon

import io.github.nhubbard.konf.source.asSource
import io.github.nhubbard.konf.source.asValue
import io.github.nhubbard.konf.toPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestHoconSource {
    private val provider = { HoconProvider.string("key = 1") as HoconSource }

    @Test
    fun testHoconSource_onGetUnderlyingConfig_itShouldReturnCorrespondingConfig() {
        val subject = provider()
        val config = subject.value
        assertEquals(1, config.getInt("key"))
    }

    @Test
    fun testHoconSource_onGetExistingKey_itShouldContainTheKey() {
        val subject = provider()
        assertTrue("key".toPath() in subject)
    }

    @Test
    fun testHoconSource_onGetExistingKey_itShouldContainTheCorrespondingValue() {
        val subject = provider()
        assertEquals(1, subject["key".toPath()].asValue<Int>())
    }

    @Test
    fun testHoconSource_onGetNonExistentKey_itShouldNotContainTheKey() {
        val subject = provider()
        assertTrue("invalid".toPath() !in subject)
    }

    @Test
    fun testHoconSource_onGetNonExistentKey_itShouldNotContainTheCorrespondingValue() {
        val subject = provider()
        assertNull(subject.getOrNull("invalid".toPath()))
    }

    @Test
    fun testHoconSource_onUseSubstitutionsInSource_itShouldResolveTheKey() {
        val source = HoconProvider.string(
            """
            key1 = 1
            key2 = ${'$'}{key1}
            """.trimIndent()
        )
        assertEquals(1, source["key2"].asValue<Int>())
    }

    @Test
    fun testHoconSource_onUseSubstitutionsInSourceWhenVariablesAreInOtherSources_itShouldResolveTheKey() {
        val source = (
            HoconProvider.string(
                """
            key1 = "1"
            key2 = ${'$'}{key1}
            key3 = "${'$'}{key4}"
            key5 = "${'$'}{key1}+${'$'}{key4}"
            key6 = "${"$$"}{key1}"
                """.trimIndent()
            ) + mapOf("key4" to "4", "key1" to "2").asSource()
        ).substituted().substituted()
        assertEquals(1, source["key2"].asValue<Int>())
        assertEquals(4, source["key3"].asValue<Int>())
        assertEquals("2+4", source["key5"].asValue<String>())
        assertEquals("\${key1}", source["key6"].asValue<String>())
    }
}