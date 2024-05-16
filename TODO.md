# To-Do List

## Small Projects

* [ ] Re-modularize the codebase so that formats are part of separate Gradle subprojects again.
* [x] Fix transient vulnerabilities by substituting second-level dependency versions.

## Medium Projects

* [ ] Figure out how to use newer versions of GraalVM on all platforms for the JS parser.
* [ ] Finish and test the comment writer functionality from PR 48.
* [ ] Add test coverage for any other code that isn't already covered.

## Large Projects

* [ ] Move all configuration format parsers to KotlinX Serialization instead of a mix of Jackson modules and various
      third-party parsers.
  * This would enable use of Konf on Kotlin Multiplatform projects!