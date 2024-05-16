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
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.allopen") version "1.9.24"
    id("org.jetbrains.dokka") version "1.9.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.6"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
    id("ca.solo-studios.sonatype-publish") version "0.1.3"
}

group = "io.github.nhubbard"
version = "2.1.0"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation(kotlin("reflect"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    // Git
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")

    // Hocon
    implementation("com.typesafe:config:1.4.3")

    // JS
    // WARNING!
    // Don't upgrade the GraalVM dependency versions!
    // The newer versions have different coordinates, and a bunch of unusual issues that have no documented fix on
    // non-Graal JDKs.
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
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("com.sparkjava:spark-core:2.9.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.13")

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
        includeEngines("junit-jupiter")
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
    systemProperties["line.separator"] = "\n"
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

kover {
    excludeSourceSets {
        names("benchmark", "snippet")
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        // Resolve Gson vulnerability from Toml4j
        if (requested.group == "com.google.code.gson" && requested.name == "gson")
            useVersion("2.10.1")
        // Resolve Jetty vulnerability from Spark
        if (requested.group == "org.eclipse.jetty" && requested.name.matches("^jetty-(server|xml|util|http)".toRegex()))
            useVersion("9.4.54.v20240208")
    }
}