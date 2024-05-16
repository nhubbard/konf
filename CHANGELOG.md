# Changelog

## 2.1.0

### General

* The copyright statement on all source code files has been updated.
* A bug affecting file watches on non-macOS platforms has been identified and fixed. The issue was caused by an erroneous generic cast unintentionally converting the watched path from the internal path type to a type alias of the same name that was exported from another file.
* Log messages from the GraalVM Polyglot engine emitted when Konf is not running on a GraalVM JDK have now been suppressed.
* A bug affecting YAML writer output for strings that contain `:` has been identified and fixed.

### Build

* Build system upgrades:
	* Gradle: 8.6 -> 8.7
	* Gradle Resolver: 0.5.0 -> 0.8.0
	* Gradle Develocity: 3.0 -> 3.17.3
	* Kotlin: 1.9.22 -> 1.9.24
	* Kotlin All-Open: 1.9.22 -> 1.9.24
	* Dokka: 1.9.10 -> 1.9.20
	* Kover: 0.7.5 -> 0.7.6
	* Central Portal Publisher: 1.1.1 -> 1.2.3
* Dependency upgrades:
	* KotlinX Coroutines Core: 1.7.3 -> 1.8.0
	* Apache Commons Text: 1.11.0 -> 1.12.0
	* Jackson Core, Modules, and DataTypes: 2.16.1 -> 2.17.1
	* JGit: 6.8.0 -> 6.9.0
* Test dependency upgrades:
	* JUnit Jupiter Params extension: 5.10.2
	* SLF4J Simple: 2.0.12 -> 2.0.13

### Tests

* All tests have been completely rewritten to use standard JUnit Jupiter with Kotlin test assertions instead of the unmaintained JetBrains Spek engine and Hamkrest assertions.
* All test classes have been reorganized to separate "helper" classes that are only used by tests from the actual tests.
* Line separators now use macOS/Linux style on all platforms to avoid test failures on Windows due to inconsistent behavior between the different configuration language parsers and writers.
* As a side effect of the JUnit conversion, test execution is now fully parallelized and takes significantly less time to complete than the previous Spek-based tests.
* Test coverage has been improved from around 50% to 90%, as Kover was missing a significant number of tested lines of code run by the Spek engine.
* Several minor and/or platform-specific bugs discovered during test conversion have been fixed.
* The coverage report no longer erroneously includes content from the `benchmark` and `snippet` source sets.
* All test utilities are now documented correctly.

## 2.0.2/2.0.3

* This is the first "official" release that has a fully working build system that's nearly identical to the system provided by the original library. It features working benchmarks, tests, and valid documentation and source code JARs.
* No major code changes have been made to the library; as long as you meet the requirements, existing Konf code should work after migrating the `com.uchuhimo.konf` namespace to `io.github.nhubbard.konf`. However, there are some changes made to dependencies and the environment:
* The Konf source code must be built with JDK 21 to work with the publishing plugin we use. It does not require JDK 21 to use once built.
* The new runtime JDK target is 17, at least for now. That makes it target Android 14 (API 34) or higher. I am considering dropping it to Java 11 to allow for greater compatibility with more Android and JDK installations.
* We now use all the most up-to-date versions of our dependencies. That means Kotlin 1.9.22, Gradle 8.6, etc.
* Konf is 100% compatible with Kotlin 2.0 as written; our releases are compiled with the experimental K2 compiler right now.