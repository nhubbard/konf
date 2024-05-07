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

package io.github.nhubbard.konf.source.env

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.singleArgumentsOf
import io.github.nhubbard.konf.source.env.helpers.FlattenSourceSpec
import io.github.nhubbard.konf.source.env.helpers.SourceSpec
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEnvProvider {
    @ParameterizedTest
    @MethodSource("providerSource")
    fun testEnvProvider_onCreateSourceFromSystemEnv_itShouldHaveCorrectType(provider: () -> EnvProvider) {
        val subject = provider()
        val source = subject.env()
        assertEquals("system-environment", source.info["type"])
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testEnvProvider_onCreateSourceFromSystemEnv_itShouldReturnSourceWithValueFromSystemEnv(provider: () -> EnvProvider) {
        val subject = provider()
        val source = subject.env()
        val config = Config { addSpec(SourceSpec) }.withSource(source)
        assertEquals("env", config[SourceSpec.Test.type])
        assertTrue(config[SourceSpec.camelCase])
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testEnvProvider_onCreateSourceFromSystemEnv_itShouldReturnCaseInsensitiveSource(provider: () -> EnvProvider) {
        val subject = provider()
        val source = subject.env()
        val config = Config().withSource(source).apply { addSpec(SourceSpec) }
        assertEquals("env", config[SourceSpec.Test.type])
        assertTrue(config[SourceSpec.camelCase])
    }

    @ParameterizedTest
    @MethodSource("providerSource")
    fun testEnvProvider_onCreateFlattenSourceFromSystemEnv_itShouldReturnSourceWithValueFromSystemEnv(provider: () -> EnvProvider) {
        val subject = provider()
        val source = subject.env(nested = false)
        val config = Config { addSpec(FlattenSourceSpec) }.withSource(source)
        assertEquals("env", config[FlattenSourceSpec.SOURCE_TEST_TYPE])
        assertTrue(config[FlattenSourceSpec.SOURCE_CAMELCASE])
    }

    companion object {
        @JvmStatic
        fun providerSource(): Stream<Arguments> = singleArgumentsOf(
            { EnvProvider },
            { EnvProvider.get() }
        )
    }
}