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

package io.github.nhubbard.konf.source.git

import io.github.nhubbard.konf.add
import io.github.nhubbard.konf.commit
import io.github.nhubbard.konf.gitInit
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.helpers.DefaultLoadersConfig
import io.github.nhubbard.konf.source.helpers.propertiesContent
import io.github.nhubbard.konf.source.helpers.toConfig
import io.github.nhubbard.konf.tempDirectory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestDefaultGitProvider {
    private val provider = { Source.from }
    private val item = DefaultLoadersConfig.type

    @Test
    fun testGitProvider_onProviderSourceFromGitRepository_itShouldProvideAsAutoDetectedFileFormat() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "source.properties").toFile().writeText(propertiesContent)
                add("source.properties")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.git(repo.toString(), "source.properties").toConfig()
            assertEquals("properties", config[item])
        }
    }
}