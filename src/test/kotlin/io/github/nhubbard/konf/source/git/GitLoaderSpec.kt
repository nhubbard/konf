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

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.Loader
import io.github.nhubbard.konf.source.helpers.Sequential
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import io.github.nhubbard.konf.tempDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object GitLoaderSpec : SubjectSpek<Loader>({
    val parentConfig = Config {
        addSpec(GitTestSourceType)
    }
    subject {
        Loader(parentConfig, PropertiesProvider)
    }

    given("a loader") {
        on("load from git repository") {
            tempDirectory().let { dir ->
                Git.init().apply {
                    setDirectory(dir)
                }.call().use { git ->
                    Paths.get(dir.path, "test").toFile().writeText("type = git")
                    git.add().apply {
                        addFilepattern("test")
                    }.call()
                    git.commit().apply {
                        message = "init commit"
                    }.call()
                }
                val repo = dir.toURI()
                val config = subject.git(repo.toString(), "test")
                it("should return a config which contains value in git repository") {
                    assertThat(config[GitTestSourceType.type], equalTo("git"))
                }
            }
        }
        mapOf(
            "load from watched git repository" to { loader: Loader, repo: String ->
                loader.watchGit(
                    repo,
                    "test",
                    period = 1,
                    unit = TimeUnit.SECONDS,
                    context = Dispatchers.Sequential
                )
            },
            "load from watched git repository to the given directory" to { loader: Loader, repo: String ->
                loader.watchGit(
                    repo,
                    "test",
                    dir = tempDirectory(prefix = "local_git_repo").path,
                    branch = Constants.HEAD,
                    unit = TimeUnit.SECONDS,
                    context = Dispatchers.Sequential,
                    optional = false
                )
            }
        ).forEach { (description, func) ->
            on(description) {
                tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
                    val file = Paths.get(dir.path, "test").toFile()
                    Git.init().apply {
                        setDirectory(dir)
                    }.call().use { git ->
                        file.writeText("type = originalValue")
                        git.add().apply {
                            addFilepattern("test")
                        }.call()
                        git.commit().apply {
                            message = "init commit"
                        }.call()
                    }
                    val repo = dir.toURI()
                    val config = func(subject, repo.toString())
                    val originalValue = config[GitTestSourceType.type]
                    file.writeText("type = newValue")
                    Git.open(dir).use { git ->
                        git.add().apply {
                            addFilepattern("test")
                        }.call()
                        git.commit().apply {
                            message = "update value"
                        }.call()
                    }
                    runBlocking(Dispatchers.Sequential) {
                        delay(TimeUnit.SECONDS.toMillis(1))
                    }
                    val newValue = config[GitTestSourceType.type]
                    it("should return a config which contains value in git repository") {
                        assertThat(originalValue, equalTo("originalValue"))
                    }
                    it("should load new value when content of git repository has been changed") {
                        assertThat(newValue, equalTo("newValue"))
                    }
                }
            }
        }
        on("load from watched git repository with listener") {
            tempDirectory(prefix = "remote_git_repo", suffix = ".git").let { dir ->
                val file = Paths.get(dir.path, "test").toFile()
                Git.init().apply {
                    setDirectory(dir)
                }.call().use { git ->
                    file.writeText("type = originalValue")
                    git.add().apply {
                        addFilepattern("test")
                    }.call()
                    git.commit().apply {
                        message = "init commit"
                    }.call()
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
                Git.open(dir).use { git ->
                    git.add().apply {
                        addFilepattern("test")
                    }.call()
                    git.commit().apply {
                        message = "update value"
                    }.call()
                }
                runBlocking(Dispatchers.Sequential) {
                    delay(TimeUnit.SECONDS.toMillis(1))
                }
                it("should return a config which contains value in git repository") {
                    assertThat(originalValue, equalTo("originalValue"))
                }
                it("should load new value when content of git repository has been changed") {
                    assertThat(newValue, equalTo("newValue"))
                }
            }
        }
    }
})