package io.github.nhubbard.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.nhubbard.konf.source.asSource
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.jupiter.api.assertThrows

object LoadKeysCaseInsensitivelySpec : Spek({
    given("a config") {
        on("by default") {
            val source = mapOf("somekey" to "value").asSource()
            val config = Config().withSource(source)
            it("should load keys case-sensitively") {
                val someKey by config.required<String>()
                assertThrows<UnsetValueException> { someKey.isNotEmpty() }
                val somekey by config.required<String>()
                assertThat(somekey, equalTo("value"))
            }
        }
        on("the feature is disabled") {
            val source = mapOf("somekey" to "value").asSource().disabled(Feature.LOAD_KEYS_CASE_INSENSITIVELY)
            val config = Config().withSource(source)
            it("should load keys case-sensitively") {
                val someKey by config.required<String>()
                assertThrows<UnsetValueException> { someKey.isNotEmpty() }
                val somekey by config.required<String>()
                assertThat(somekey, equalTo("value"))
            }
        }
        on("the feature is enabled on config") {
            val source = mapOf("somekey" to "value").asSource()
            val config = Config().enable(Feature.LOAD_KEYS_CASE_INSENSITIVELY).withSource(source)
            it("should load keys case-insensitively") {
                val someKey by config.required<String>()
                assertThat(someKey, equalTo("value"))
            }
        }
        on("the feature is enabled on source") {
            val source = mapOf("somekey" to "value").asSource().enabled(Feature.LOAD_KEYS_CASE_INSENSITIVELY)
            val config = Config().withSource(source)
            it("should load keys case-insensitively") {
                val someKey by config.required<String>()
                assertThat(someKey, equalTo("value"))
            }
        }
    }
})