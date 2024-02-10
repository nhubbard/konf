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

plugins {
    java
    jacoco
    `maven-publish`
    signing
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.allopen") version "1.9.22"
    id("com.diffplug.spotless") version "6.25.0"
    id("org.jetbrains.dokka") version "1.9.10"
}

group = "com.nhubbard"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

sourceSets {
    register("snippet")
}

val snippetImplementation by configurations
snippetImplementation.extendsFrom(configurations.implementation.get())

dependencies {
    // Core implementation dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation(kotlin("reflect"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-text:1.11.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")

    // Git
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")

    // Hocon
    implementation("com.typesafe:config:1.4.3")

    // JS
    implementation("org.graalvm.sdk:graal-sdk:23.0.3")
    implementation("org.graalvm.js:js:23.0.3")

    // TOML
    implementation("com.moandjiezana.toml:toml4j:0.7.2")

    // XML
    implementation("org.dom4j:dom4j:2.1.4")
    implementation("jaxen:jaxen:2.0.0")

    // YAML
    implementation("org.yaml:snakeyaml:2.2")

    // Core test dependencies
    testImplementation(kotlin("test"))
    testImplementation("com.sparkjava:spark-core:2.9.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.12")
    testImplementation("org.jetbrains.spek:spek-api:1.1.5")
    testImplementation("org.jetbrains.spek:spek-data-driven-extension:1.1.5")
    testImplementation("org.jetbrains.spek:spek-subject-extension:1.1.5")
    testRuntimeOnly("org.jetbrains.spek:spek-junit-platform-engine:1.1.5")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("org.hamcrest:hamcrest:2.2")

    snippetImplementation(sourceSets.main.get().output)
    val snippet by sourceSets
    testImplementation(snippet.output)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("spek")
    }
    testLogging.apply {
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    systemProperties["org.slf4j.simpleLogger.defaultLogLevel"] = "warn"
    systemProperties["junit.jupiter.execution.parallel.enabled"] = true
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    environment("SOURCE_TEST_TYPE", "env")
    environment("SOURCE_CAMELCASE", "true")
}

kotlin {
    jvmToolchain(21)
}