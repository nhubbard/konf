package io.github.nhubbard.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.nhubbard.konf.source.asSource
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.assertThrows

object LoadKeysAsLittleCamelCaseSpec : Spek({
    given("a config") {
        on("by default") {
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
            it("should load keys as little camel case") {
                val someKey by config.required<String>()
                assertThat(someKey, equalTo("value"))
                val someKey2 by config.required<String>()
                assertThat(someKey2, equalTo("value"))
                val someKey3 by config.required<String>()
                assertThat(someKey3, equalTo("value"))
                val someKey4 by config.required<String>()
                assertThat(someKey4, equalTo("value"))
                val some0key5 by config.required<String>()
                assertThat(some0key5, equalTo("value"))
                val someKey6 by config.required<String>()
                assertThat(someKey6, equalTo("value"))
                val someKey7 by config.required<String>()
                assertThat(someKey7, equalTo("value"))
                val someSomeKey8 by config.required<String>()
                assertThat(someSomeKey8, equalTo("value"))
                val someKey9 by config.required<String>()
                assertThat(someKey9, equalTo("value"))
                val someKey10 by config.required<String>()
                assertThat(someKey10, equalTo("value"))
            }
        }
        on("the feature is enabled") {
            val source = mapOf("some_key" to "value").asSource().enabled(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE)
            val config = Config().withSource(source)
            it("should load keys as little camel case") {
                val someKey by config.required<String>()
                assertThat(someKey, equalTo("value"))
            }
        }
        on("the feature is disabled on config") {
            val source = mapOf("some_key" to "value").asSource()
            val config = Config().disable(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE).withSource(source)
            it("should load keys as usual") {
                val someKey by config.required<String>()
                assertThrows<UnsetValueException> { someKey.isNotEmpty() }
                val someKey2 by config.required<String>()
                assertThat(someKey2, equalTo("value"))
            }
        }
        on("the feature is disabled on source") {
            val source = mapOf("some_key" to "value").asSource().disabled(Feature.LOAD_KEYS_AS_LITTLE_CAMEL_CASE)
            val config = Config().withSource(source)
            it("should load keys as usual") {
                val someKey by config.required<String>()
                assertThrows<UnsetValueException> { someKey.isNotEmpty() }
                val someKey1 by config.required<String>()
                assertThat(someKey1, equalTo("value"))
            }
        }
    }
})