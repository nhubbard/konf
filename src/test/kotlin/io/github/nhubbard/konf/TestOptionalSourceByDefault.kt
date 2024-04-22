package io.github.nhubbard.konf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestOptionalSourceByDefault {
    @Test
    fun testAConfig_whenTheFeatureIsDisabled_shouldThrowExceptionWhenFileDoesNotExist() {
        val config = Config().disable(Feature.OPTIONAL_SOURCE_BY_DEFAULT)
        assertThrows<FileNotFoundException> { config.from.file("not_existed.json") }
    }

    @Test
    fun testAConfig_whenTheFeatureIsEnabled_shouldLoadEmptySource() {
        val config = Config().enable(Feature.OPTIONAL_SOURCE_BY_DEFAULT)
        config.from.mapped {
            assertEquals(it.tree.children, mutableMapOf<String, TreeNode>())
            it
        }.file("not_existed.json")
        config.from.mapped {
            assertEquals(it.tree.children, mutableMapOf<String, TreeNode>())
            it
        }.json.file("not_existed.json")
    }
}