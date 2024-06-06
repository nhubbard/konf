# To-Do List

## Small Projects

* [ ] Re-modularize the codebase so that formats are part of separate Gradle subprojects again.
* [x] Fix transient vulnerabilities by substituting second-level dependency versions.

## Medium Projects

* [ ] Figure out how to use newer versions of GraalVM on all platforms for the JS parser.
* [ ] Write tests:
  * [ ] PR #48 (write descriptions as comments in exported config values)
  * [ ] File watcher on Windows/Linux (inc. `InvalidWatchKeyException`)
  * [ ] Description of required, optional, and lazy config items
  * [ ] Other random areas that aren't explicitly listed in this

## Large Projects

* [ ] Move all configuration format parsers to KotlinX Serialization instead of a mix of Jackson modules and various
      third-party parsers.
  * This could help enable use of Konf on Kotlin Multiplatform projects, but it would be extremely challenging.