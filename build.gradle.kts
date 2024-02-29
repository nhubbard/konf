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

import java.util.*

// Helper function to protect private properties from being published
fun getPrivateProperty(key: String, env: String, default: String = ""): String {
    val file = file("private.properties")
    return if (file.exists()) {
        val properties = Properties()
        properties.load(file.inputStream())
        properties.getProperty(key)
    } else {
        // Fallback if private.properties is not available
        System.getenv(env).takeIf { !it.isNullOrEmpty() }
            ?: default
    }
}

val ossUserToken by extra { getPrivateProperty("ossUserToken", "OSS_USER_TOKEN") }
val ossUserPassword by extra { getPrivateProperty("ossUserPassword", "OSS_USER_PASSWORD") }
val signPublication by extra { !System.getenv("JITPACK").toBoolean() }

plugins {
    java
    signing
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.allopen") version "1.9.22"
    id("org.jetbrains.dokka") version "1.9.10"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.1.1"
}

group = "io.github.nhubbard"
version = "2.0.2"

val projectDescription =
    "A type-safe cascading configuration library for Kotlin and Java, supporting most configuration formats"
val projectUrl = "https://github.com/nhubbard/konf"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    register("snippet")
    register("benchmark")
}

val snippetImplementation by configurations
snippetImplementation.extendsFrom(configurations.implementation.get())

val benchmarkImplementation by configurations
benchmarkImplementation.extendsFrom(configurations.implementation.get())

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

    // Snippet implementation
    snippetImplementation(sourceSets.main.get().output)
    val snippet by sourceSets
    testImplementation(snippet.output)

    // Benchmark implementation
    benchmarkImplementation(sourceSets.main.get().output)
    benchmarkImplementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")
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

    finalizedBy(tasks.koverHtmlReport)
    finalizedBy(tasks.koverXmlReport)
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.check {
    dependsOn(tasks.koverHtmlReport)
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(17)
            reportUndocumented.set(false)
            sourceLink {
                localDirectory.set(file("./"))
                remoteUrl.set(uri("https://github.com/nhubbard/konf/blob/v${project.version}/").toURL())
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks.sourcesJar {
    from(sourceSets.main.get().allSource)
}

tasks.withType<Javadoc>().all { enabled = false }

tasks.javadocJar.configure {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.get().outputDirectory)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named<Jar>("javadocJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.get().outputDirectory)
    excludes.addAll(tasks.javadoc.get().destinationDir?.listFiles()?.map { it.toString() } ?: listOf())
}

kotlin {
    jvmToolchain(21)

    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.BenchmarkMode")
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("benchmark")
    }
}

centralPortal {
    username = ossUserToken
    password = ossUserPassword

    pom {
        name = "konf"
        description = projectDescription
        url = projectUrl

        licenses {
            license {
                name.set("The Apache Software License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("nhubbard")
                name.set("nhubbard")
                email.set("nhubbard@users.noreply.github.com")
                url.set("https://github.com/nhubbard")
            }
        }

        scm {
            url.set(projectUrl)
        }
    }
}

signing {
    isRequired = signPublication
    if (signPublication) useGpgCmd()
}