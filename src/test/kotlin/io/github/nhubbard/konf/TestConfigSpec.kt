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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestConfigSpec {
    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldBeInTheSpec(spec: Spec, item: Item<*>, description: String) {
        assertTrue(item in spec.items)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldHaveTheSpecifiedDescription(spec: Spec, item: Item<*>, description: String) {
        assertEquals(item.description, "description")
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldNameWithoutPrefix(spec: Spec, item: Item<*>, description: String) {
        assertEquals(item.name, "c.int")
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldHaveAValidPath(spec: Spec, item: Item<*>, description: String) {
        assertEquals(item.path, listOf("c", "int"))
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldPointToTheSpec(spec: Spec, item: Item<*>, description: String) {
        assertEquals(item.spec, spec)
    }

    @ParameterizedTest
    @MethodSource("testItemSource")
    fun testConfigSpec_onItem_itShouldHaveTheSpecifiedType(spec: Spec, item: Item<*>, description: String) {
        assertEquals(item.type, TypeFactory.defaultInstance().constructType(Int::class.javaObjectType))
    }

    @Test
    fun testConfigSpec_forRequiredItem_onAddToSpec_itShouldRemainRequired() {
        assertFalse(specForRequired.item.nullable)
        assertTrue(specForRequired.item.isRequired)
        assertFalse(specForRequired.item.isOptional)
        assertFalse(specForRequired.item.isLazy)
        assertTrue(specForRequired.item.asRequiredItem === specForRequired.item)
        assertThrows<ClassCastException> { specForRequired.item.asOptionalItem }
        assertThrows<ClassCastException> { specForRequired.item.asLazyItem }
    }

    @Test
    fun testConfigSpec_forOptionalItem_onAddToSpec_itShouldRemainRequired() {
        assertFalse(specForOptional.item.nullable)
        assertFalse(specForOptional.item.isRequired)
        assertTrue(specForOptional.item.isOptional)
        assertFalse(specForOptional.item.isLazy)
        assertThrows<ClassCastException> { specForOptional.item.asRequiredItem }
        assertTrue(specForOptional.item.asOptionalItem === specForOptional.item)
        assertThrows<ClassCastException> { specForOptional.item.asLazyItem }
    }

    @Test
    fun testConfigSpec_forOptionalItem_onAddToSpec_itShouldContainTheSpecifiedDefaultValue() {
        assertEquals(specForOptional.item.default, 1)
    }

    @Test
    fun testConfigSpec_forLazyItem_onAddToSpec_itShouldStillBeLazy() {
        assertTrue(specForLazy.item.nullable)
        assertFalse(specForLazy.item.isRequired)
        assertFalse(specForLazy.item.isOptional)
        assertTrue(specForLazy.item.isLazy)
        assertThrows<ClassCastException> { specForLazy.item.asRequiredItem }
        assertThrows<ClassCastException> { specForLazy.item.asOptionalItem }
        assertTrue(specForLazy.item.asLazyItem === specForLazy.item)
    }

    @Test
    fun testConfigSpec_forLazyItem_onAddToSpec_shouldContainTheSpecifiedThunk() {
        assertEquals(specForLazy.item.thunk(configForLazy), 2)
    }

    @Test
    fun testConfigSpec_onAddRepeatedItem_shouldThrowRepeatedItemException() {
        val spec = ConfigSpec()
        val item by Spec.dummy.required<Int>()
        spec.addItem(item)
        val e = assertCheckedThrows<RepeatedItemException> { spec.addItem(item) }
        assertEquals(e.name, "item")
    }

    @Test
    fun testConfigSpec_onAddInnerSpec_shouldContainTheAddedSpec() {
        val spec = ConfigSpec()
        val innerSpec: Spec = ConfigSpec()
        spec.addInnerSpec(innerSpec)
        assertEquals(spec.innerSpecs, setOf(innerSpec))
    }

    @Test
    fun testConfigSpec_onAddInnerSpec_shouldThrowRepeatedInnerItemException() {
        val spec = ConfigSpec()
        val innerSpec: Spec = ConfigSpec()
        spec.addInnerSpec(innerSpec)
        val e = assertCheckedThrows<RepeatedInnerSpecException> { spec.addInnerSpec(innerSpec) }
        assertEquals(e.spec, innerSpec)
    }

    @Test
    fun testConfigSpecGetOperation_onGetAnEmptyPath_itShouldReturnItself() {
        assertEquals(specForNested[""], specForNested)
    }

    @Test
    fun testConfigSpecGetOperation_onGetAValidPath_itShouldReturnAConfigSpecWithProperPrefix() {
        assertEquals(specForNested["a"].prefix, "bb")
        assertEquals(specForNested["a.bb"].prefix, "")
    }

    @Test
    fun testConfigSpecGetOperation_onGetAValidPath_itShouldReturnAConfigSpecWithTheProperItemsAndInnerSpecs() {
        specForNested.let {
            assertEquals(it["a"].items, it.items)
            assertEquals(it["a"].innerSpecs, it.innerSpecs)
            assertEquals(it["a.bb.inner"].items, Nested.Inner.items)
            assertEquals(it["a.bb.inner"].innerSpecs.size, 0)
            assertEquals(it["a.bb.inner"].prefix, "")
            assertEquals(it["a.bb.inner2"].items, Nested.Inner2.items)
            assertEquals(it["a.bb.inner2"].innerSpecs.size, 0)
            assertEquals(it["a.bb.inner2"].prefix, "level2")
            assertEquals(it["a.bb.inner3"].items.size, 0)
            assertEquals(it["a.bb.inner3"].innerSpecs.size, 2)
            assertEquals(it["a.bb.inner3"].innerSpecs.toList()[0].prefix, "a")
            assertEquals(it["a.bb.inner3"].innerSpecs.toList()[1].prefix, "b")
        }
    }

    @Test
    fun testConfigSpecGetOperation_onGetAnInvalidPath_itShouldThrowNoSuchPathException() {
        specForNested.let {
            var e = assertCheckedThrows<NoSuchPathException> { it["b"] }
            assertEquals(e.path, "b")
            assertThrows<InvalidPathException> { it["a."] }
            e = assertCheckedThrows<NoSuchPathException> { it["a.b"] }
            assertEquals(e.path, "a.b")
            e = assertCheckedThrows<NoSuchPathException> { it["a.bb.inner4"] }
            assertEquals(e.path, "a.bb.inner4")
        }
    }

    @Test
    fun testConfigSpecPrefixOperation_onPrefixWithEmptyPath_itShouldReturnItself() {
        assertEquals(Prefix("") + specForNested, specForNested)
    }

    @Test
    fun testConfigSpecPrefixOperation_onPrefixWithNonEmptyPath_itShouldReturnAConfigSpecWithProperPrefix() {
        assertEquals((Prefix("c") + specForNested).prefix, "c.a.bb")
        assertEquals((Prefix("c") + specForNested["a.bb"]).prefix, "c")
    }

    @Test
    fun testConfigSpecPrefixOperation_onPrefixWithNonEmptyPath_itShouldReturnAConfigSpecWithTheSameItemsAndInnerSpecs() {
        assertEquals((Prefix("c") + specForNested).items, specForNested.items)
        assertEquals((Prefix("c") + specForNested).innerSpecs, specForNested.innerSpecs)
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
        assertEquals(e.name, "item1")
    }

    @Test
    fun testConfigSpecPlusOperation_onGetItems_shouldContainAllItemsInBothSpecs() {
        assertEquals(addSpec.items, leftSpec.items + rightSpec.items)
    }

    @Test
    fun testConfigSpecPlusOperation_onQualifyItemName_itShouldAddProperPrefix() {
        assertEquals(addSpec.qualify(leftSpec.item1), "a.item1")
        assertEquals(addSpec.qualify(rightSpec.item2), "b.item2")
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
        assertEquals(e.name, "item1")
    }

    @Test
    fun testConfigSpecWithFallbackOp_onGetItems_itShouldContainAllItemsFromFacadeAndFallback() {
        assertEquals(comboSpec.items, fallbackSpec.items + facadeSpec.items)
    }

    @Test
    fun testConfigSpecWithFallbackOp_onQualify_itShouldAddAProperPrefix() {
        assertEquals(comboSpec.qualify(fallbackSpec.item1), "a.item1")
        assertEquals(comboSpec.qualify(facadeSpec.item2), "b.item2")
    }

    @ParameterizedTest
    @MethodSource("prefixInferenceSource")
    fun testConfigSpecPrefixInference_isCorrect(prefix: String, expected: String) {
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
            twoArgumentsOf(configSpecInstance.prefix, ""),
            twoArgumentsOf(AnonymousConfigSpec.spec.prefix, ""),
            twoArgumentsOf(objectExpression.prefix, ""),
            twoArgumentsOf(Uppercase.prefix, "uppercase"),
            twoArgumentsOf(OK.prefix, "ok"),
            twoArgumentsOf(TCPService.prefix, "tcpService"),
            twoArgumentsOf(lowercase.prefix, "lowercase"),
            twoArgumentsOf(SuffixSpec.prefix, "suffix"),
            twoArgumentsOf(OriginalSpec.prefix, "original")
        )
    }
}