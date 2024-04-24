package io.github.nhubbard.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.FlattenDefaultLoadersConfig
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object DefaultLoadersWithFlattenEnvSpec : Spek({
    given("a loader") {
        on("load as flatten format from system environment") {
            val config = Config {
                addSpec(FlattenDefaultLoadersConfig)
            }.from.env(nested = false)
            it("should return a config which contains value from system environment") {
                assertThat(config[FlattenDefaultLoadersConfig.SOURCE_TEST_TYPE], equalTo("env"))
            }
        }
    }
})