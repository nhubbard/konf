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

import io.github.nhubbard.konf.source.InvalidRemoteRepoException
import io.github.nhubbard.konf.source.Provider
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.SourceException
import io.github.nhubbard.konf.source.base.EmptyMapSource
import io.github.nhubbard.konf.tempDirectory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.transport.URIish
import java.io.File
import java.io.IOException
import java.nio.file.Paths

/**
 * Returns a new source from a specified git repository.
 *
 * @param repo git repository
 * @param file file in the git repository
 * @param dir local directory of the git repository
 * @param branch the initial branch
 * @param optional whether this source is optional
 * @return a new source from a specified git repository
 */
fun Provider.git(
    repo: String,
    file: String,
    dir: String? = null,
    branch: String = Constants.HEAD,
    optional: Boolean = false
): Source {
    return (dir?.let(::File) ?: tempDirectory(prefix = "local_git_repo")).let { directory ->
        val extendContext: Source.() -> Unit = {
            info["repo"] = repo
            info["file"] = file
            info["dir"] = directory.path
            info["branch"] = branch
        }
        try {
            if ((directory.list { _, name -> name == ".git" } ?: emptyArray()).isEmpty()) {
                Git.cloneRepository().apply {
                    setURI(repo)
                    setDirectory(directory)
                    setBranch(branch)
                }.call().close()
            } else {
                Git.open(directory).use { git ->
                    val uri = URIish(repo)
                    val remoteName = git.remoteList().call().firstOrNull { it.urIs.contains(uri) }?.name
                        ?: throw InvalidRemoteRepoException(repo, directory.path)
                    git.pull().apply {
                        remote = remoteName
                        remoteBranchName = branch
                    }.call()
                }
            }
        } catch (ex: Exception) {
            when (ex) {
                is GitAPIException, is IOException, is SourceException -> {
                    if (optional) {
                        return EmptyMapSource().apply(extendContext)
                    } else {
                        throw ex
                    }
                }
                else -> throw ex
            }
        }
        file(Paths.get(directory.path, file).toFile(), optional).apply(extendContext)
    }
}
