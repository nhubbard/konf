/*
 * Copyright (c) 2017-2024 Uchuhimo
 * Copyright (c) 2024-present Nicholas Hubbard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for specific language governing permissions and
 * limitations under the License.
 */

package io.github.nhubbard.konf

import com.fasterxml.jackson.databind.type.TypeFactory
import io.github.nhubbard.konf.helpers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestConfigSpec {
    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldBeInTheSpec(spec: Spec, item: Item<*>, description: String) {
        assertTrue(item in spec.items)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldHaveTheSpecifiedDescription(spec: Spec, item: Item<*>, description: String) {
        assertEquals("description", item.description)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldNameWithoutPrefix(spec: Spec, item: Item<*>, description: String) {
        assertEquals("c.int", item.name)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldHaveAValidPath(spec: Spec, item: Item<*>, description: String) {
        assertEquals(listOf("c", "int"), item.path)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldPointToTheSpec(spec: Spec, item: Item<*>, description: String) {
        assertEquals(spec, item.spec)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldHaveTheSpecifiedType(spec: Spec, item: Item<*>, description: String) {
        assertEquals(TypeFactory.defaultInstance().constructType(Int::class.javaObjectType), item.type)
    }

    @Test
    fun testConfigSpec_forRequiredItem_onAddToSpec_itShouldRemainRequired() {
        assertFalse(specForRequired.item.nullable)
        assertTrue(specForRequired.item.isRequired)
        assertFalse(specForRequired.item.isOptional)
        assertFalse(specForRequired.item.isLazy)
        assertSame(specForRequired.item, specForRequired.item.asRequiredItem)
        assertFailsWith<ClassCastException> { specForRequired.item.asOptionalItem }
        assertFailsWith<ClassCastException> { specForRequired.item.asLazyItem }
    }

    @Test
    fun testConfigSpec_forOptionalItem_onAddToSpec_itShouldRemainRequired() {
        assertFalse(specForOptional.item.nullable)
        assertFalse(specForOptional.item.isRequired)
        assertTrue(specForOptional.item.isOptional)
        assertFalse(specForOptional.item.isLazy)
        assertFailsWith<ClassCastException> { specForOptional.item.asRequiredItem }
        assertSame(specForOptional.item, specForOptional.item.asOptionalItem)
        assertFailsWith<ClassCastException> { specForOptional.item.asLazyItem }
    }

    @Test
    fun testConfigSpec_forOptionalItem_onAddToSpec_itShouldContainTheSpecifiedDefaultValue() {
        assertEquals(1, specForOptional.item.default)
    }

    @Test
    fun testConfigSpec_forLazyItem_onAddToSpec_itShouldStillBeLazy() {
        assertTrue(specForLazy.item.nullable)
        assertFalse(specForLazy.item.isRequired)
        assertFalse(specForLazy.item.isOptional)
        assertTrue(specForLazy.item.isLazy)
        assertFailsWith<ClassCastException> { specForLazy.item.asRequiredItem }
        assertFailsWith<ClassCastException> { specForLazy.item.asOptionalItem }
        assertSame(specForLazy.item, specForLazy.item.asLazyItem)
    }

    @Test
    fun testConfigSpec_forLazyItem_onAddToSpec_shouldContainTheSpecifiedThunk() {
        assertEquals(2, specForLazy.item.thunk(configForLazy))
    }

    @Test
    fun testConfigSpec_onAddRepeatedItem_shouldThrowRepeatedItemException() {
        val spec = ConfigSpec()
        val item by Spec.dummy.required<Int>()
        spec.addItem(item)
        val e = assertCheckedThrows<RepeatedItemException> { spec.addItem(item) }
        assertEquals("item", e.name)
    }

    @Test
    fun testConfigSpec_onAddInnerSpec_shouldContainTheAddedSpec() {
        val spec = ConfigSpec()
        val innerSpec: Spec = ConfigSpec()
        spec.addInnerSpec(innerSpec)
        assertEquals(setOf(innerSpec), spec.innerSpecs)
    }

    @Test
    fun testConfigSpec_onAddInnerSpec_shouldThrowRepeatedInnerItemException() {
        val spec = ConfigSpec()
        val innerSpec: Spec = ConfigSpec()
        spec.addInnerSpec(innerSpec)
        val e = assertCheckedThrows<RepeatedInnerSpecException> { spec.addInnerSpec(innerSpec) }
        assertEquals(innerSpec, e.spec)
    }

    @Test
    fun testConfigSpecGetOperation_onGetAnEmptyPath_itShouldReturnItself() {
        assertEquals(specForNested, specForNested[""])
    }

    @Test
    fun testConfigSpecGetOperation_onGetAValidPath_itShouldReturnAConfigSpecWithProperPrefix() {
        assertEquals("bb", specForNested["a"].prefix)
        assertEquals("", specForNested["a.bb"].prefix)
    }

    @Test
    fun testConfigSpecGetOperation_onGetAValidPath_itShouldReturnAConfigSpecWithTheProperItemsAndInnerSpecs() {
        specForNested.let {
            assertEquals(it.items, it["a"].items)
            assertEquals(it.innerSpecs, it["a"].innerSpecs)
            assertEquals(Nested.Inner.items, it["a.bb.inner"].items)
            assertEquals(0, it["a.bb.inner"].innerSpecs.size)
            assertEquals("", it["a.bb.inner"].prefix)
            assertEquals(Nested.Inner2.items, it["a.bb.inner2"].items)
            assertEquals(0, it["a.bb.inner2"].innerSpecs.size)
            assertEquals("level2", it["a.bb.inner2"].prefix)
            assertEquals(0, it["a.bb.inner3"].items.size)
            assertEquals(2, it["a.bb.inner3"].innerSpecs.size)
            assertEquals("a", it["a.bb.inner3"].innerSpecs.toList()[0].prefix)
            assertEquals("b", it["a.bb.inner3"].innerSpecs.toList()[1].prefix)
        }
    }

    @Test
    fun testConfigSpecGetOperation_onGetAnInvalidPath_itShouldThrowNoSuchPathException() {
        specForNested.let {
            var e = assertCheckedThrows<NoSuchPathException> { it["b"] }
            assertEquals("b", e.path)
            assertFailsWith<InvalidPathException> { it["a."] }
            e = assertCheckedThrows<NoSuchPathException> { it["a.b"] }
            assertEquals("a.b", e.path)
            e = assertCheckedThrows<NoSuchPathException> { it["a.bb.inner4"] }
            assertEquals("a.bb.inner4", e.path)
        }
    }

    @Test
    fun testConfigSpecPrefixOperation_onPrefixWithEmptyPath_itShouldReturnItself() {
        assertEquals(specForNested, Prefix("") + specForNested)
    }

    @Test
    fun testConfigSpecPrefixOperation_onPrefixWithNonEmptyPath_itShouldReturnAConfigSpecWithProperPrefix() {
        assertEquals("c.a.bb", (Prefix("c") + specForNested).prefix)
        assertEquals("c", (Prefix("c") + specForNested["a.bb"]).prefix)
    }

    @Test
    fun testConfigSpecPrefixOperation_onPrefixWithNonEmptyPath_itShouldReturnAConfigSpecWithTheSameItemsAndInnerSpecs() {
        assertEquals(specForNested.items, (Prefix("c") + specForNested).items)
        assertEquals(specForNested.innerSpecs, (Prefix("c") + specForNested).innerSpecs)
    }

    @Test
    fun testConfigSpecPlusOperation_onAddValidItem_itShouldContainItemInFacadeSpec() {
        val item by Spec.dummy.required<Int>()
        addSpec.addItem(item)
        assertTrue(item in addSpec.items)
        assertTrue(item in rightSpec.items)
    }

    @Test
    fun testConfigSpecPlusOperation_onAddRepeatedItem_shouldThrowRepeatedItemException() {
        val e = assertCheckedThrows<RepeatedItemException> { addSpec.addItem(leftSpec.item1) }
        assertEquals("item1", e.name)
    }

    @Test
    fun testConfigSpecPlusOperation_onGetItems_shouldContainAllItemsInBothSpecs() {
        assertEquals(leftSpec.items + rightSpec.items, addSpec.items)
    }

    @Test
    fun testConfigSpecPlusOperation_onQualifyItemName_itShouldAddProperPrefix() {
        assertEquals("a.item1", addSpec.qualify(leftSpec.item1))
        assertEquals("b.item2", addSpec.qualify(rightSpec.item2))
    }

    @Test
    fun testConfigSpecWithFallbackOp_onAddValidItem_itShouldPutItemInFallbackSpec() {
        val item by Spec.dummy.required<Int>()
        comboSpec.addItem(item)
        assertTrue(item in comboSpec.items)
        assertTrue(item in facadeSpec.items)
    }

    @Test
    fun testConfigSpecWithFallbackOp_onAddRepeatedItem_itShouldThrowRepeatedItemException() {
        val e = assertCheckedThrows<RepeatedItemException> { comboSpec.addItem(fallbackSpec.item1) }
        assertEquals("item1", e.name)
    }

    @Test
    fun testConfigSpecWithFallbackOp_onGetItems_itShouldContainAllItemsFromFacadeAndFallback() {
        assertEquals(fallbackSpec.items + facadeSpec.items, comboSpec.items)
    }

    @Test
    fun testConfigSpecWithFallbackOp_onQualify_itShouldAddAProperPrefix() {
        assertEquals("a.item1", comboSpec.qualify(fallbackSpec.item1))
        assertEquals("b.item2", comboSpec.qualify(facadeSpec.item2))
    }

    @ParameterizedTest
    @MethodSource("prefixInferenceSource")
    fun testConfigSpecPrefixInference_isCorrect(expected: String, prefix: String) {
        assertEquals(expected, prefix)
    }

    companion object {
        object SpecForRequired: ConfigSpec("a.b") {
            val item by required<Int>("c.int", "description")
        }

        object SpecForOptional: ConfigSpec("a.b") {
            val item by optional(1, "c.int", "description")
        }

        object SpecForLazy: ConfigSpec("a.b") {
            val item by lazy<Int?>("c.int", "description") { 2 }
        }

        object LeftSpec: ConfigSpec("a") {
            val item1 by required<Int>()
        }

        object RightSpec: ConfigSpec("b") {
            val item2 by required<Int>()
        }

        @JvmStatic val specForRequired = SpecForRequired
        @JvmStatic val specForOptional = SpecForOptional
        @JvmStatic val specForLazy = SpecForLazy
        @JvmStatic val configForLazy = Config { addSpec(specForLazy) }
        @JvmStatic val specForNested = Nested
        @JvmStatic val leftSpec = LeftSpec
        @JvmStatic val rightSpec = RightSpec
        @JvmStatic val addSpec = leftSpec + rightSpec
        @JvmStatic val fallbackSpec = LeftSpec
        @JvmStatic val facadeSpec = RightSpec
        @JvmStatic val comboSpec = facadeSpec.withFallback(fallbackSpec)
        @JvmStatic val configSpecInstance = ConfigSpec()
        @JvmStatic val objectExpression = object : ConfigSpec() {}

        @JvmStatic
        fun testItemSource(): Stream<Arguments> = argumentsOf(
            threeArgumentsOf(specForRequired, specForRequired.item, "a required item"),
            threeArgumentsOf(specForOptional, specForOptional.item, "an optional item"),
            threeArgumentsOf(specForLazy, specForLazy.item, "a lazy item")
        )

        @JvmStatic
        fun prefixInferenceSource(): Stream<Arguments> = argumentsOf(
            twoArgumentsOf("", configSpecInstance.prefix),
            twoArgumentsOf("", AnonymousConfigSpec.spec.prefix),
            twoArgumentsOf("", objectExpression.prefix),
            twoArgumentsOf("uppercase", Uppercase.prefix),
            twoArgumentsOf("ok", OK.prefix),
            twoArgumentsOf("tcpService", TCPService.prefix),
            twoArgumentsOf("lowercase", lowercase.prefix),
            twoArgumentsOf("suffix", SuffixSpec.prefix),
            twoArgumentsOf("original", OriginalSpec.prefix)
        )
    }
}