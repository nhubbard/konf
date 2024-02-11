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
import com.natpryce.hamkrest.throws
import io.github.nhubbard.konf.source.InvalidRemoteRepoException
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.asValue
import io.github.nhubbard.konf.source.properties.PropertiesProvider
import io.github.nhubbard.konf.tempDirectory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import java.nio.file.Paths
import kotlin.test.assertTrue

object GitProviderSpec : SubjectSpek<Provider>({
    subject { PropertiesProvider }

    given("a provider") {
        on("create source from git repository") {
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
                val source = subject.git(repo.toString(), "test")
                it("should create from the specified git repository") {
                    assertThat(source.info["repo"], equalTo(repo.toString()))
                    assertThat(source.info["file"], equalTo("test"))
                    assertThat(source.info["branch"], equalTo(Constants.HEAD))
                }
                it("should return a source which contains value in git repository") {
                    assertThat(source["type"].asValue<String>(), equalTo("git"))
                }
            }
        }
        on("create source from invalid git repository") {
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
                it("should throw InvalidRemoteRepoException") {
                    assertThat(
                        { subject.git(tempDirectory().path, "test", dir = dir.path) },
                        throws<InvalidRemoteRepoException>()
                    )
                }
                it("should return an empty source if optional") {
                    assertTrue {
                        subject.git(tempDirectory().path, "test", dir = dir.path, optional = true).tree.children.isEmpty()
                    }
                }
            }
        }
    }
})
