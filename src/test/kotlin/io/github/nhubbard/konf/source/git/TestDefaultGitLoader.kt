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
import io.github.nhubbard.konf.source.helpers.DefaultLoadersConfig
import io.github.nhubbard.konf.source.helpers.Sequential
import io.github.nhubbard.konf.source.helpers.propertiesContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.lib.Constants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestDefaultGitLoader {
    private val provider = {
        Config {
            addSpec(DefaultLoadersConfig)
        }.from
    }
    private val item = DefaultLoadersConfig.type

    @Test
    fun testGitLoader_onLoadFromGitRepository_itShouldLoadAsAutoDetectedFileFormat() {
        val subject = provider()
        tempDirectory().let { dir ->
            gitInit(dir) {
                Paths.get(dir.path, "source.properties").toFile().writeText(propertiesContent)
                add("source.properties")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.git(repo.toString(), "source.properties")
            assertEquals("properties", config[item])
        }
    }

    @Test
    fun testGitLoader_onLoadFromWatchedGitRepository_itShouldLoadAutomaticallyAndWatchFile() {
        val subject = provider()
        tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
            val file = Paths.get(dir.path, "source.properties").toFile()
            gitInit(dir) {
                file.writeText(propertiesContent)
                add("source.properties")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.watchGit(
                repo.toString(),
                "source.properties",
                period = 1,
                unit = TimeUnit.SECONDS,
                context = Dispatchers.Sequential
            )
            val originalValue = config[item]
            file.writeText(propertiesContent.replace("properties", "newValue"))
            gitOpen(dir) {
                add("source.properties")
                commit("update value")
            }
            runBlocking(Dispatchers.Sequential) {
                delay(TimeUnit.SECONDS.toMillis(1))
            }
            val newValue = config[item]
            assertEquals("properties", originalValue)
            assertEquals("newValue", newValue)
        }
    }

    @Test
    fun testGitLoader_onLoadFromWatchedGitRepositoryInGivenDirectory_itShouldLoadAutomaticallyAndWatchFile() {
        val subject = provider()
        tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
            val file = Paths.get(dir.path, "source.properties").toFile()
            gitInit(dir) {
                file.writeText(propertiesContent)
                add("source.properties")
                commit("init commit")
            }
            val repo = dir.toURI()
            val config = subject.watchGit(
                repo.toString(),
                "source.properties",
                dir = tempDirectory(prefix = "local_git_repo").path,
                branch = Constants.HEAD,
                unit = TimeUnit.SECONDS,
                context = Dispatchers.Sequential,
                optional = false
            )
            val originalValue = config[item]
            file.writeText(propertiesContent.replace("properties", "newValue"))
            gitOpen(dir) {
                add("source.properties")
                commit("update value")
            }
            runBlocking(Dispatchers.Sequential) {
                delay(TimeUnit.SECONDS.toMillis(1))
            }
            val newValue = config[item]
            assertEquals("properties", originalValue)
            assertEquals("newValue", newValue)
        }
    }

    @Test
    fun testGitLoader_onLoadFromWatchedGitRepoWithListener_itShouldLoadAndReloadWithNewValue() {
        val subject = provider()
        tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
            val file = Paths.get(dir.path, "source.properties").toFile()
            gitInit(dir) {
                file.writeText(propertiesContent)
                add("source.properties")
                commit("init commit")
            }
            val repo = dir.toURI()
            var newValue = ""
            val config = subject.watchGit(
                repo.toString(),
                "source.properties",
                period = 1,
                unit = TimeUnit.SECONDS,
                context = Dispatchers.Sequential
            ) { config, _ ->
                newValue = config[item]
            }
            val originalValue = config[item]
            file.writeText(propertiesContent.replace("properties", "newValue"))
            gitOpen(dir) {
                add("source.properties")
                commit("update value")
            }
            runBlocking(Dispatchers.Sequential) {
                delay(TimeUnit.SECONDS.toMillis(1))
            }
            assertEquals("properties", originalValue)
            assertEquals("newValue", newValue)
        }
    }
}