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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.allopen") version "2.1.0"
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.benchmark)
    alias(libs.plugins.sonatype.publisher)
    alias(libs.plugins.solo.publisher)
}

group = "io.github.nhubbard"
version = "2.2.1"

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
    // Core Kotlin dependencies
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    // KotlinX Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Reflections
    implementation(libs.reflections)

    // Apache Commons Text
    implementation(libs.commons.text)

    // FasterXML Jackson
    implementation(libs.bundles.jackson)

    // Git
    implementation(libs.jgit)

    // Hocon
    implementation(libs.hocon)

    // JS
    implementation(libs.bundles.graal)

    // TOML
    implementation(libs.toml)

    // XML
    implementation(libs.dom4j)
    implementation(libs.jaxen)

    // YAML
    implementation(libs.snakeyaml)

    // Core test dependencies
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.spark)
    testRuntimeOnly(libs.slf4j.simple)

    // Snippet implementation
    snippetImplementation(sourceSets.main.get().output)
    val snippet by sourceSets
    testImplementation(snippet.output)

    // Benchmark implementation
    benchmarkImplementation(sourceSets.main.get().output)
    benchmarkImplementation(libs.kotlinx.benchmark.runtime)
}

tasks.test {
    useJUnitPlatform()
    testLogging.apply {
        showStandardStreams = true
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    systemProperties(
        "org.slf4j.simpleLogger.defaultLogLevel" to "warn",
        "junit.jupiter.execution.parallel.enabled" to true,
        "junit.jupiter.execution.parallel.mode.default" to "concurrent",
        "line.separator" to "\n"
    )
    environment(
        "SOURCE_TEST_TYPE" to "env",
        "SOURCE_CAMELCASE" to "true"
    )
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

    finalizedBy(tasks.koverHtmlReport)
    finalizedBy(tasks.koverXmlReport)
}

tasks.compileJava {
    sourceCompatibility = "11"
    targetCompatibility = "11"
    options.encoding = "UTF-8"
}

tasks.check {
    dependsOn(tasks.koverHtmlReport)
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            jdkVersion.set(11)
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

// This makes sure that Gradle is always run using JDK 21, independent of the build requirements for Konf.
// tasks.updateDaemonJvm {
//    jvmVersion = JavaVersion.VERSION_21
// }

kotlin {
    jvmToolchain(11)

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }

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
    currentProject {
        sources {
            excludedSourceSets.addAll("benchmark", "snippet")
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        // Resolve Gson vulnerability from Toml4j
        if (requested.group == "com.google.code.gson" && requested.name == "gson")
            useVersion("2.11.0")
        // Resolve Jetty vulnerability from Spark
        if (requested.group == "org.eclipse.jetty" && requested.name.matches("^jetty-(server|xml|util|http)".toRegex()))
            useVersion("9.4.55.v20240627")
    }
}