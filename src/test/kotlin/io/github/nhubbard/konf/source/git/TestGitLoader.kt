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

import io.github.nhubbard.konf.*
import io.github.nhubbard.konf.source.Loader
import io.github.nhubbard.konf.source.git.helpers.GitTestSourceType
import io.github.nhubbard.konf.source.helpers.Sequential
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.lib.Constants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestGitLoader {
    private val parentConfig = Config {
        addSpec(GitTestSourceType)
    }
    private val provider = {
        Loader(parentConfig, PropertiesProvider)
    }

    @Test
    fun testGitLoader_onLoadFromGitRepository_itShouldReturnConfigWithValueFromGitRepo() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "test").toFile().writeText("type = git")
                add("test")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.git(repo.toString(), "test")
            assertEquals("git", config[GitTestSourceType.type])
        }
    }

    @Test
    fun testGitLoader_onLoadFromWatchedGitRepository_itShouldReturnConfigWithValueFromGitRepo() {
        val subject = provider()
        tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
            val file = Paths.get(dir.path, "test").toFile()
            gitInit(dir) {
                file.writeText("type = originalValue")
                add("test")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.watchGit(
                repo.toString(),
                "test",
                period = 1,
                unit = TimeUnit.SECONDS,
                context = Dispatchers.Sequential
            )
            val originalValue = config[GitTestSourceType.type]
            file.writeText("type = newValue")
            gitOpen(dir) {
                add("test")
                commit("update value")
            }
            runBlocking(Dispatchers.Sequential) {
                delay(TimeUnit.SECONDS.toMillis(1))
            }
            val newValue = config[GitTestSourceType.type]
            assertEquals("originalValue", originalValue)
            assertEquals("newValue", newValue)
        }
    }

    @Test
    fun testGitLoader_onLoadFromWatchedGitRepositoryToTheGivenDirectory_itShouldReturnAConfigWhichChangesValueInGitRepository() {
        val subject = provider()
        tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
            val file = Paths.get(dir.path, "test").toFile()
            gitInit(dir) {
                file.writeText("type = originalValue")
                add("test")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.watchGit(
                repo.toString(),
                "test",
                dir = tempDirectory(prefix = "local_git_repo").path,
                branch = Constants.HEAD,
                unit = TimeUnit.SECONDS,
                context = Dispatchers.Sequential,
                optional = false
            )
            val originalValue = config[GitTestSourceType.type]
            file.writeText("type = newValue")
            gitOpen(dir) {
                add("test")
                commit("update value")
            }
            runBlocking(Dispatchers.Sequential) {
                delay(TimeUnit.SECONDS.toMillis(1))
            }
            val newValue = config[GitTestSourceType.type]
            assertEquals("originalValue", originalValue)
            assertEquals("newValue", newValue)
        }
    }

    @Test
    fun testGitLoader_onLoadFromWatchedGitRepositoryWithListener_itShouldReturnAConfigWhichChangesValueInGitRepository() {
        val subject = provider()
        tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
            val file = Paths.get(dir.path, "test").toFile()
            gitInit(dir) {
                file.writeText("type = originalValue")
                add("test")
                commit("init commit")
            }
            val repo = dir.toURI()
            var newValue = ""
            val config = subject.watchGit(
                repo.toString(),
                "test",
                period = 1,
                unit = TimeUnit.SECONDS,
                context = Dispatchers.Sequential
            ) { config, _ ->
                newValue = config[GitTestSourceType.type]
            }
            val originalValue = config[GitTestSourceType.type]
            file.writeText("type = newValue")
            gitOpen(dir) {
                add("test")
                commit("update value")
            }
            runBlocking(Dispatchers.Sequential) {
                delay(TimeUnit.SECONDS.toMillis(1))
            }
            assertEquals("originalValue", originalValue)
            assertEquals("newValue", newValue)
        }
    }
}