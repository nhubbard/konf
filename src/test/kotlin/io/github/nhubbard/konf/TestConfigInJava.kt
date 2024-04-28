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

@file:Suppress("unused")

package io.github.nhubbard.konf

import io.github.nhubbard.konf.helpers.NetworkBuffer
import io.github.nhubbard.konf.source.base.asKVSource
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestConfigInJava {
    private val spec = NetworkBufferInJava.spec
    private val size = NetworkBufferInJava.size
    private val maxSize = NetworkBufferInJava.maxSize
    private val name = NetworkBufferInJava.name
    private val type = NetworkBufferInJava.type
    private val offset = NetworkBufferInJava.offset
    private val prefix = "network.buffer"

    fun qualify(name: String) = "$prefix.$name"

    var subject = Config { addSpec(spec) }

    @BeforeEach
    fun resetSubject() {
        subject = Config { addSpec(spec) }
    }

    @Nested
    inner class GivenAConfig {
        private val invalidItem by ConfigSpec("invalid").required<Int>()
        private val invalidItemName = "invalid.invalidItem"

        @Nested
        inner class AddSpecOperations {
            @Nested
            inner class AddOrthogonalSpec {
                private val newSpec = object : ConfigSpec(spec.prefix) {
                    val minSize by optional(1)
                }
                private val config = subject.withSource(mapOf(newSpec.qualify(newSpec.minSize) to 2).asKVSource())

                init {
                    config.addSpec(newSpec)
                }

                @Test
                fun testShouldContainItemsInNewSpec() {
                    assertTrue(newSpec.minSize in config)
                    assertTrue(spec.qualify(newSpec.minSize) in config)
                    assertEquals(config.nameOf(newSpec.minSize), spec.qualify(newSpec.minSize))
                }

                @Test
                fun testShouldContainNewSpec() {
                    assertTrue(newSpec in config.specs)
                    assertTrue(spec in config.specs)
                }

                @Test
                fun testShouldLoadValuesFromExistingSourcesForItemsInNewSpec() {
                    assertEquals(config[newSpec.minSize], 2)
                }
            }

            @Nested
            inner class AddRepeatedItem {
                @Test
                fun testShouldThrowRepeatedItemException() {
                    val e = assertCheckedThrows<RepeatedItemException> { subject.addSpec(spec) }
                    assertEquals(e.name, spec.qualify(size))
                }
            }

            @Nested
            inner class AddRepeatedName {
                private val newSpec = ConfigSpec(prefix).apply {
                    val size by required<Int>()
                }

                @Test
                fun testShouldThrowNameConflictException() {
                    assertThrows<NameConflictException> {
                        subject.addSpec(newSpec)
                    }
                }
            }

            @Nested
            inner class AddConflictNameAsPrefixOfExistingName {
                private val newSpec = ConfigSpec().apply {
                    val buffer by required<Int>()
                }

                @Test
                fun testShouldThrowNameConflictException() {
                    assertThrows<NameConflictException> {
                        subject.addSpec(
                            newSpec.withPrefix(prefix.toPath().let { it.subList(0, it.size - 1) }.name)
                        )
                    }
                }
            }

            @Nested
            inner class AddConflictNameWithExistingNameAsPrefix {
                private val newSpec = ConfigSpec(qualify(type.name)).apply {
                    val subType by required<Int>()
                }

                @Test
                fun testShouldThrowNameConflictException() {
                    assertThrows<NameConflictException> {
                        subject.addSpec(newSpec)
                    }
                }
            }
        }

        @Nested
        inner class AddItemOperations {
            @Nested
            inner class AddOrthogonalItem {
                private val minSize by Spec.dummy.optional(1)
                private val config = subject.withSource(mapOf(spec.qualify(minSize) to 2).asKVSource())

                init {
                    config.addItem(minSize, spec.prefix)
                }

                @Test
                fun testShouldContainItem() {
                    assertTrue(minSize in config)
                    assertTrue(spec.qualify(minSize) in config)
                    assertEquals(config.nameOf(minSize), spec.qualify(minSize))
                }

                @Test
                fun testShouldLoadValuesFromExistingSourcesForItem() {
                    assertEquals(config[minSize], 2)
                }
            }

            @Nested
            inner class AddRepeatedItem {
                @Test
                fun testShouldThrowRepeatedItemException() {
                    val e = assertCheckedThrows<RepeatedItemException> { subject.addItem(size, spec.prefix) }
                    assertEquals(e.name, spec.qualify(size))
                }
            }

            @Nested
            inner class AddRepeatedName {
                private val size by Spec.dummy.required<Int>()

                @Test
                fun testShouldThrowNameConflictException() {
                    assertThrows<NameConflictException> {
                        subject.addItem(size, prefix)
                    }
                }
            }

            @Nested
            inner class AddConflictNameWhichIsPrefixOfExistingName {
                private val buffer by Spec.dummy.required<Int>()

                @Test
                fun testShouldThrowNameConflictException() {
                    assertThrows<NameConflictException> {
                        subject.addItem(buffer, prefix.toPath().let { it.subList(0, it.size - 1) }.name)
                    }
                }
            }

            @Nested
            inner class AddConflictNameWhereExistingNameIsPrefixOfIt {
                private val subType by Spec.dummy.required<Int>()

                @Test
                fun testShouldThrowNameConflictException() {
                    assertThrows<NameConflictException> {
                        subject.addItem(subType, qualify(type.name))
                    }
                }
            }
        }

        @Nested
        inner class Iterators {
            @Test
            fun testIteratorCoversAllItemsInConfig() {
                assertEquals(subject.items.toSet(), spec.items.toSet())
            }

            @Test
            fun testNameIteratorCoversAllItemsInConfig() {
                assertEquals(subject.nameOfItems.toSet(), spec.items.map { qualify(it.name) }.toSet())
            }
        }

        @Nested
        inner class MapExport {
            @Test
            fun testShouldNotContainUnsetItemsInMap() {
                assertEquals(
                    subject.toMap(),
                    mapOf<String, Any>(
                        qualify(name.name) to "buffer",
                        qualify(type.name) to NetworkBuffer.Type.OFF_HEAP.name,
                        qualify(offset.name) to "null"
                    )
                )
            }

            @Test
            fun testShouldContainCorrespondingItemsInMap() {
                subject[size] = 4
                subject[type] = NetworkBuffer.Type.ON_HEAP
                subject[offset] = 0
                val map = subject.toMap()
                assertEquals(
                    map,
                    mapOf(
                        qualify(size.name) to 4,
                        qualify(maxSize.name) to 8,
                        qualify(name.name) to "buffer",
                        qualify(type.name) to NetworkBuffer.Type.ON_HEAP.name,
                        qualify(offset.name) to 0
                    )
                )
            }

            @Test
            fun testShouldRecoverAllItemsWhenReloadFromMap() {
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

        @Nested
        inner class HierarchicalMapExport {
            private fun prefixToMap(prefix: String, value: Map<String, Any>): Map<String, Any> = when {
                prefix.isEmpty() -> value
                prefix.contains('.') -> mapOf<String, Any>(
                    prefix.substring(0, prefix.indexOf('.')) to prefixToMap(prefix.substring(prefix.indexOf('.') + 1), value)
                )
                else -> mapOf(prefix to value)
            }

            @Test
            fun testShouldNotContainUnsetItemsInMap() {
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

            @Test
            fun testShouldContainCorrespondingItemsInMap() {
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

            @Test
            fun testShouldRecoverAllItemsWhenReloadedFromMap() {
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

        @Nested
        inner class ObjectMethods {
            private val map = mapOf(
                qualify(name.name) to "buffer",
                qualify(type.name) to NetworkBuffer.Type.OFF_HEAP.name,
                qualify(offset.name) to "null"
            )

            @Test
            fun testShouldNotEqualObjectForOtherClass() {
                assertFalse(subject.equals(1))
            }

            @Test
            fun testShouldEqualItself() {
                assertEquals(subject, subject)
            }

            @Test
            fun testShouldConvertToStringInMapLikeFormat() {
                assertEquals(subject.toString(), "Config(items=$map)")
            }
        }

        @Nested
        inner class LockConfig {
            @Test
            fun testShouldBeLocked() {
                // Can't test this; the original test left it blank, and the lock is private.
                subject.lock {}
            }
        }

        @Nested
        inner class GetOperation {
            @Test
            fun testGetWithValidItemShouldReturnCorrespondingValue() {
                assertEquals(subject[name], "buffer")
                assertTrue(name in subject)
                assertNull(subject[offset])
                assertTrue(offset in subject)
                assertNull(subject.getOrNull(maxSize))
                assertTrue(maxSize in subject)
            }

            @Test
            fun testGetWithInvalidItemShouldThrowUsingGet() {
                val e = assertCheckedThrows<NoSuchItemException> { subject[invalidItem] }
                assertEquals(e.name, invalidItem.asName)
            }

            @Test
            fun testGetWithInvalidItemShouldReturnNullWhenUsingGetOrNull() {
                assertNull(subject.getOrNull(invalidItem))
                assertTrue(invalidItem !in subject)
            }

            @Test
            fun testGetWithValidNameShouldReturnCorrespondingValue() {
                assertEquals(subject(qualify("name")), "buffer")
                assertEquals(subject.getOrNull<String>(qualify("name")), "buffer")
                assertTrue(qualify("name") in subject)
            }

            @Test
            fun testGetWithInvalidNameShouldThrowWhenUsingGet() {
                val e = assertCheckedThrows<NoSuchItemException> { subject<String>(spec.qualify(invalidItem)) }
                assertEquals(e.name, spec.qualify(invalidItem))
            }

            @Test
            fun testGetWithInvalidNameShouldReturnNullWhenUsingGetOrNull() {
                assertNull(subject.getOrNull<String>(spec.qualify(invalidItem)))
                assertTrue(spec.qualify(invalidItem) !in subject)
            }

            @Test
            fun testGetUnsetItemShouldThrowUnsetValueException() {
                var e = assertCheckedThrows<UnsetValueException> { subject[size] }
                assertEquals(e.name, size.asName)
                e = assertCheckedThrows<UnsetValueException> { subject[maxSize] }
                assertEquals(e.name, size.asName)
                assertTrue(size in subject)
                assertTrue(maxSize in subject)
            }

            @Test
            fun testGetWithLazyItemThatReturnsNullWhenTheTypeIsNullableShouldReturnNull() {
                val lazyItem by Spec.dummy.lazy<Int?> { null }
                subject.addItem(lazyItem, prefix)
                assertNull(subject[lazyItem])
            }

            @Test
            fun testGetWithLazyItemThatReturnsNullWhenTheTypeIsNotNullableShouldThrowInvalidLazySetException() {
                @Suppress("UNCHECKED_CAST")
                val thunk = { _: ItemContainer -> null } as (ItemContainer) -> Int
                val lazyItem by Spec.dummy.lazy(thunk = thunk)
                subject.addItem(lazyItem, prefix)
                assertThrows<InvalidLazySetException> {
                    subject[lazyItem]
                }
            }
        }

        @Nested
        inner class SetOperation {
            @Test
            fun testSetWithValidItemWhenCorrespondingValueIsUnsetShouldContainTheSpecifiedValue() {
                subject[size] = 1024
                assertEquals(subject[size], 1024)
            }

            @Test
            fun testSetWithValidItemWhenCorrespondingValueExistsShouldContainTheSpecifiedValue() {
                subject[name] = "newName"
                assertEquals(subject[name], "newName")
                subject[offset] = 0
                assertEquals(subject[offset], 0)
                subject[offset] = null
                assertNull(subject[offset])
            }

            @Test
            fun testRawSetWithValidItemShouldContainTheSpecifiedValue() {
                subject.rawSet(size, 2048)
                assertEquals(subject[size], 2048)
            }

            @Test
            fun testSetWithValidItemWhenCorrespondingValueIsLazyHasCorrectLifecycle() {
                subject[size] = 1024
                assertEquals(subject[maxSize], subject[size] * 2)
                subject[maxSize] = 0
                assertEquals(subject[maxSize], 0)
                subject[size] = 2048
                assertNotEquals(subject[maxSize], subject[size] * 2)
                assertEquals(subject[maxSize], 0)
            }

            @Test
            fun testSetWithInvalidItemShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> { subject[invalidItem] = 1024 }
                assertEquals(e.name, invalidItem.asName)
            }

            @Test
            fun testSetWithValidNameShouldContainTheSpecifiedValue() {
                subject[qualify("size")] = 1024
                assertEquals(subject[size], 1024)
            }

            @Test
            fun testSetWithInvalidNameShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> { subject[invalidItemName] = 1024 }
                assertEquals(e.name, invalidItemName)
            }

            @Test
            fun testSetWithIncorrectTypeOfValueShouldThrowClassCastException() {
                assertThrows<ClassCastException> {
                    subject[qualify(size.name)] = "1024"
                }
                assertThrows<ClassCastException> {
                    subject[qualify(size.name)] = null
                }
            }

            @Test
            fun testLazySetWithValidItemShouldContainTheSpecifiedValue() {
                subject.lazySet(maxSize) { it[size] * 4 }
                subject[size] = 1024
                assertEquals(subject[maxSize], subject[size] * 4)
            }

            @Test
            fun testLazySetWithInvalidItemShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> { subject.lazySet(invalidItem) { 1024 } }
                assertEquals(e.name, invalidItem.asName)
            }

            @Test
            fun testLazySetWithValidNameShouldContainTheSpecifiedValue() {
                subject.lazySet(qualify(maxSize.name)) { it[size] * 4 }
                subject[size] = 1024
                assertEquals(subject[maxSize], subject[size] * 4)
            }

            @Test
            fun testLazySetWithValidNameAndInvalidValueWithIncompatibleTypeThrowsInvalidLazySetException() {
                subject.lazySet(qualify(maxSize.name)) { "string" }
                assertThrows<InvalidLazySetException> {
                    subject[qualify(maxSize.name)]
                }
            }

            @Test
            fun testLazySetWithInvalidNameShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> { subject.lazySet(invalidItemName) { 1024 } }
                assertEquals(e.name, invalidItemName)
            }

            @Test
            fun testUnsetWithValidItemShouldContainNullWhenUsingGetOrNull() {
                subject.unset(type)
                assertNull(subject.getOrNull(type))
            }

            @Test
            fun testUnsetWithInvalidItemShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> { subject.unset(invalidItem) }
                assertEquals(e.name, invalidItem.asName)
            }

            @Test
            fun testUnsetWithValidNameShouldContainNullWhenUsingGetOrNull() {
                subject.unset(qualify(type.name))
                assertNull(subject.getOrNull(type))
            }

            @Test
            fun testUnsetWithInvalidNameShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> { subject.unset(invalidItemName) }
                assertEquals(e.name, invalidItemName)
            }
        }

        @Test
        fun testClearOperationShouldContainNoValue() {
            val config = if (subject.name == "multi-layer") {
                subject.parent!!
            } else {
                subject
            }
            assertTrue(name in config && type in config)
            config.clear()
            assertTrue(name !in config && type !in config)
        }

        @Nested
        inner class ItemProperty {
            @Test
            fun testDeclaringAPropertyByItemShouldBehaveTheSameAsGet() {
                val nameProperty by subject.property(name)
                assertEquals(nameProperty, subject[name])
            }

            @Test
            fun testDeclaringAPropertyByItemShouldSupportTheSetOperation() {
                var nameProperty by subject.property(name)
                nameProperty = "newName"
                assertEquals(nameProperty, "newName")
            }

            @Test
            fun testDeclaringAPropertyByInvalidItemShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> {
                    @Suppress("UNUSED_VARIABLE")
                    val nameProperty by subject.property(invalidItem)
                }
                assertEquals(e.name, invalidItem.asName)
            }

            @Test
            fun testDeclaringAPropertyByNameShouldBehaveTheSameAsGet() {
                val nameProperty by subject.property<String>(qualify(name.name))
                assertEquals(nameProperty, subject[name])
            }

            @Test
            fun testDeclaringAPropertyByNameShouldSupportSetOperation() {
                var nameProperty by subject.property<String>(qualify(name.name))
                nameProperty = "newName"
                assertEquals(nameProperty, "newName")
            }

            @Test
            fun testDeclaringAPropertyByInvalidNameShouldThrowNoSuchItemException() {
                val e = assertCheckedThrows<NoSuchItemException> {
                    @Suppress("UNUSED_VARIABLE")
                    val nameProperty by subject.property<Int>(invalidItemName)
                }
                assertEquals(e.name, invalidItemName)
            }
        }
    }
}