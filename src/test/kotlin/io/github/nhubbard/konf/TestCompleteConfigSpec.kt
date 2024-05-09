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

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.nhubbard.konf.helpers.*
import io.github.nhubbard.konf.source.LoadException
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.base.asKVSource
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Stream
import kotlin.concurrent.withLock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class TestCompleteConfigSpec {
    val spec = NetworkBuffer
    val size = NetworkBuffer.size
    private val maxSize = NetworkBuffer.maxSize
    val name = NetworkBuffer.name
    val type = NetworkBuffer.type
    private val offset = NetworkBuffer.offset

    private val lock = ReentrantLock()

    private val invalidItem by ConfigSpec("invalid").required<Int>()
    private val invalidItemName = "invalid.invalidItem"

    private fun String.qualify(name: String): String = if (isEmpty()) name else "$this.$name"

    private fun prefixToMap(prefix: String, value: Map<String, Any>): Map<String, Any> {
        return when {
            prefix.isEmpty() -> value
            prefix.contains('.') ->
                mapOf<String, Any>(
                    prefix.substring(0, prefix.indexOf('.')) to
                            prefixToMap(prefix.substring(prefix.indexOf('.') + 1), value)
                )
            else -> mapOf(prefix to value)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testFeatureOperation_enableFeature_shouldLetTheFeatureBeEnabled(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.enable(Feature.FAIL_ON_UNKNOWN_PATH)
        assertTrue(subject.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testFeatureOperation_disableFeature_shouldLetTheFeatureBeDisabled(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.disable(Feature.FAIL_ON_UNKNOWN_PATH)
        assertFalse(subject.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testFeatureOperation_byDefault_shouldUseTheFeatureDefaultSetting(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(
            subject.isEnabled(Feature.FAIL_ON_UNKNOWN_PATH),
            Feature.FAIL_ON_UNKNOWN_PATH.enabledByDefault
        )
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSubscribeOperation_loadSourceWhenSubscriberIsDefined(prefix: String, provider: () -> Config) {
        val subject = provider()
        var loadFunction: (source: Source) -> Unit = {}
        var counter = 0
        val config = subject.withLoadTrigger("") { _, load ->
            loadFunction = load
        }.withLayer()
        val source = mapOf(
            prefix.qualify(type.name) to NetworkBuffer.Type.ON_HEAP
        ).asKVSource()
        val handler1 = config.beforeLoad {
            counter += 1
            assertEquals(it, source)
            assertEquals(config[type], NetworkBuffer.Type.OFF_HEAP)
        }
        val handler2 = config.beforeLoad {
            counter += 1
            assertEquals(it, source)
            assertEquals(config[type], NetworkBuffer.Type.OFF_HEAP)
        }
        val handler3 = config.afterLoad {
            counter += 1
            assertEquals(it, source)
            assertEquals(config[type], NetworkBuffer.Type.ON_HEAP)
        }
        val handler4 = config.afterLoad {
            counter += 1
            assertEquals(it, source)
            assertEquals(config[type], NetworkBuffer.Type.ON_HEAP)
        }
        loadFunction(source)
        handler1.close()
        handler2.close()
        handler3.close()
        handler4.close()
        assertEquals(counter, 4)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addOrthogonalSpec_shouldContainItemsInNewSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        val newSpec = object : ConfigSpec(spec.prefix) {
            val minSize by optional(1)
        }
        val config = subject.withSource(mapOf(newSpec.qualify(newSpec.minSize) to 2).asKVSource())
        config.addSpec(newSpec)
        assertTrue(newSpec.minSize in config)
        assertTrue(newSpec.qualify(newSpec.minSize) in config)
        assertEquals(config.nameOf(newSpec.minSize), newSpec.qualify(newSpec.minSize))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addOrthogonalSpec_shouldContainNewSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        val newSpec = object : ConfigSpec(spec.prefix) {
            val minSize by optional(1)
        }
        val config = subject.withSource(mapOf(newSpec.qualify(newSpec.minSize) to 2).asKVSource())
        config.addSpec(newSpec)
        assertTrue(newSpec in config.specs)
        assertTrue(spec in config.specs)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addOrthogonalSpec_shouldLoadValuesFromExistingSourceForItemsInNewSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        val newSpec = object : ConfigSpec(spec.prefix) {
            val minSize by optional(1)
        }
        val config = subject.withSource(mapOf(newSpec.qualify(newSpec.minSize) to 2).asKVSource())
        config.addSpec(newSpec)
        assertEquals(config[newSpec.minSize], 2)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addSpecWithInnerSpecs_shouldContainItemsInNewSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service)
        assertTrue(Service.name in subject)
        assertTrue(Service.UI.host in subject)
        assertTrue(Service.UI.port in subject)
        assertTrue(Service.Backend.host in subject)
        assertTrue(Service.Backend.port in subject)
        assertTrue(Service.Backend.Login.user in subject)
        assertTrue(Service.Backend.Login.password in subject)
        assertTrue("service.name" in subject)
        assertTrue("service.ui.host" in subject)
        assertTrue("service.ui.port" in subject)
        assertTrue("service.backend.host" in subject)
        assertTrue("service.backend.port" in subject)
        assertTrue("service.backend.login.user" in subject)
        assertTrue("service.backend.login.password" in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addSpecWithInnerSpecs_shouldContainNewSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service)
        assertTrue(Service in subject.specs)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addSpecWithInnerSpecs_shouldNotContainInnerSpecsInNewSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service)
        assertFalse(Service.UI in subject.specs)
        assertFalse(Service.Backend in subject.specs)
        assertFalse(Service.Backend.Login in subject.specs)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addNestedSpec_shouldContainItemsInTheNestedSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service.Backend)
        assertTrue(Service.Backend.host in subject)
        assertTrue(Service.Backend.port in subject)
        assertTrue(Service.Backend.Login.user in subject)
        assertTrue(Service.Backend.Login.password in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addNestedSpec_shouldNotContainItemsInTheOuterSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service.Backend)
        assertFalse(Service.name in subject)
        assertFalse(Service.UI.host in subject)
        assertFalse(Service.UI.port in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addNestedSpec_shouldContainTheNestedSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service.Backend)
        assertTrue(Service.Backend in subject.specs)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addNestedSpec_shouldNotContainTheOuterSpecOrInnerSpecsInTheNestedSpec(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.addSpec(Service.Backend)
        assertFalse(Service in subject.specs)
        assertFalse(Service.UI in subject.specs)
        assertFalse(Service.Backend.Login in subject.specs)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addRepeatedItem_shouldThrowRepeatedItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<RepeatedItemException> {
            subject.addSpec(spec)
        }
        assertEquals(e.name, spec.qualify(size))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addRepeatedName_shouldThrowNameConflictException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val newSpec = ConfigSpec(prefix).apply {
            val size by required<Int>()
        }
        assertThrows<NameConflictException> {
            subject.addSpec(newSpec)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addConflictPrefixedName_shouldThrowNameConflictException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val newSpec = ConfigSpec().apply {
            val buffer by required<Int>()
        }
        assertThrows<NameConflictException> {
            subject.addSpec(
                newSpec.withPrefix(prefix.toPath().let { it.subList(0, it.size - 1) }.name)
            )
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddSpecOperation_addConflictWhereExistingNameIsPrefixOfExisting_shouldThrowNameConflictException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val newSpec = ConfigSpec(prefix.qualify(type.name)).apply {
            val subType by required<Int>()
        }
        assertThrows<NameConflictException> {
            subject.addSpec(newSpec)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddItemOperation_addOrthogonalItem_shouldContainItem(prefix: String, provider: () -> Config) {
        val subject = provider()
        val minSize by Spec.dummy.optional(1)
        val config = subject.withSource(mapOf(prefix.qualify(minSize.name) to 2).asKVSource())
        config.addItem(minSize, prefix)
        assertTrue(minSize in config)
        assertTrue(prefix.qualify(minSize.name) in config)
        assertEquals(config.nameOf(minSize), prefix.qualify(minSize.name))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddItemOperation_addOrthogonalItem_shouldLoadValuesFromExistingSourcesForItem(prefix: String, provider: () -> Config) {
        val subject = provider()
        val minSize by Spec.dummy.optional(1)
        val config = subject.withSource(mapOf(prefix.qualify(minSize.name) to 2).asKVSource())
        config.addItem(minSize, prefix)
        assertEquals(config[minSize], 2)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddItemOperation_addRepeatedItem_shouldThrowRepeatedItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<RepeatedItemException> {
            subject.addItem(size, prefix)
        }
        assertEquals(e.name, prefix.qualify(size.name))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddItemOperation_addRepeatedName_shouldThrowNameConflictException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val size by Spec.dummy.required<Int>()
        assertThrows<NameConflictException> {
            subject.addItem(size, prefix)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddItemOperation_addConflictPrefixName_shouldThrowNameConflictException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val buffer by Spec.dummy.required<Int>()
        assertThrows<NameConflictException> {
            subject.addItem(
                buffer,
                prefix.toPath().let { it.subList(0, it.size - 1) }.name
            )
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testAddItemOperation_addConflictExistingName_shouldThrowNameConflictException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val subType by Spec.dummy.required<Int>()
        assertThrows<NameConflictException> {
            subject.addItem(subType, prefix.qualify(type.name))
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testIterateItemsInConfig_shouldCoverAllItemsInConfig(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(subject.items.toSet(), spec.items.toSet())
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testIterateNameOfItemsInConfig_shouldCoverAllItemsInConfig(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(subject.nameOfItems.toSet(), spec.items.map { prefix.qualify(it.name) }.toSet())
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToMap_shouldNotContainUnsetItemsInMap(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(
            subject.toMap(),
            mapOf<String, Any>(
                prefix.qualify(name.name) to "buffer",
                prefix.qualify(type.name) to NetworkBuffer.Type.OFF_HEAP.name,
                prefix.qualify(offset.name) to "null"
            )
        )
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToMap_shouldContainCorrespondingItemsInMap(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 4
            subject[type] = NetworkBuffer.Type.ON_HEAP
            subject[offset] = 0
            val map = subject.toMap()
            assertEquals(
                map,
                mapOf(
                    prefix.qualify(size.name) to 4,
                    prefix.qualify(maxSize.name) to 8,
                    prefix.qualify(name.name) to "buffer",
                    prefix.qualify(type.name) to NetworkBuffer.Type.ON_HEAP.name,
                    prefix.qualify(offset.name) to 0
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToMap_shouldRecoverAllItemsWhenReloadedFromMap(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 4
            subject[type] = NetworkBuffer.Type.ON_HEAP
            subject[offset] = 0
            val map = subject.toMap()
            val newConfig = Config { addSpec(spec[spec.prefix].withPrefix(prefix)) }.from.map.kv(map)
            assertEquals(newConfig[size], 4)
            assertEquals(newConfig[maxSize], 8)
            assertEquals(newConfig[name], "buffer")
            assertEquals(newConfig[type], NetworkBuffer.Type.ON_HEAP)
            assertEquals(newConfig[offset], 0)
            assertEquals(newConfig.toMap(), subject.toMap())
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToHierarchicalMap_shouldNotContainUnsetItemsInMap(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(
            subject.toHierarchicalMap(),
            prefixToMap(
                prefix,
                mapOf(
                    "name" to "buffer",
                    "type" to NetworkBuffer.Type.OFF_HEAP.name,
                    "offset" to "null"
                )
            )
        )
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToHierarchicalMap_shouldContainCorrespondingItemsInMap(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 4
            subject[type] = NetworkBuffer.Type.ON_HEAP
            subject[offset] = 0
            val map = subject.toHierarchicalMap()
            assertEquals(
                map,
                prefixToMap(
                    prefix,
                    mapOf(
                        "size" to 4,
                        "maxSize" to 8,
                        "name" to "buffer",
                        "type" to NetworkBuffer.Type.ON_HEAP.name,
                        "offset" to 0
                    )
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToHierarchicalMap_shouldRecoverAllItemsWhenReloadedFromMap(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 4
            subject[type] = NetworkBuffer.Type.ON_HEAP
            subject[offset] = 0
            val map = subject.toHierarchicalMap()
            val newConfig = Config { addSpec(spec[spec.prefix].withPrefix(prefix)) }.from.map.hierarchical(map)
            assertEquals(newConfig[size], 4)
            assertEquals(newConfig[maxSize], 8)
            assertEquals(newConfig[name], "buffer")
            assertEquals(newConfig[type], NetworkBuffer.Type.ON_HEAP)
            assertEquals(newConfig[offset], 0)
            assertEquals(newConfig.toMap(), subject.toMap())
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testObjectMethods_shouldNotEqualToObjectOfOtherClass(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertFalse(subject.equals(1))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testObjectMethods_shouldEqualToItself(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(subject, subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testObjectMethods_shouldConvertToStringInMapLikeFormat(prefix: String, provider: () -> Config) {
        val subject = provider()
        val map = mapOf(
            prefix.qualify(name.name) to "buffer",
            prefix.qualify(type.name) to NetworkBuffer.Type.OFF_HEAP.name,
            prefix.qualify(offset.name) to "null"
        )
        assertEquals(subject.toString(), "Config(items=$map)")
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testLockConfig_shouldBeLocked(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.lock {}
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithValidItem_shouldReturnCorrespondingValue(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(subject[name], "buffer")
        assertTrue(name in subject)
        assertNull(subject[offset])
        assertTrue(offset in subject)
        assertNull(subject.getOrNull(maxSize))
        assertTrue(maxSize in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithInvalidItem_shouldThrowNoSuchItemExceptionWhenUsingGet(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> { subject[invalidItem] }
        assertEquals(e.name, invalidItem.asName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithInvalidItem_shouldReturnNullWhenUsingGetOrNull(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertNull(subject.getOrNull(invalidItem))
        assertTrue(invalidItem !in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithValidName_shouldReturnCorrespondingValue(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(subject(prefix.qualify("name")), "buffer")
        assertEquals(subject.getOrNull<String>(prefix.qualify("name")), "buffer")
        assertTrue(prefix.qualify("name") in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithValidNameWithTrailingWhitespace_shouldReturnCorrespondingValue(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertEquals(subject(prefix.qualify("name ")), "buffer")
        assertEquals(subject.getOrNull<String>(prefix.qualify("name  ")), "buffer")
        assertTrue(prefix.qualify("name   ") in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithInvalidName_shouldThrowNoSuchItemExceptionWhenUsingGet(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> {
            subject<String>(prefix.qualify(invalidItem.name))
        }
        assertEquals(e.name, prefix.qualify(invalidItem.name))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getWithInvalidName_shouldReturnNullWhenUsingGetOrNull(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertNull(subject.getOrNull<String>(prefix.qualify(invalidItem.name)))
        assertTrue(prefix.qualify(invalidItem.name) !in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_getUnsetItem_shouldThrowUnsetValueException(prefix: String, provider: () -> Config) {
        val subject = provider()
        var e = assertCheckedThrows<UnsetValueException> { subject[size] }
        assertEquals(e.name, size.asName)
        e = assertCheckedThrows<UnsetValueException> { subject[maxSize] }
        assertEquals(e.name, size.asName)
        assertTrue(size in subject)
        assertTrue(maxSize in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_withLazyItemThatReturnsNullWhenTheTypeIsNullable_shouldReturnNull(prefix: String, provider: () -> Config) {
        val subject = provider()
        val lazyItem by Spec.dummy.lazy<Int?> { null }
        subject.addItem(lazyItem, prefix)
        assertNull(subject[lazyItem])
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testGetOperation_withLazyItemThatReturnsNullWhenTheTypeIsNotNullable_shouldThrowInvalidLazySetException(prefix: String, provider: () -> Config) {
        val subject = provider()
        @Suppress("UNCHECKED_CAST")
        val thunk = { _: ItemContainer -> null } as (ItemContainer) -> Int
        val lazyItem by Spec.dummy.lazy(thunk = thunk)
        subject.addItem(lazyItem, prefix)
        assertThrows<InvalidLazySetException> { subject[lazyItem] }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithValidItemWhenCorrespondingValueIsUnset_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject[size] = 1024
        assertEquals(subject[size], 1024)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithValidItemWhenCorrespondingValueExists_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject[name] = "newName"
        assertEquals(subject[name], "newName")
        subject[offset] = 0
        assertEquals(subject[offset], 0)
        subject[offset] = null
        assertNull(subject[offset])
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_rawSetWithValidItem_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject.rawSet(size, 2048)
            assertEquals(subject[size], 2048)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithValidItemWhenCorrespondingValueIsLazy_fromLazyToNormal(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 1024
            assertEquals(subject[maxSize], subject[size] * 2)
            subject[maxSize] = 0
            assertEquals(subject[maxSize], 0)
            subject[size] = 2048
            assertNotEquals(subject[maxSize], subject[size] * 2)
            assertEquals(subject[maxSize], 0)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithInvalidItem_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            val e = assertCheckedThrows<NoSuchItemException> { subject[invalidItem] = 1024 }
            assertEquals(e.name, invalidItem.asName)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithValidName_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[prefix.qualify("size")] = 1024
            assertEquals(subject[size], 1024)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithValidNameWhichContainsTrailingWhitespaces_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[prefix.qualify("size  ")] = 1024
            assertEquals(subject[size], 1024)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithInvalidName_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> { subject[invalidItemName] = 1024 }
        assertEquals(e.name, invalidItemName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWithIncorrectTypeOfValue_shouldThrowClassCastException(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertThrows<ClassCastException> { subject[prefix.qualify(size.name)] = "1024" }
        assertThrows<ClassCastException> { subject[prefix.qualify(size.name)] = null }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWhenBeforeSetSubscriberIsDefined(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            val childConfig = subject.withLayer()
            subject[size] = 1
            var counter = 0
            val handler1 = childConfig.beforeSet { item, value ->
                counter += 1
                assertEquals(item, size)
                assertEquals(value, 2)
                assertEquals(childConfig[size], 1)
            }
            val handler2 = childConfig.beforeSet { item, value ->
                counter += 1
                assertEquals(item, size)
                assertEquals(value, 2)
                assertEquals(childConfig[size], 1)
            }
            val handler3 = size.beforeSet { _, value ->
                counter += 1
                assertEquals(value, 2)
                assertEquals(childConfig[size], 1)
            }
            val handler4 = size.beforeSet { _, value ->
                counter += 1
                assertEquals(value, 2)
                assertEquals(childConfig[size], 1)
            }
            subject[size] = 2
            handler1.close()
            handler2.close()
            handler3.close()
            handler4.close()
            assertEquals(counter, 4)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWhenAfterSetSubscriberIsDefined(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            val childConfig = subject.withLayer()
            subject[size] = 1
            var counter = 0
            val handler1 = childConfig.afterSet { item, value ->
                counter += 1
                assertEquals(item, size)
                assertEquals(value, 2)
                assertEquals(childConfig[size], 2)
            }
            val handler2 = childConfig.afterSet { item, value ->
                counter += 1
                assertEquals(item, size)
                assertEquals(value, 2)
                assertEquals(childConfig[size], 2)
            }
            val handler3 = size.afterSet { _, value ->
                counter += 1
                assertEquals(value, 2)
                assertEquals(childConfig[size], 2)
            }
            val handler4 = size.afterSet { _, value ->
                counter += 1
                assertEquals(value, 2)
                assertEquals(childConfig[size], 2)
            }
            subject[size] = 2
            handler1.close()
            handler2.close()
            handler3.close()
            handler4.close()
            assertEquals(counter, 4)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWhenOnSetSubscriberIsDefined(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            var counter = 0
            size.onSet { counter += 1 }.use {
                subject[size] = 1
                subject[size] = 16
                subject[size] = 256
                subject[size] = 1024
                assertEquals(counter, 4)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_setWhenMultipleOnSetSubscribersAreDefined(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            var counter = 0
            size.onSet { counter += 1 }.use {
                size.onSet { counter += 2 }.use {
                    subject[size] = 1
                    subject[size] = 16
                    subject[size] = 256
                    subject[size] = 1024
                    assertEquals(counter, 12)
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_lazySetWithValidItem_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject.lazySet(maxSize) { it[size] * 4 }
            subject[size] = 1024
            assertEquals(subject[maxSize], subject[size] * 4)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_lazySetWithInvalidItem_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> { subject.lazySet(invalidItem) { 1024 } }
        assertEquals(e.name, invalidItem.asName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_lazySetWithValidName_shouldContainTheSpecifiedValue(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject.lazySet(prefix.qualify(maxSize.name)) { it[size] * 4 }
            subject[size] = 1024
            assertEquals(subject[maxSize], subject[size] * 4)
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_lazySetWithValidNameThatContainsTrailingWhitespaces(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.lazySet(prefix.qualify(maxSize.name + "  ")) { it[size] * 4 }
        subject[size] = 1024
        assertEquals(subject[maxSize], subject[size] * 4)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_lazySetWithValidNameAndInvalidValueWithIncompatibleType_shouldThrowInvalidLazySetExceptionWhenGetting(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.lazySet(prefix.qualify(maxSize.name)) { "string" }
        assertThrows<InvalidLazySetException> {
            subject[prefix.qualify(maxSize.name)]
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_lazySetWithInvalidName_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> { subject.lazySet(invalidItemName) { 1024 } }
        assertEquals(e.name, invalidItemName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_unsetWithValidItem_shouldContainNullWhenUsingGetOrNull(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.unset(type)
        assertNull(subject.getOrNull(type))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_unsetWithInvalidItem_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> { subject.unset(invalidItem) }
        assertEquals(e.name, invalidItem.asName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_unsetWithValidName_shouldContainNullWhenUsingGetOrNull(prefix: String, provider: () -> Config) {
        val subject = provider()
        subject.unset(prefix.qualify(type.name))
        assertNull(subject.getOrNull(type))
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testSetOperation_unsetWithInvalidName_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> { subject.unset(invalidItemName) }
        assertEquals(e.name, invalidItemName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testClearOperation_shouldContainNoValues(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 1
            subject[maxSize] = 4
            assertEquals(subject[size], 1)
            assertEquals(subject[maxSize], 4)
            subject.clear()
            assertNull(subject.getOrNull(size))
            assertNull(subject.getOrNull(maxSize))
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testClearAllOperation_shouldContainNoValue(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertTrue(name in subject && type in subject)
        subject.clearAll()
        assertTrue(name !in subject && type !in subject)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testCheckWhetherAllRequiredItemsHaveValuesOrNot_shouldReturnFalseWhenSomeRequiredItemsDoNotHaveValues(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertFalse(subject.containsRequired())
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testCheckWhetherAllRequiredItemsHaveValuesOrNot_shouldReturnTrueWhenAllRequiredItemsHaveValues(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 1
            assertTrue(subject.containsRequired())
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testValidateWhetherAllRequiredItemsHaveValuesOrNot_shouldThrowUnsetValueExceptionWhenSomeRequiredItemsDoNotHaveValues(prefix: String, provider: () -> Config) {
        val subject = provider()
        assertThrows<UnsetValueException> { subject.validateRequired() }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testValidateWhetherAllRequiredItemsHaveValuesOrNot_shouldReturnItselfWhenAllRequiredItemsHaveValues(prefix: String, provider: () -> Config) {
        lock.withLock {
            val subject = provider()
            subject[size] = 1
            assertTrue(subject === subject.validateRequired())
        }
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testItemProperty_declareAPropertyByItem_shouldBehaveTheSameAsGet(prefix: String, provider: () -> Config) {
        val subject = provider()
        val nameProperty by subject.property(name)
        assertEquals(nameProperty, subject[name])
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testItemProperty_declareAPropertyByItem_shouldSupportSetOperation(prefix: String, provider: () -> Config) {
        val subject = provider()
        var nameProperty by subject.property(name)
        nameProperty = "newName"
        assertEquals(nameProperty, "newName")
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testItemProperty_declareAPropertyByInvalidItem_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> {
            @Suppress("UNUSED_VARIABLE")
            var nameProperty by subject.property(invalidItem)
        }
        assertEquals(e.name, invalidItem.asName)
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testItemProperty_declareAPropertyByName_shouldBehaveSameAsGet(prefix: String, provider: () -> Config) {
        val subject = provider()
        val nameProperty by subject.property<String>(prefix.qualify(name.name))
        assertEquals(nameProperty, subject[name])
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testItemProperty_declareAPropertyByName_shouldSupportSetOperation(prefix: String, provider: () -> Config) {
        val subject = provider()
        var nameProperty by subject.property<String>(prefix.qualify(name.name))
        nameProperty = "newName"
        assertEquals(nameProperty, "newName")
    }

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testItemProperty_declareAPropertyByInvalidName_shouldThrowNoSuchItemException(prefix: String, provider: () -> Config) {
        val subject = provider()
        val e = assertCheckedThrows<NoSuchItemException> {
            @Suppress("UNUSED_VARIABLE")
            var nameProperty by subject.property<Int>(invalidItemName)
        }
        assertEquals(e.name, invalidItemName)
    }

    // Extra test from DrillDownConfigSpec
    @Test
    fun testDrillDownConfig_pathIsEmptyString_shouldReturnItself() {
        val subject = drillDownSource()
        assertTrue(subject.at("") === subject)
    }

    // Extra test from BothConfigSpec
    @Test
    fun testBothConfig_givenAMergedConfig_whenSetItemInTheFallbackConfig_shouldHaveHigherPriorityThanTheDefaultValue() {
        val subject = bothConfigSource()
        (subject as MergedConfig).fallback[NetworkBuffer.type] = NetworkBuffer.Type.ON_HEAP
        assertEquals(subject[NetworkBuffer.type], NetworkBuffer.Type.ON_HEAP)
    }

    // Extra test from RollUpConfigSpec
    @Test
    fun testRollUp_givenPrefixIsEmptyString_shouldReturnItself() {
        val subject = rollUpSource()
        assertTrue(Prefix() + subject === subject)
    }

    // Extra tests from MultiLayerConfigSpec
    @Test
    fun testMultiLayer_shouldHaveSpecifiedName() {
        val subject = multiLayerSource()
        assertEquals(subject.name, "multi-layer")
    }

    @Test
    fun testMultiLayer_shouldContainSameItemsWithParentConfig() {
        val subject = multiLayerSource()
        assertEquals(subject[NetworkBuffer.name], subject.parent!![NetworkBuffer.name])
        assertEquals(subject[NetworkBuffer.type], subject.parent!![NetworkBuffer.type])
        assertEquals(subject[NetworkBuffer.offset], subject.parent!![NetworkBuffer.offset])
    }

    @Test
    fun testMultiLayer_onSetWithItem_itShouldKeepOtherLevelsUnchanged() {
        val subject = multiLayerSource()
        subject[NetworkBuffer.name] = "newName"
        assertEquals(subject[NetworkBuffer.name], "newName")
        assertEquals(subject.parent!![NetworkBuffer.name], "buffer")
    }

    @Test
    fun testMultiLayer_onSetWithName_itShouldKeepOtherLevelsUnchanged() {
        val subject = multiLayerSource()
        subject[subject.nameOf(NetworkBuffer.name)] = "newName"
        assertEquals(subject[NetworkBuffer.name], "newName")
        assertEquals(subject.parent!![NetworkBuffer.name], "buffer")
    }

    @Test
    fun testMultiLayer_onSetParentValue_itShouldPropagate() {
        val subject = multiLayerSource()
        subject.parent!![NetworkBuffer.name] = "newName"
        assertEquals(subject[NetworkBuffer.name], "newName")
        assertEquals(subject.parent!![NetworkBuffer.name], "newName")
    }

    @Test
    fun testMultiLayer_onAddSpec_itShouldAddAndNotChangeExisting() {
        val subject = multiLayerSource()
        val spec = object : ConfigSpec(NetworkBuffer.prefix) {
            val minSize by optional(1)
        }
        subject.addSpec(spec)
        assertTrue(spec.minSize in subject)
        assertTrue(subject.nameOf(spec.minSize) in subject)
        assertTrue(spec.minSize !in subject.parent!!)
        assertTrue(subject.nameOf(spec.minSize) !in subject.parent!!)
    }

    @Test
    fun testMultiLayer_onAddSpecToParent_itShouldThrowLayerFrozenException() {
        val subject = multiLayerSource()
        val spec = object : ConfigSpec(NetworkBuffer.prefix) {
            @Suppress("unused")
            val minSize by optional(1)
        }
        assertThrows<LayerFrozenException> { subject.parent!!.addSpec(spec) }
    }

    @Test
    fun testMultiLayer_onAddItemToParent_itShouldThrowLayerFrozenException() {
        val subject = multiLayerSource()
        val minSize by Spec.dummy.optional(1)
        assertThrows<LayerFrozenException> { subject.parent!!.addItem(minSize) }
    }

    @Test
    fun testMultiLayer_onIterateItemsInConfigAfterAddingSpec_itShouldCoverAllItemsInConfig() {
        val subject = multiLayerSource()
        val spec = object : ConfigSpec(NetworkBuffer.prefix) {
            @Suppress("unused")
            val minSize by optional(1)
        }
        subject.addSpec(spec)
        assertEquals(
            subject.iterator().asSequence().toSet(),
            (NetworkBuffer.items + spec.items).toSet()
        )
    }

    @Test
    fun testMultiLayer_onAddCustomDeserializerToMapperInParent_itShouldThrowLoadExceptionBeforeAddingDeserializer() {
        val spec = object : ConfigSpec() {
            @Suppress("unused")
            val item by required<StringWrapper>()
        }
        val parent = Config { addSpec(spec) }
        val child = parent.withLayer("child")
        assertTrue(parent.mapper === child.mapper)
        assertThrows<LoadException> { child.from.map.kv(mapOf("item" to "string")) }
    }

    @Test
    fun testMultiLayer_onAddCustomDeserializerToMapperInParent_itShouldBeAbleToUseTheSpecifiedDeserializerAfterAdding() {
        val spec = object : ConfigSpec() {
            val item by required<StringWrapper>()
        }
        val parent = Config { addSpec(spec) }
        val child = parent.withLayer("child")

        assertTrue(parent.mapper === child.mapper)
        parent.mapper.registerModule(
            SimpleModule().apply {
                addDeserializer(StringWrapper::class.java, StringWrapperDeserializer())
            }
        )
        val afterLoad = child.from.map.kv(mapOf("item" to "string"))
        assertTrue(child.mapper === afterLoad.mapper)
        assertEquals(afterLoad[spec.item], StringWrapper("string"))
    }

    companion object {
        @JvmStatic
        fun configTestSpecSource(): Stream<Arguments> = argumentsOf(
            // ConfigTestSpec
            configSpecOf {
                Config {
                    addSpec(NetworkBuffer)
                }
            },
            // DrillDownConfigSpec
            configSpecOf("buffer") {
                Config {
                    addSpec(NetworkBuffer)
                }.at("network")
            },
            // DrillDownMultiLayerConfigSpec
            configSpecOf("buffer") {
                Config {
                    addSpec(NetworkBuffer)
                }.withLayer("multi-layer").at("network")
            },
            // FacadeConfigSpec
            configSpecOf {
                Config() + Config {
                    addSpec(NetworkBuffer)
                }
            },
            // MultiLayerFacadeConfigSpec
            configSpecOf {
                (Config() + Config {
                    addSpec(NetworkBuffer)
                }).withLayer("multi-layer")
            },
            // FacadeMultiLayerConfigSpec
            configSpecOf {
                Config() + Config {
                    addSpec(NetworkBuffer)
                }.withLayer("multi-layer")
            },
            // FacadeConfigUsingWithFallbackSpec
            configSpecOf {
                Config {
                    addSpec(NetworkBuffer)
                }.withFallback(Config())
            },
            // FallbackConfigSpec
            configSpecOf {
                Config {
                    addSpec(NetworkBuffer)
                } + Config()
            },
            // MultiLayerFallbackConfigSpec
            configSpecOf {
                (Config {
                    addSpec(NetworkBuffer)
                } + Config()).withLayer("multi-layer")
            },
            // FallbackMultiLayerConfigSpec
            configSpecOf {
                Config {
                    addSpec(NetworkBuffer)
                }.withLayer("multi-layer") + Config()
            },
            // FallbackConfigUsingWithFallbackSpec
            configSpecOf {
                Config().withFallback(Config { addSpec(NetworkBuffer) })
            },
            // BothConfigSpec
            configSpecOf {
                Config { addSpec(NetworkBuffer) } + Config { addSpec(NetworkBuffer) }
            },
            // UpdateFallbackConfigSpec
            configSpecOf {
                UpdateFallbackConfig((Config { addSpec(NetworkBuffer) } + Config { addSpec(NetworkBuffer) }) as MergedConfig)
            },
            // MultiLayerDrillDownConfigSpec
            configSpecOf("buffer") {
                Config { addSpec(NetworkBuffer) }.at("network").withLayer("multi-layer")
            },
            // MultiLayerFacadeDrillDownConfigSpec
            configSpecOf("buffer") {
                (Config() + Config {
                    addSpec(NetworkBuffer)
                }.withLayer("layer1").at("network").withLayer("layer2")).withLayer("layer3")
            },
            // MultiLayerRollUpConfigSpec
            configSpecOf("prefix.network.buffer") {
                (Prefix("prefix") + Config { addSpec(NetworkBuffer) }).withLayer("multi-layer")
            },
            // MultiLayerRollUpFallbackConfigSpec
            configSpecOf("prefix.network.buffer") {
                ((Prefix("prefix") + Config { addSpec(NetworkBuffer) }.withLayer("layer1")).withLayer("layer2") + Config()).withLayer("layer3")
            },
            // RelocatedConfigSpec
            configSpecOf {
                Prefix("network.buffer") + Config { addSpec(NetworkBuffer) }.at("network.buffer")
            },
            // RollUpConfigSpec
            configSpecOf("prefix.network.buffer") {
                Prefix("prefix") + Config { addSpec(NetworkBuffer) }
            },
            // RollUpMultiLayerConfigSpec
            configSpecOf("prefix.network.buffer") {
                Prefix("prefix") + Config { addSpec(NetworkBuffer) }.withLayer("multi-layer")
            },
            // MultiLayerConfigSpec
            configSpecOf {
                Config { addSpec(NetworkBuffer) }.withLayer("multi-layer")
            }
        )

        @JvmStatic
        val drillDownSource: () -> Config = { Config { addSpec(NetworkBuffer) }.at("network") }

        @JvmStatic
        val bothConfigSource: () -> Config = { Config { addSpec(NetworkBuffer) } + Config { addSpec(NetworkBuffer) } }

        @JvmStatic
        val rollUpSource: () -> Config = { Prefix("prefix") + Config { addSpec(NetworkBuffer) } }

        @JvmStatic
        val multiLayerSource: () -> Config = { Config { addSpec(NetworkBuffer) }.withLayer("multi-layer") }
    }
}