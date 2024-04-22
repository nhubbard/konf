package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.asSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestLoadKeysAsLittleCamelCase {
    @Test
    fun testConfig_byDefault_shouldLoadKeysAsLittleCamelCase() {
        val source = mapOf(
            "some_key" to "value",
            "some_key2_" to "value",
            "_some_key3" to "value",
            "SomeKey4" to "value",
            "some_0key5" to "value",
            "some__key6" to "value",
            "some___key7" to "value",
            "some_some_key8" to "value",
            "some key9" to "value",
            "SOMEKey10" to "value"
        ).asSource()
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertEquals(someKey, "value")
        val someKey2 by config.required<String>()
        assertEquals(someKey2, "value")
        val someKey3 by config.required<String>()
        assertEquals(someKey3, "value")
        val someKey4 by config.required<String>()
        assertEquals(someKey4, "value")
        val some0key5 by config.required<String>()
        assertEquals(some0key5, "value")
        val someKey6 by config.required<String>()
        assertEquals(someKey6, "value")
        val someKey7 by config.required<String>()
        assertEquals(someKey7, "value")
        val someSomeKey8 by config.required<String>()
        assertEquals(someSomeKey8, "value")
        val someKey9 by config.required<String>()
        assertEquals(someKey9, "value")
        val someKey10 by config.required<String>()
        assertEquals(someKey10, "value")
    }

    @Test
    fun testConfig_whenFeatureEnabled_shouldLoadKeysAsLittleCamelCase() {
        val source = mapOf("some_key" to "value").asSource().enabled(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertEquals(someKey, "value")
    }

    @Test
    fun testConfig_whenFeatureDisabledOnConfig_shouldLoadKeysWithoutTransformation() {
        val source = mapOf("some_key" to "value").asSource()
        val config = Config().disable(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE).withSource(source)
        val someKey by config.required<String>()
        assertThrows<UnsetValueException> { someKey.isNotEmpty() }
        val someKey2 by config.required<String>()
        assertThrows<UnsetValueException> { assertEquals(someKey2, "value") }
    }

    @Test
    fun testConfig_whenFeatureDisabledOnSource_shouldLoadKeysWithoutTransformation() {
        val source = mapOf("some_key" to "value").asSource().disabled(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE)
        val config = Config().withSource(source)
        val someKey by config.required<String>()
        assertThrows<UnsetValueException> { someKey.isNotEmpty() }
        val someKey1 by config.required<String>()
        assertThrows<UnsetValueException> { assertEquals(someKey1, "value") }
    }
}