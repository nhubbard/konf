# Konf

[![Java 17+](https://img.shields.io/badge/Java-17+-4c7e9f.svg)](http://java.oracle.com)
[![Maven metadata URL](https://img.shields.io/maven-central/v/io.github.nhubbard/konf)](https://search.maven.org/artifact/io.github.nhubbard/konf)
[![JitPack](https://jitpack.io/v/nhubbard/konf.svg)](https://jitpack.io/#nhubbard/konf)
[![codebeat badge](https://codebeat.co/badges/37ec4fe4-893b-41ec-bd1c-56794fa3d0c5)](https://codebeat.co/projects/github-com-nhubbard-konf-master)
[![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)

A type-safe cascading configuration library for Kotlin/Java/Android, supporting most configuration formats.

## Features

- **Type-safe**. Get/set value in config with type-safe APIs.
- **Thread-safe**. All APIs for config is thread-safe.
- **Batteries included**. Support sources from JSON, XML, YAML, [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md), [TOML](https://github.com/toml-lang/toml), properties, map, command line and system environment out of box.
- **Cascading**. Config can fork from another config by adding a new layer on it. Each layer of config can be updated independently. This feature is powerful enough to support complicated situations such as configs with different values share common fallback config, which is automatically updated when configuration file changes.
- **Self-documenting**. Document config item with type, default value and description when declaring.
- **Extensible**. Konf makes it easy to customize new sources for config or expose items in config.

## Contents

- [konf](#konf)
    - [Features](#features)
    - [Contents](#contents)
    - [Prerequisites](#prerequisites)
    - [Use in your projects](#use-in-your-projects)
        - [Maven](#maven)
        - [Gradle](#gradle)
        - [Gradle Kotlin DSL](#gradle-kotlin-dsl)
        - [Maven (master snapshot)](#maven-master-snapshot)
        - [Gradle (master snapshot)](#gradle-master-snapshot)
        - [Gradle Kotlin DSL (master snapshot)](#gradle-kotlin-dsl-master-snapshot)
    - [Quick start](#quick-start)
    - [Define items](#define-items)
    - [Using your configuration](#using-your-configuration)
        - [Create config](#create-config)
        - [Add config spec](#add-config-spec)
        - [Retrieve value from config](#retrieve-value-from-config)
        - [Cast config to value](#cast-config-to-value)
        - [Check whether an item exists in config or not](#check-whether-an-item-exists-in-config-or-not)
        - [Modify value in config](#modify-value-in-config)
        - [Subscribing to update events](#subscribing-to-update-events)
        - [Export value in config as property](#export-value-in-config-as-property)
        - [Fork from another config](#fork-from-another-config)
    - [Load values from source](#load-values-from-source)
        - [Subscribe the update event for load operation](#subscribe-to-update-events-for-load-operations)
        - [Strict parsing when loading](#strict-parsing-when-loading)
        - [Path substitution](#path-substitution)
    - [Prefix/Merge operations for source/config/config spec](#prefixmerge-operations-for-sourceconfigconfig-spec)
    - [Export/Reload values in config](#exportreload-values-in-config)
    - [Supported item types](#supported-item-types)
    - [Optional features](#optional-features)
    - [Build from source](#build-from-source)
- [License](#license)

## Prerequisites

- JDK 17 or higher
- Tested on Android SDK 34 or higher

## Use in your projects

This library has been published to [Maven Central](https://search.maven.org/artifact/com.nhubbard/konf) and [JitPack](https://jitpack.io/#nhubbard/konf).

### Maven

```xml
<dependency>
  <groupId>io.github.nhubbard</groupId>
  <artifactId>konf</artifactId>
  <version>2.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.nhubbard:konf:2.1.0'
```

### Gradle Kotlin DSL

```kotlin
implementation("io.github.nhubbard:konf:2.1.0")
```

### Maven (master snapshot)

Add JitPack repository to `<repositories>` section:

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Add dependencies:

```xml
<dependency>
    <groupId>com.github.nhubbard</groupId>
    <artifactId>konf</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

### Gradle (master snapshot)

Add the JitPack repository:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add dependencies:

```groovy
implementation 'com.github.nhubbard:konf:master-SNAPSHOT'
```

### Gradle Kotlin DSL (master snapshot)

Add the JitPack repository:

```kotlin
repositories {
    maven("https://jitpack.io")
}
```

Add dependencies:

```kotlin
implementation("com.github.nhubbard:konf:master-SNAPSHOT")
```

## Quick start

1. Define your options in a `ConfigSpec`:

    ```kotlin
    object ServerSpec : ConfigSpec() {
        val host by optional("0.0.0.0")
        val tcpPort by required<Int>()
    }
    ```

2. Create an instance of `Config` with your `ConfigSpec` and your preferred sources:

    ```kotlin
    val config = Config { addSpec(ServerSpec) }
            .from.yaml.file("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
    ```

   or:

    ```kotlin
    val config = Config { addSpec(ServerSpec) }.withSource(
        Source.from.yaml.file("server.yml") +
        Source.from.json.resource("server.json") +
        Source.from.env() +
        Source.from.systemProperties()
    )
    ```

   The `config` variable now contains all items defined in `ServerSpec`,
   and will load values from the four defined sources.

   The values in resource file `server.json` will override those in `server.yml`,
   the system environment variables, `server.json`, and system properties.

   If you want to watch file `server.yml` and reload values when file content is changed, you can use `watchFile` instead of `file`:

    ```kotlin
    val config = Config { addSpec(ServerSpec) }
            .from.yaml.watchFile("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
    ```

3. Define your configuration values in your source. For example:
    - in `server.yml`:
        ```yaml
        server:
            host: 0.0.0.0
            tcp_port: 8080
        ```
    - in `server.json`:
        ```json
        {
            "server": {
                "host": "0.0.0.0",
                "tcp_port": 8080
            }
        }
        ```
    - in system environment:
        ```bash
        SERVER_HOST=0.0.0.0
        SERVER_TCPPORT=8080
        ```
    - on the command line for system properties:
        ```bash
        -Dserver.host=0.0.0.0 -Dserver.tcp_port=8080
        ```

4. Now, you can retrieve values from `config` with type-safe APIs:
    ```kotlin
    data class Server(val host: String, val tcpPort: Int) {
        fun start() {}
    }
    
    val server = Server(config[ServerSpec.host], config[ServerSpec.tcpPort])
    server.start()
    ```

5. You can also retrieve values from multiple sources without using the config spec:

    ```kotlin
    val server = Config()
            .from.yaml.file("server.yml")
            .from.json.resource("server.json")
            .from.env()
            .from.systemProperties()
            .at("server")
            .toValue<Server>()
    server.start()
    ```

## Define items

Configuration items are declared in the config spec and added to the config by `Config#addSpec`.

All items in each `ConfigSpec` have the same prefix. For example, to define a config spec with prefix `server`:

```kotlin
object ServerSpec : ConfigSpec("server")
```

If the `ConfigSpec` is binding with a single class, you can declare the `ConfigSpec` as a companion object of the class:

```kotlin
class Server {
    companion object : ConfigSpec("server") {
        val host by optional("0.0.0.0")
        val tcpPort by required<Int>()
    }
}
```

The `ConfigSpec` prefix can also automatically be inferred from the class name. For example:

```kotlin
object ServerSpec : ConfigSpec()
```

or

```kotlin
class Server {
    companion object : ConfigSpec()
}
```

Here are some examples showing the inference convention:

* `Uppercase` to `uppercase`
* `lowercase` to `lowercase`
* `SuffixSpec` to `suffix`
* `TCPService` to `tcpService`

The `ConfigSpec` can also be nested.

For example, the path `Service.Backend.Login.user` in the following example will be inferred as "service.backend.login.user":

```kotlin
object Service : ConfigSpec() {
    object Backend : ConfigSpec() {
        object Login : ConfigSpec() {
            val user by optional("admin")
        }
    }
}
```

There are three kinds of `Item`:

- **Required items:** These don't have default values. If a value isn't provided at runtime, an exception is raised.
    ```kotlin
    // You can provide a description for each configuration entry.
    val tcpPort by required<Int>(description = "port of server")
    // You can also omit the description:
    val name by required<String>()
    ```

- **Optional items.** These items have default values, and thus can be safely retrieved at any time.
    ```kotlin
    // Similarly to required items, you can omit the description.
    // However, you have to provide a default value for optional items.
    val host by optional("0.0.0.0", description = "host IP of server")
    ```

- **Lazy items.** These also have default values, but the default value is not a constant; instead, it is evaluated from a lambda every time it is retrieved.
    ```kotlin
    val nextPort by lazy { config -> config[tcpPort] + 1 }
    ```

You can also define a `ConfigSpec` in Java, with a more verbose API (compared to the Kotlin version in "quick start"):

```java
public class ServerSpec {
  public static final ConfigSpec spec = new ConfigSpec("server");

  public static final OptionalItem<String> host =
      new OptionalItem<String>(spec, "host", "0.0.0.0") {};

  public static final RequiredItem<Integer> tcpPort = new RequiredItem<Integer>(spec, "tcpPort") {};
}
```

The `{}` after every item declaration is necessary to avoid erasing the type of the item.

## Using your configuration

### Create config

To create a new empty config, use the default constructor:

```kotlin
val config = Config()
```

To create a new config with your `ConfigSpec`s, add them using the `addSpec` function in a lambda passed to `Config`:

```kotlin
val config = Config { addSpec(Server) }
```

### Add config spec

If you need to add more config specs after calling the constructor, you can use the `addSpec` function on your instance
of `Config`:

```kotlin
config.addSpec(Server)
config.addSpec(Client)
```

### Retrieve value from config

To retrieve the value associated with your config item, you can use the type-safe API:

```kotlin
val host = config[Server.host]
```

Alternatively, you can use the "unsafe" API with a fully qualified string path to your spec:

```kotlin
val host = config.get<String>("server.host")
```

You can also omit the `.get`:

```kotlin
val host = config<String>("server.host")
```

The unsafe API is the suggested method to use in Java.
It is possible to use the type-safe API from Java, but it is significantly more clumsy than using the unsafe API.

### Cast config to value

You can cast a config instance to a value given a target type:

```kotlin
val server = config.toValue<Server>()
```

### Check whether an item exists in config or not

To check whether an item exists in the config, use the `contains` function or the `in` overload:

```kotlin
config.contains(Server.host)
// or
Server.host in config
```

To check whether an item exists in the config by name, you can do the same,
but pass the fully qualified value path instead:

```kotlin
config.contains("server.host")
// or
"server.host" in config
```

To check whether all required configuration items exist in the config, use `containsRequired`:

```kotlin
config.containsRequired()
```

To throw an exception if any required config items don't have values, use `validateRequired`:

```kotlin
config.validateRequired()
```

### Modify value in config

To associate a new value with an item, you can use the type-safe API:

```kotlin
config[Server.tcpPort] = 80
```

Alternatively, use the unsafe fully qualified item path API:

```kotlin
config["server.tcpPort"] = 80
```

To discard the associated value of the item, with the type-safe API, use `unset`:

```kotlin
config.unset(Server.tcpPort)
```

Similarly, to discard the associated value of the item by name, use the unsafe `unset` API:

```kotlin
config.unset("server.tcpPort")
```

To associate an item with a lazy lambda using the type-safe API, use `lazySet`:

```kotlin
config.lazySet(Server.tcpPort) { it[basePort] + 1 }
```

Similarly, to associate an item with a lazy lambda using the unsafe API, use `lazySet`:

```kotlin
config.lazySet("server.tcpPort") { it[basePort] + 1 }
```

### Subscribing to update events

If you want your program to react when a configuration item is updated, use `onSet` on an item:

```kotlin
val handler = Server.host.onSet { value -> println("the host has changed to $value") }
```

If you want your program to react before a configuration item is updated, use `beforeSet`:

```kotlin
val handler = Server.host.beforeSet { config, value -> println("the host will change to $value") }
```

You can also use the same API on an instance of `Config` to react on all config updates:

```kotlin
val handler = config.beforeSet { item, value -> println("${item.name} will change to $value") }
```

If you want your program to react after a configuration item is updated, use `afterSet` on an item:

```kotlin
val handler = Server.host.afterSet { config, value -> println("the host has changed to $value") }
```

or on a `Config` instance:

```kotlin
val handler = config.afterSet { item, value -> println("${item.name} has changed to $value") }
```

Finally, to cancel the subscription, use `cancel` on the handler returned by `beforeSet`, `onSet`, or `afterSet`:

```kotlin
handler.cancel()
```

### Export value in config as property

To export a read-write property value from the configuration, use the `property` delegate with a `var` statement:

```kotlin
var port by config.property(Server.tcpPort)
port = 9090
check(port == 9090)
```

To export a read-only property value from the configuration, use the `property` delegate with a `val` statement:

```kotlin
val port by config.property(Server.tcpPort)
check(port == 9090)
```

### Fork from another config

It is possible to "fork" a configuration to make a separate instance of a `Config`

When the parent instance is modified, the changes *will* propagate to the child instance.

If the child instance is modified, the changes will *not* propagate to the parent instance.

```kotlin
val config = Config { addSpec(Server) }
config[Server.tcpPort] = 1000
// Fork from the parent config.
val childConfig = config.withLayer("child")
// Create a child config that inherits its values from the parent config.
check(childConfig[Server.tcpPort] == 1000)
// Modifications of the parent config will affect the values of the child config.
config[Server.tcpPort] = 2000
check(config[Server.tcpPort] == 2000)
check(childConfig[Server.tcpPort] == 2000)
// Modifications to the child config will not affect the values of the parent config.
childConfig[Server.tcpPort] = 3000
check(config[Server.tcpPort] == 2000)
check(childConfig[Server.tcpPort] == 3000)
```

## Load values from source

Use the `from` receiver to load values from a source that won't affect existing values in the config.

It will return a new child config by loading all values into new layer in child config:

```kotlin
val config = Config { addSpec(Server) }
// The values in the source are loaded into the new layer in the child config
val childConfig = config.from.env()
check(childConfig.parent === config)
```

The included sources are declared in [`DefaultLoaders`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/DefaultLoaders.kt).

Each source is shown below.

The corresponding config spec for these samples is [`ConfigForLoad`](https://github.com/nhubbard/konf/blob/master/src/test/kotlin/com/nhubbard/konf/source/ConfigForLoad.kt).

| **Type**                                                            | **Usage**                        | **Provider**                                                                                                                                         | **Sample**                                                                                                                               |
|---------------------------------------------------------------------|----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) | `config.from.hocon`              | [`HoconProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/hocon/HoconProvider.kt)                | [`source.conf`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.conf)                                      |
| JSON                                                                | `config.from.json`               | [`JsonProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/json/JsonProvider.kt)                   | [`source.json`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.json)                                      |
| properties                                                          | `config.from.properties`         | [`PropertiesProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/properties/PropertiesProvider.kt) | [`source.properties`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.properties)                          |
| [TOML](https://github.com/toml-lang/toml)                           | `config.from.toml`               | [`TomlProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/toml/TomlProvider.kt)                   | [`source.toml`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.toml)                                      |
| XML                                                                 | `config.from.xml`                | [`XmlProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/xml/XmlProvider.kt)                      | [`source.xml`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.xml)                                        |
| YAML                                                                | `config.from.yaml`               | [`YamlProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/yaml/YamlProvider.kt)                   | [`source.yaml`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.yaml)                                      |
| JavaScript                                                          | `config.from.js`                 | [`JsProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/js/JsProvider.kt)                         | [`source.js`](https://github.com/nhubbard/konf/blob/master/src/test/resources/source/source.js)                                          |
| Hierarchical map                                                    | `config.from.map.hierarchical`   | Built-in                                                                                                                                             | [`MapSourceLoadSpec`](https://github.com/nhubbard/konf/blob/master/src/test/kotlin/com/nhubbard/konf/source/base/MapSourceLoadSpec.kt)   |
| Map in key-value format                                             | `config.from.map.kv`             | Built-in                                                                                                                                             | [`KVSourceSpec`](https://github.com/nhubbard/konf/blob/master/src/test/kotlin/com/nhubbard/konf/source/base/KVSourceSpec.kt)             |
| Map in flat format                                                  | `config.from.map.flat`           | Built-in                                                                                                                                             | [`FlatSourceLoadSpec`](https://github.com/nhubbard/konf/blob/master/src/test/kotlin/com/nhubbard/konf/source/base/FlatSourceLoadSpec.kt) |
| System environment variables                                        | `config.from.env()`              | [`EnvProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/env/EnvProvider.kt)                      | -                                                                                                                                        |
| System properties                                                   | `config.from.systemProperties()` | [`PropertiesProvider`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/properties/PropertiesProvider.kt) | -                                                                                                                                        |

These sources can also be manually created using their provider, and then loaded into an instance of `Config` using
`config.withSource(source)`.

All `from` APIs have a standalone version that returns sources without loading them into the config, as shown below:

| **Type**                                                            | **Usage**                        |
|---------------------------------------------------------------------|----------------------------------|
| [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) | `Source.from.hocon`              |
| JSON                                                                | `Source.from.json`               |
| Properties                                                          | `Source.from.properties`         |
| [TOML](https://github.com/toml-lang/toml)                           | `Source.from.toml`               |
| XML                                                                 | `Source.from.xml`                |
| YAML                                                                | `Source.from.yaml`               |
| JavaScript                                                          | `Source.from.js`                 |
| Hierarchical map                                                    | `Source.from.map.hierarchical`   |
| Map in key-value format                                             | `Source.from.map.kv`             |
| Map in flat format                                                  | `Source.from.map.flat`           |
| System environment variables                                        | `Source.from.env()`              |
| System properties                                                   | `Source.from.systemProperties()` |

The format of the system properties source is the same as the properties source.

The system environment source follows the same mapping convention as the properties file source, but all letters in the
name are in uppercase, and `.` in the name is replaced with `_`.

For example, an item with the fully qualified path `server.port` would be loaded from environment variables as
`SERVER_PORT`.

HOCON/JSON/properties/TOML/XML/YAML/JavaScript sources can be loaded from a variety of input formats.
Using the properties source as an example:

- From a file: `config.from.properties.file("/path/to/file")`
- From a watched file: `config.from.properties.watchFile("/path/to/file", 100, TimeUnit.MILLISECONDS)`
    - You can re-trigger the setup process every time the updated file is loaded using `watchFile("/path/to/file") { config, source -> setup(config) }`.
- From a string: `config.from.properties.string("server.port = 8080")`
- From a URL: `config.from.properties.url("http://localhost:8080/source.properties")`
- From a watched URL: `config.from.properties.watchUrl("http://localhost:8080/source.properties", 1, TimeUnit.MINUTES)`
    - You can re-trigger the setup process every time the URL is loaded using `watchUrl("http://localhost:8080/source.properties") { config, source -> setup(config) }`.
- From a Git repository: `config.from.properties.git("https://github.com/nhubbard/konf.git", "/path/to/source.properties", branch = "dev")`
- From a watched Git repository: `config.from.properties.watchGit("https://github.com/nhubbard/konf.git", "/path/to/source.properties", period = 1, unit = TimeUnit.MINUTES)`
    - You can re-trigger the setup process every time the Git file is loaded using `watchGit("https://github.com/nhubbard/konf.git", "/path/to/source.properties") { config, source -> setup(config) }`.
- From a resource: `config.from.properties.resource("source.properties")`
- From a `Reader`: `config.from.properties.reader(reader)`
- From an `InputStream`: `config.from.properties.inputStream(inputStream)`
- From a `ByteArray`: `config.from.properties.bytes(bytes)`
- From a portion of a `ByteArray`: `config.from.properties.bytes(bytes, 1, 12)`

If the source is a file, the file extension can be auto-detected.

You can use `config.from.file("/path/to/source.json")` instead of `config.from.json.file("/path/to/source.json")`,
or use `config.from.watchFile("/path/to/source.json")` instead of `config.from.json.watchFile("/path/to/source.json")`.

URLs also support auto-detecting the extension (use `config.from.url` or `config.from.watchUrl`).

The following file extensions support auto-detection:

| **Type**   | **Extension(s)** |
|------------|------------------|
| HOCON      | `conf`           |
| JSON       | `json`           |
| Properties | `properties`     |
| TOML       | `toml`           |
| XML        | `xml`            |
| YAML       | `yml`, `yaml`    |
| JavaScript | `js`             |

You can also implement your own [`Source`](https://github.com/nhubbard/konf/blob/master/src/main/kotlin/io/github/nhubbard/konf/source/Source.kt)
to customize your new source, which can be loaded into config using `config.withSource(source)`.

### Subscribe to update events for load operations

To subscribe to update events before every load operation, use `beforeLoad`:

```kotlin
val handler = config.beforeLoad { source -> println("$source will be loaded") }
```

You can re-trigger the setup process by subscribing to the update event after every load operation using `afterLoad`:

```kotlin
val handler = config.afterLoad { source -> setup(config) }
```

And to cancel the subscription, use `cancel`:

```kotlin
handler.cancel()
```

### Strict parsing when loading

By default, Konf extracts the desired paths from sources and ignores other unknown paths in sources.
If you want Konf to throw an exception when unknown paths are found, you can enable the `FAIL_ON_UNKNOWN_PATH` feature:

```kotlin
config.enable(Feature.FAIL_ON_UNKNOWN_PATH)
    .from.properties.file("server.properties")
    .from.json.resource("server.json")
```

`config` will validate paths from both the properties file and the JSON resource.
Furthermore, if you want to validate only one source file, you can use `enable` like so:

```kotlin
config.from.enable(Feature.FAIL_ON_UNKNOWN_PATH).properties.file("/path/to/file")
    .from.json.resource("server.json")
```

### Path substitution

Path substitution is a feature substitutes path references in a source with their values.

The following rules apply to path substitution:

- Only quoted string value will be substituted. This is to ensure path substitutions made by Konf will not conflict with HOCON substitutions.
- The definition of a path variable uses Kotlin string interpolation syntax; e.g., `${java.version}`.
- The path variable is resolved in the context of the current source.
- If the string value only contains the path variable, it will be replaced by the whole subtree in the path; otherwise, it will be replaced by the string value in the path.
- Use `${path:-default}` to provide a default value when the path is unresolved; e.g., `${java.version:-8}`.
- Use `$${path}` to escape the path variable, e.g., `$${java.version}` will be resolved to `${java.version}` instead of the value in `java.version`.
- Path substitution works in a recursive way, so nested path variables like `${jre-${java.specification.version}}` are allowed.
- Konf also supports all key prefixes of the Apache Commons Text [StringSubstitutor](https://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/StringSubstitutor.html).

Konf will perform path substitution for every source by default, except for the system environment source, upon loading
the config.

You can disable this behavior by using `config.disable(Feature.SUBSTITUTE_SOURCE_BEFORE_LOADED)` for the config
or `source.disabled(Feature.SUBSTITUTE_SOURCE_BEFORE_LOADED)` for a single source.

By default, Konf will throw exception if any path variables are unresolved.
You can use `source.substituted(false)` manually to ignore these unresolved variables.

To resolve path variables that refer to other sources, you can merge these sources before loading them into the config.
For example, if we have two sources `source1.json` and `source2.properties`, where `source1.json` is:

```json
{ 
  "base": {
    "user": "konf",
    "password": "passwd"
  }
}
```

and `source2.properties` is:

```properties
connection.jdbc=mysql://${base.user}:${base.password}@server:port
```

then use:

```kotlin
config.withSource(
    Source.from.file("source1.json") +
    Source.from.file("source2.properties")
)
```

to merge these sources correctly.

We can then resolve `mysql://${base.user}:${base.password}@server:port` as `mysql://konf:passwd@server:port`.

## Prefix/Merge operations for source/config/config spec

All of the `Source`/`Config`/`ConfigSpec` support the add, remove, and merge prefix operations as shown below:

| **Type** | **Add Prefix**                                                                                        | **Remove Prefix**                                           | **Merge**                                              |
|----------|-------------------------------------------------------------------------------------------------------|-------------------------------------------------------------|--------------------------------------------------------|
| `Source` | `source.withPrefix(prefix)` or `Prefix(prefix) + source` or `config.from.prefixed(prefix).file(file)` | `source[prefix]` or `config.from.scoped(prefix).file(file)` | `fallback + facade` or `facade.withFallback(fallback)` |
| `Config` | `config.withPrefix(prefix)` or `Prefix(prefix) + config`                                              | `config.at(prefix)`                                         | `fallback + facade` or `facade.withFallback(fallback)` |
| `Spec`   | `spec.withPrefix(prefix)` or `Prefix(prefix) + spec`                                                  | `spec[prefix]`                                              | `fallback + facade` or `facade.withFallback(fallback)` |

## Export/Reload values in config

To export all values in the config as a tree, use `config.toTree()`:

```kotlin
val tree = config.toTree()
```

To export all values in the config to a map in key-value format, use `config.toMap()`:

```kotlin
val map = config.toMap()
```

To export all values in the config to a hierarchical map, use `config.toHierarchicalMap()`:

```kotlin
val map = config.toHierarchicalMap()
```

To export all values in the config to a map in a flat format, use `config.toFlatMap()`:

```kotlin
val map = config.toFlatMap()
```

To export all values in the config to JSON, use `config.toJson.toFile(file)`:

```kotlin
val file = createTempFile(suffix = ".json")
config.toJson.toFile(file)
```

To reload the values from JSON, recreate the config:

```kotlin
val newConfig = Config {
    addSpec(Server)
}.from.json.file(file)
check(config == newConfig)
```

The config can be saved to a variety of output formats. Using JSON as an example:

- Export to file: `config.toJson.toFile("/path/to/file")`
- Export to string: `config.toJson.toText()`
- Export to `Writer`: `config.toJson.toWriter(writer)`
- Export to `OutputStream`: `config.toJson.toOutputStream(outputStream)`
- Export to `ByteArray`: `config.toJson.toBytes()`

You can also implement the [`Writer`](https://github.com/nhubbard/konf/blob/master/konf-core/src/main/kotlin/io/github/nhubbard/konf/source/Writer.kt) interface
to customize your new writer
(see
[`JsonWriter`](https://github.com/nhubbard/konf/blob/master/konf-core/src/main/kotlin/io/github/nhubbard/konf/source/json/JsonWriter.kt) for how to integrate your writer with config).

## Supported item types

Supported item types include:

- All primitive types
- All primitive array types
- `BigInteger`
- `BigDecimal`
- `String`
- Date and Time
    - `java.util.Date`
    - `OffsetTime`
    - `OffsetDateTime`
    - `ZonedDateTime`
    - `LocalDate`
    - `LocalTime`
    - `LocalDateTime`
    - `Year`
    - `YearMonth`
    - `Instant`
    - `Duration`
- `SizeInBytes`
- Enum
- Array
- Collection
    - `List`
    - `Set`
    - `SortedSet`
    - `Map`
    - `SortedMap`
- Kotlin Built-in classes
    - `Pair`
    - `Triple`
    - `IntRange`
    - `CharRange`
    - `LongRange`
- Data classes
- POJOs supported by Jackson core modules

Konf supports the size in bytes format as described in the [HOCON specification](https://github.com/typesafehub/config/blob/master/HOCON.md#size-in-bytes-format) with the class `SizeInBytes`.

Konf supports both the [ISO-8601 duration format](https://en.wikipedia.org/wiki/ISO_8601#Durations) and [HOCON duration format](https://github.com/typesafehub/config/blob/master/HOCON.md#duration-format) for `Duration`.

Konf uses [Jackson](https://github.com/FasterXML/jackson) to support Kotlin built-in classes, data classes, and POJOs.
You can use `config.mapper` to access the `ObjectMapper` instance used by config,
and configure it to support more types from third-party Jackson modules.
The default modules registered by Konf include:

- Jackson core modules
- `JavaTimeModule` in [jackson-modules-java8](https://github.com/FasterXML/jackson-modules-java8)
- [jackson-module-kotlin](https://github.com/FasterXML/jackson-module-kotlin)

## Optional features

There are some optional features that you can enable/disable in the config scope or the source scope using
`Config#enable(Feature)`/`Config#disable(Feature)` or `Source#enabled(Feature)`/`Source#disable(Feature)`.
You can use `Config#isEnabled()` or `Source#isEnabled()` to check whether a feature is enabled.

These features include:

- `FAIL_ON_UNKNOWN_PATH`: feature that determines what happens when unknown paths appear in the source. If enabled, an exception is thrown when loading from the source to indicate it contains unknown paths. This feature is disabled by default.
- `LOAD_KEYS_CASE_INSENSITIVELY`: feature that determines whether keys are loaded from the sources case-insensitively. This feature is disabled by default except for system environment.
- `LOAD_KEYS_AS_LITTLE_CAMEL_CASE`: feature that determines whether loading keys from sources as little camel case. This feature is enabled by default.
- `OPTIONAL_SOURCE_BY_DEFAULT`: feature that determines whether sources are optional by default. This feature is disabled by default.
- `SUBSTITUTE_SOURCE_BEFORE_LOADED`: feature that determines whether sources should be substituted before loaded into config. This feature is enabled by default.
- `WRITE_DESCRIPTIONS_AS_COMMENTS`: feature that exports item descriptions as comments in supported formats. This feature is disabled by default and currently in development.

## Build from source

To build the library with Gradle, use the following command:

```
./gradlew clean assemble
```

To test the library with Gradle, use the following command:

```
./gradlew clean test
```

Since Gradle has excellent incremental build support, you can usually omit executing the `clean` task.

To install the library in a local Maven repository for consumption in other projects, use the following command:

```
./gradlew clean install
```

# License

Copyright © 2017-2024 Uchuhimo and 2024-present Nicholas Hubbard. Licensed under an [Apache 2.0](./LICENSE) license.
