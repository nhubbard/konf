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

pluginManagement {
    repositories {
        maven("https://maven.solo-studios.ca/releases/")
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "konf"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("com.gradle.develocity") version "3.17.4"
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("jackson", "2.17.1")
            // WARNING!
            // Don't upgrade the GraalVM dependency version!
            // The newer versions have different coordinates and a bunch of unusual issues.
            // There is no documented fix on non-Graal JDKs.
            version("graal", "22.3.5")

            plugin("dokka", "org.jetbrains.dokka").version("1.9.20")
            plugin("kover", "org.jetbrains.kotlinx.kover").version("0.8.0")
            plugin("benchmark", "org.jetbrains.kotlinx.benchmark").version("0.4.10")
            plugin("sonatype-publisher", "net.thebugmc.gradle.sonatype-central-portal-publisher").version("1.2.3")
            plugin("solo-publisher", "ca.solo-studios.sonatype-publish").version("0.1.3")

            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.8.1")
            library("reflections", "org.reflections", "reflections").version("0.10.2")
            library("commons-text", "org.apache.commons", "commons-text").version("1.12.0")
            library("jackson-core", "com.fasterxml.jackson.core", "jackson-core").versionRef("jackson")
            library("jackson-annotations", "com.fasterxml.jackson.core", "jackson-annotations").versionRef("jackson")
            library("jackson-databind", "com.fasterxml.jackson.core", "jackson-databind").versionRef("jackson")
            library("jackson-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef("jackson")
            library("jackson-jsr310", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").versionRef("jackson")
            library("jgit", "org.eclipse.jgit", "org.eclipse.jgit").version("6.9.0.202403050737-r")
            library("hocon", "com.typesafe", "config").version("1.4.3")
            library("graal-sdk", "org.graalvm.sdk", "graal-sdk").versionRef("graal")
            library("graal-js", "org.graalvm.js", "js").versionRef("graal")
            library("toml", "com.moandjiezana.toml", "toml4j").version("0.7.2")
            library("dom4j", "org.dom4j", "dom4j").version("2.1.4")
            library("jaxen", "jaxen", "jaxen").version("2.0.0")
            library("snakeyaml", "org.yaml", "snakeyaml").version("2.2")
            library("junit-params", "org.junit.jupiter", "junit-jupiter-params").version("5.10.2")
            library("spark", "com.sparkjava", "spark-core").version("2.9.4")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").version("2.0.13")
            library("kotlinx-benchmark-runtime", "org.jetbrains.kotlinx", "kotlinx-benchmark-runtime").version("0.4.10")

            bundle("jackson", listOf("jackson-core", "jackson-annotations", "jackson-databind", "jackson-kotlin", "jackson-jsr310"))
            bundle("graal", listOf("graal-sdk", "graal-js"))
        }
    }
}