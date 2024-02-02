dependencies {
    api(project(":konf-core"))
    implementation("io.hotmoka", "toml4j", Versions.toml4j)

    testImplementation(testFixtures(project(":konf-core")))
}
