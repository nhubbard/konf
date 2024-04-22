package io.github.nhubbard.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.throws
import io.github.nhubbard.konf.helpers.Valid
import io.github.nhubbard.konf.source.UnknownPathsException
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object FailOnUnknownPathSpec : Spek({
    //language=Json
    val source =
        """
        {
            "level1": {
              "level2": {
                "valid": "value1",
                "invalid": "value2"
              }
            }
        }
        """.trimIndent()
    given("a config") {
        on("the feature is disabled") {
            val config = Config {
                addSpec(Valid)
            }
            it("should ignore unknown paths") {
                val conf = config.from.disabled(Feature.FAIL_ON_UNKNOWN_PATH).json.string(source)
                assertThat(conf[Valid.valid], equalTo("value1"))
            }
        }
        on("the feature is enabled on config") {
            val config = Config {
                addSpec(Valid)
            }.enable(Feature.FAIL_ON_UNKNOWN_PATH)
            it("should throws UnknownPathsException and reports the unknown paths") {
                assertThat(
                    { config.from.json.string(source) },
                    throws(
                        has(
                            UnknownPathsException::paths,
                            equalTo(listOf("level1.level2.invalid"))
                        )
                    )
                )
            }
        }
        on("the feature is enabled on source") {
            val config = Config {
                addSpec(Valid)
            }
            it("should throws UnknownPathsException and reports the unknown paths") {
                assertThat(
                    {
                        config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).json.string(source)
                    },
                    throws(
                        has(
                            UnknownPathsException::paths,
                            equalTo(listOf("level1.level2.invalid"))
                        )
                    )
                )
            }
        }
    }
})