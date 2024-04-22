package io.github.nhubbard.konf

import io.github.nhubbard.konf.helpers.Valid
import io.github.nhubbard.konf.source.UnknownPathsException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestFailOnUnknownPath {
    //language=Json
    private val source =
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

    @Test
    fun testConfig_whenFeatureDisabled_shouldIgnoreUnknownPaths() {
        val config = Config {
            addSpec(Valid)
        }
        val conf = config.from.disabled(Feature.FAIL_ON_UNKNOWN_PATH).json.string(source)
        assertEquals(conf[Valid.valid], "value1")
    }

    @Test
    fun testConfig_whenFeatureEnabledOnConfig_shouldThrowUnknownPathsException() {
        val config = Config {
            addSpec(Valid)
        }.enable(Feature.FAIL_ON_UNKNOWN_PATH)
        val e = assertCheckedThrows<UnknownPathsException> { config.from.json.string(source) }
        assertEquals(e.paths, listOf("level1.level2.invalid"))
    }

    @Test
    fun testConfig_whenFeatureEnabledOnSource_shouldThrowUnknownPathsException() {
        val config = Config {
            addSpec(Valid)
        }
        val e = assertCheckedThrows<UnknownPathsException> {
            config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).json.string(source)
        }
        assertEquals(e.paths, listOf("level1.level2.invalid"))
    }
}