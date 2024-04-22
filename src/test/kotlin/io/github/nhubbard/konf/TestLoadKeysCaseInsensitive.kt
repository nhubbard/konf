package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.asSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestLoadKeysCaseInsensitive {
    @Test
    fun testConfig_byDefault_shouldLoadKeysCaseSensitive() {
        val source = mapOf("somekey" to "value").asSource()
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertThrows<UnsetValueException> { someKey.isNotEmpty() }
        val somekey by config.required<String>()
        assertEquals(somekey, "value")
    }

    @Test
    fun testConfig_whenFeatureDisabled_shouldLoadKeysCaseSensitive() {
        val source = mapOf("somekey" to "value").asSource().disabled(Feature.LOAD_KEYS_CASE_INSENSITIVELY)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertThrows<UnsetValueException> { someKey.isNotEmpty() }
        val somekey by config.required<String>()
        assertEquals(somekey, "value")
    }

    @Test
    fun testConfig_whenFeatureEnabledByConfig_shouldLoadKeysCaseInsensitive() {
        val source = mapOf("somekey" to "value").asSource()
        val config = Config().enable(Feature.LOAD_KEYS_CASE_INSENSITIVELY).withSource(source)
        val someKey by config.required<String>()
        assertEquals(someKey, "value")
    }

    @Test
    fun testConfig_whenFeatureEnabledBySource_shouldLoadKeysCaseInsensitive() {
        val source = mapOf("somekey" to "value").asSource().enabled(Feature.LOAD_KEYS_CASE_INSENSITIVELY)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertEquals(someKey, "value")
    }
}