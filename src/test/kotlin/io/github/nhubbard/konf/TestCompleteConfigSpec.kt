package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.base.asKVSource
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestCompleteConfigSpec {
    val spec = NetworkBuffer
    val size = NetworkBuffer.size
    val maxSize = NetworkBuffer.maxSize
    val name = NetworkBuffer.name
    val type = NetworkBuffer.type
    val offset = NetworkBuffer.offset

    val invalidItem by ConfigSpec("invalid").required<Int>()
    val invalidItemName = "invalid.invalidItem"

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

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToMap_shouldRecoverAllItemsWhenReloadedFromMap(prefix: String, provider: () -> Config) {
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

    @ParameterizedTest
    @MethodSource("configTestSpecSource")
    fun testExportValuesToHierarchicalMap_shouldRecoverAllItemsWhenReloadedFromMap(prefix: String, provider: () -> Config) {
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

    // TODO: Resume from "set operation" in ConfigTestSpec.kt

    // TODO: Extra tests from DrillDownConfigSpec, BothConfigSpec, RollUpConfigSpec
    // TODO: MultiLayerConfigSpec?

    companion object {
        /**
         * The method source for each suite of tests.
         */
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
            }
        )
    }
}