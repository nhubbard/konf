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

package io.github.nhubbard.konf

/**
 * Exception for config.
 */
open class ConfigException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Exception indicates that there is existed item with the same name in config.
 */
class RepeatedItemException(val name: String) : ConfigException("item $name has been added")

/**
 * Exception indicates that there is existed inner spec in config.
 */
class RepeatedInnerSpecException(val spec: Spec) :
    ConfigException("spec ${spec.javaClass.simpleName}(prefix=\"${spec.prefix}\") has been added")

/**
 * Exception indicates that there is existed item with conflicted name in config.
 */
class NameConflictException(message: String) : ConfigException(message)

/**
 * Exception indicates that the evaluated result of lazy thunk is invalid.
 */
class InvalidLazySetException(message: String) : ConfigException(message)

val Item<*>.asName: String get() = "item $name"

/**
 * Exception indicates that the specified item is in unset state.
 */
class UnsetValueException(val name: String) : ConfigException("$name is unset") {
    constructor(item: Item<*>) : this(item.asName)
}

/**
 * Exception indicates that the specified item has the default value.
 */
class GetDefaultValueException(val name: String) : ConfigException("$name has default value") {
    constructor(item: Item<*>) : this(item.asName)
}

/**
 * Exception indicates that the specified item is not in this config.
 */
class NoSuchItemException(val name: String) : ConfigException("cannot find $name in config") {
    constructor(item: Item<*>) : this(item.asName)
}

/**
 * Exception indicates that item cannot be added to this config because it has the child layer.
 */
class LayerFrozenException(val config: Config) :
    ConfigException("config ${config.name} has child layer, cannot add new item")

/**
 * Exception indicates that the expected value in the specified path is not existed in the source.
 */
class NoSuchPathException(val path: String) :
    ConfigException("cannot find path \"$path\" in config spec")

/**
 * Exception indicates that the specified path is invalid.
 */
class InvalidPathException(val path: String) :
    ConfigException("\"$path\" is not a valid path")

/**
 * Exception indicates that the specified path conflicts with existed paths in the tree node.
 */
class PathConflictException(val path: String) :
    ConfigException("\"$path\" conflicts with existed paths in the tree node")
