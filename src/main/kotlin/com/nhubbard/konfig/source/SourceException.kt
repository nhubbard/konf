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

package com.nhubbard.konfig.source

import com.nhubbard.konfig.ConfigException
import com.nhubbard.konfig.Path
import com.nhubbard.konfig.TreeNode
import com.nhubbard.konfig.name

/**
 * Exception for source.
 */
open class SourceException : ConfigException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Exception indicates that actual type of value in source is unmatched with expected type.
 */
class WrongTypeException(val source: String, actual: String, expected: String) :
    SourceException("source $source has type $actual rather than $expected")

/**
 * Exception indicates that expected value in specified path is not existed in the source.
 */
class NoSuchPathException(val source: Source, val path: Path) :
    SourceException("cannot find path \"${path.name}\" in source ${source.description}")

/**
 * Exception indicates that there is a parsing error.
 */
class ParseException : SourceException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Exception indicates that value of specified class in unsupported in the source.
 */
class UnsupportedTypeException(source: Source, clazz: Class<*>) :
    SourceException("value of type ${clazz.simpleName} is unsupported in source ${source.description}")

/**
 * Exception indicates that watch key is no longer valid for the source.
 */
class InvalidWatchKeyException(source: Source) :
    SourceException("watch key for source ${source.description} is no longer valid")

/**
 * Exception indicates that the given repository is not in the remote list of the local repository.
 */
class InvalidRemoteRepoException(repo: String, dir: String) :
    SourceException("$repo is not in the remote list of $dir")

/**
 * Exception indicates failure to map source to value of specified class.
 */
class ObjectMappingException(source: String, clazz: Class<*>, cause: Throwable) :
    SourceException("unable to map source $source to value of type ${clazz.simpleName}", cause)

/**
 * Exception indicates that value of specified class is unsupported as key of map.
 */
class UnsupportedMapKeyException(val clazz: Class<*>) : SourceException(
    "cannot support map with ${clazz.simpleName} key"
)

/**
 * Exception indicates failure to load specified path.
 */
class LoadException(val path: Path, cause: Throwable) :
    SourceException("fail to load ${path.name}", cause)

/**
 * Exception indicates that the source contains unknown paths.
 */
class UnknownPathsException(source: Source, val paths: List<String>) :
    SourceException(
        "source ${source.description} contains the following unknown paths:\n" +
                paths.joinToString("\n")
    )

/**
 * Exception indicates that specified source is not found.
 */
class SourceNotFoundException(message: String) : SourceException(message)

/**
 * Exception indicates that specified source has unsupported extension.
 */
class UnsupportedExtensionException(source: String) : SourceException(
    "cannot detect supported extension for \"$source\"," +
            " supported extensions: conf, json, properties, toml, xml, yml, yaml"
)

/**
 * Exception indicates that undefined paths occur during variable substitution.
 */
class UndefinedPathVariableException(val source: Source, val text: String) : SourceException(
    "\"$text\" in source ${source.description} contains undefined path variables during path substitution"
)

/**
 * Exception indicates that the specified node has unsupported type.
 */
class UnsupportedNodeTypeException(val source: Source, val node: TreeNode) : SourceException(
    "$node of type ${node::class.java.simpleName} in source ${source.description} is unsupported"
)
