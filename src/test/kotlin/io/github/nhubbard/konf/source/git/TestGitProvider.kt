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
import io.github.nhubbard.konf.source.InvalidRemoteRepoException
import io.github.nhubbard.konf.source.asValue
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import io.github.nhubbard.konf.tempDirectory
import org.eclipse.jgit.lib.Constants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestGitProvider {
    private val provider = { PropertiesProvider }

    @Test
    fun testGitProvider_onCreateSourceFromGitRepository_itShouldCreateFromTheSpecifiedGitRepository() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "test").toFile().writeText("type = git")
                add("test")
                commit("Init commit")
            }
            val repo = dir.toURI()
            val source = subject.git(repo.toString(), "test")
            assertEquals(repo.toString(), source.info["repo"])
            assertEquals("test", source.info["file"])
            assertEquals(Constants.HEAD, source.info["branch"])
        }
    }

    @Test
    fun testGitProvider_onCreateSourceFromGitRepository_itShouldReturnSourceWithValueInGitRepository() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "test").toFile().writeText("type = git")
                add("test")
                commit("Init commit")
            }
            val repo = dir.toURI()
            val source = subject.git(repo.toString(), "test")
            assertEquals("git", source["type"].asValue<String>())
        }
    }

    @Test
    fun testGitProvider_onCreateSourceFromInvalidGitRepository_itShouldThrowInvalidRemoteRepoException() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "test").toFile().writeText("type = git")
                add("test")
                commit("init commit")
            }
            assertThrows<InvalidRemoteRepoException> {
                subject.git(tempDirectory().path, "test", dir = dir.path)
            }
        }
    }

    @Test
    fun testGitProvider_onCreateSourceFromInvalidGitRepository_itShouldReturnEmptySourceIfOptional() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "test").toFile().writeText("type = git")
                add("test")
                commit("init commit")
            }
            assertTrue(subject.git(tempDirectory().path, "test", dir = dir.path, optional = true).tree.children.isEmpty())
        }
    }
}