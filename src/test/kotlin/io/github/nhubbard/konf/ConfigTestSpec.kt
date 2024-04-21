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

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.dsl.SubjectProviderDsl
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

object ConfigTestSpec : SubjectSpek<Config>({
    subject { Config { addSpec(NetworkBuffer) } }

    configTestSpec()
})

fun SubjectProviderDsl<Config>.configTestSpec(prefix: String = "network.buffer") {
    val spec = NetworkBuffer
    val size = NetworkBuffer.size
    val maxSize = NetworkBuffer.maxSize
    val name = NetworkBuffer.name
    val type = NetworkBuffer.type
    val offset = NetworkBuffer.offset

    fun qualify(name: String): String = if (prefix.isEmpty()) name else "$prefix.$name"

    given("a config") {
        val invalidItem by ConfigSpec("invalid").required<Int>()
        val invalidItemName = "invalid.invalidItem"
        group("set operation") {
            on("set with valid item when corresponding value is unset") {
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[size], equalTo(1024))
                }
            }
            on("set with valid item when corresponding value exists") {
                it("should contain the specified value") {
                    subject[name] = "newName"
                    assertThat(subject[name], equalTo("newName"))
                    subject[offset] = 0
                    assertThat(subject[offset], equalTo(0))
                    subject[offset] = null
                    assertNull(subject[offset])
                }
            }
            on("raw set with valid item") {
                it("should contain the specified value") {
                    subject.rawSet(size, 2048)
                    assertThat(subject[size], equalTo(2048))
                }
            }
            on("set with valid item when corresponding value is lazy") {
                test(
                    "before set, the item should be lazy; after set," +
                            " the item should be no longer lazy, and it contains the specified value"
                ) {
                    subject[size] = 1024
                    assertThat(subject[maxSize], equalTo(subject[size] * 2))
                    subject[maxSize] = 0
                    assertThat(subject[maxSize], equalTo(0))
                    subject[size] = 2048
                    assertThat(subject[maxSize], !equalTo(subject[size] * 2))
                    assertThat(subject[maxSize], equalTo(0))
                }
            }
            on("set with invalid item") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        { subject[invalidItem] = 1024 },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName)))
                    )
                }
            }
            on("set with valid name") {
                subject[qualify("size")] = 1024
                it("should contain the specified value") {
                    assertThat(subject[size], equalTo(1024))
                }
            }
            on("set with valid name which contains trailing whitespaces") {
                subject[qualify("size  ")] = 1024
                it("should contain the specified value") {
                    assertThat(subject[size], equalTo(1024))
                }
            }
            on("set with invalid name") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        { subject[invalidItemName] = 1024 },
                        throws(has(NoSuchItemException::name, equalTo(invalidItemName)))
                    )
                }
            }
            on("set with incorrect type of value") {
                it("should throw ClassCastException") {
                    assertThat({ subject[qualify(size.name)] = "1024" }, throws<ClassCastException>())
                    assertThat({ subject[qualify(size.name)] = null }, throws<ClassCastException>())
                }
            }
            on("set when beforeSet subscriber is defined") {
                val childConfig = subject.withLayer()
                subject[size] = 1
                var counter = 0
                val handler1 = childConfig.beforeSet { item, value ->
                    counter += 1
                    it("should contain the old value") {
                        assertThat(item, equalTo(size))
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(1))
                    }
                }
                val handler2 = childConfig.beforeSet { item, value ->
                    counter += 1
                    it("should contain the old value") {
                        assertThat(item, equalTo(size))
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(1))
                    }
                }
                val handler3 = size.beforeSet { _, value ->
                    counter += 1
                    it("should contain the old value") {
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(1))
                    }
                }
                val handler4 = size.beforeSet { _, value ->
                    counter += 1
                    it("should contain the old value") {
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(1))
                    }
                }
                subject[size] = 2
                handler1.close()
                handler2.close()
                handler3.close()
                handler4.close()
                it("should notify subscriber") {
                    assertThat(counter, equalTo(4))
                }
            }
            on("set when afterSet subscriber is defined") {
                val childConfig = subject.withLayer()
                subject[size] = 1
                var counter = 0
                val handler1 = childConfig.afterSet { item, value ->
                    counter += 1
                    it("should contain the new value") {
                        assertThat(item, equalTo(size))
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(2))
                    }
                }
                val handler2 = childConfig.afterSet { item, value ->
                    counter += 1
                    it("should contain the new value") {
                        assertThat(item, equalTo(size))
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(2))
                    }
                }
                val handler3 = size.afterSet { _, value ->
                    counter += 1
                    it("should contain the new value") {
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(2))
                    }
                }
                val handler4 = size.afterSet { _, value ->
                    counter += 1
                    it("should contain the new value") {
                        assertThat(value, equalTo(2))
                        assertThat(childConfig[size], equalTo(2))
                    }
                }
                subject[size] = 2
                handler1.close()
                handler2.close()
                handler3.close()
                handler4.close()
                it("should notify subscriber") {
                    assertThat(counter, equalTo(4))
                }
            }
            on("set when onSet subscriber is defined") {
                var counter = 0
                size.onSet { counter += 1 }.use {
                    subject[size] = 1
                    subject[size] = 16
                    subject[size] = 256
                    subject[size] = 1024
                    it("should notify subscriber") {
                        assertThat(counter, equalTo(4))
                    }
                }
            }
            on("set when multiple onSet subscribers are defined") {
                var counter = 0
                size.onSet { counter += 1 }.use {
                    size.onSet { counter += 2 }.use {
                        subject[size] = 1
                        subject[size] = 16
                        subject[size] = 256
                        subject[size] = 1024
                        it("should notify all subscribers") {
                            assertThat(counter, equalTo(12))
                        }
                    }
                }
            }
            on("lazy set with valid item") {
                subject.lazySet(maxSize) { it[size] * 4 }
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[maxSize], equalTo(subject[size] * 4))
                }
            }
            on("lazy set with invalid item") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        { subject.lazySet(invalidItem) { 1024 } },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName)))
                    )
                }
            }
            on("lazy set with valid name") {
                subject.lazySet(qualify(maxSize.name)) { it[size] * 4 }
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[maxSize], equalTo(subject[size] * 4))
                }
            }
            on("lazy set with valid name which contains trailing whitespaces") {
                subject.lazySet(qualify(maxSize.name + "  ")) { it[size] * 4 }
                subject[size] = 1024
                it("should contain the specified value") {
                    assertThat(subject[maxSize], equalTo(subject[size] * 4))
                }
            }
            on("lazy set with valid name and invalid value with incompatible type") {
                subject.lazySet(qualify(maxSize.name)) { "string" }
                it("should throw InvalidLazySetException when getting") {
                    assertThat({ subject[qualify(maxSize.name)] }, throws<InvalidLazySetException>())
                }
            }
            on("lazy set with invalid name") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        { subject.lazySet(invalidItemName) { 1024 } },
                        throws(has(NoSuchItemException::name, equalTo(invalidItemName)))
                    )
                }
            }
            on("unset with valid item") {
                subject.unset(type)
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(type), absent())
                }
            }
            on("unset with invalid item") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        { subject.unset(invalidItem) },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName)))
                    )
                }
            }
            on("unset with valid name") {
                subject.unset(qualify(type.name))
                it("should contain `null` when using `getOrNull`") {
                    assertThat(subject.getOrNull(type), absent())
                }
            }
            on("unset with invalid name") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        { subject.unset(invalidItemName) },
                        throws(has(NoSuchItemException::name, equalTo(invalidItemName)))
                    )
                }
            }
        }
        on("clear operation") {
            subject[size] = 1
            subject[maxSize] = 4
            it("should contain no value") {
                assertThat(subject[size], equalTo(1))
                assertThat(subject[maxSize], equalTo(4))
                subject.clear()
                assertNull(subject.getOrNull(size))
                assertNull(subject.getOrNull(maxSize))
            }
        }
        on("clear all operation") {
            it("should contain no value") {
                assertTrue { name in subject && type in subject }
                subject.clearAll()
                assertTrue { name !in subject && type !in subject }
            }
        }
        on("check whether all required items have values or not") {
            it("should return false when some required items don't have values") {
                assertFalse { subject.containsRequired() }
            }
            it("should return true when all required items have values") {
                subject[size] = 1
                assertTrue { subject.containsRequired() }
            }
        }
        on("validate whether all required items have values or not") {
            it("should throw UnsetValueException when some required items don't have values") {
                assertThat(
                    {
                        subject.validateRequired()
                    },
                    throws<UnsetValueException>()
                )
            }
            it("should return itself when all required items have values") {
                subject[size] = 1
                assertThat(subject, sameInstance(subject.validateRequired()))
            }
        }
        group("item property") {
            on("declare a property by item") {
                var nameProperty by subject.property(name)
                it("should behave same as `get`") {
                    assertThat(nameProperty, equalTo(subject[name]))
                }
                it("should support set operation as `set`") {
                    nameProperty = "newName"
                    assertThat(nameProperty, equalTo("newName"))
                }
            }
            on("declare a property by invalid item") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        {
                            @Suppress("UNUSED_VARIABLE")
                            var nameProperty by subject.property(invalidItem)
                        },
                        throws(has(NoSuchItemException::name, equalTo(invalidItem.asName)))
                    )
                }
            }
            on("declare a property by name") {
                var nameProperty by subject.property<String>(qualify(name.name))
                it("should behave same as `get`") {
                    assertThat(nameProperty, equalTo(subject[name]))
                }
                it("should support set operation as `set`") {
                    nameProperty = "newName"
                    assertThat(nameProperty, equalTo("newName"))
                }
            }
            on("declare a property by invalid name") {
                it("should throw NoSuchItemException") {
                    assertThat(
                        {
                            @Suppress("UNUSED_VARIABLE")
                            var nameProperty by subject.property<Int>(invalidItemName)
                        },
                        throws(has(NoSuchItemException::name, equalTo(invalidItemName)))
                    )
                }
            }
        }
    }
}