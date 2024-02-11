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

import io.github.nhubbard.konf.source.DefaultProviders
import io.github.nhubbard.konf.source.Source
import org.eclipse.jgit.lib.Constants
import java.io.File

/**
 * Returns a source from a specified git repository.
 *
 * The format of the url is auto-detected from the url extension.
 * Supported url formats and the corresponding extensions:
 * - HOCON: conf
 * - JSON: json
 * - Properties: properties
 * - TOML: toml
 * - XML: xml
 * - YAML: yml, yaml
 *
 * Throws [io.github.nhubbard.konf.source.UnsupportedExtensionException] if the url extension is unsupported.
 *
 * @param repo git repository
 * @param file file in the git repository
 * @param dir local directory of the git repository
 * @param branch the initial branch
 * @param optional whether the source is optional
 * @return a source from a specified git repository
 * @throws io.github.nhubbard.konf.source.UnsupportedExtensionException
 */
fun DefaultProviders.git(
    repo: String,
    file: String,
    dir: String? = null,
    branch: String = Constants.HEAD,
    optional: Boolean = false
): Source = dispatchExtension(File(file).extension, "{repo: $repo, file: $file}")
    .git(repo, file, dir, branch, optional)
