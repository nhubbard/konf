package io.github.nhubbard.konf

import io.github.nhubbard.konf.source.asSource
import io.github.nhubbard.konf.source.asTree
import io.github.nhubbard.konf.source.base.toHierarchical
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestTreeNode {
    private lateinit var subject: TreeNode
    private lateinit var facadeNode: TreeNode
    private lateinit var facade: TreeNode
    private lateinit var fallbackNode: TreeNode
    private lateinit var fallback: TreeNode

    @BeforeEach
    fun setUp() {
        subject = ContainerNode(
            mutableMapOf(
                "level1" to ContainerNode(
                    mutableMapOf("level2" to EmptyNode)
                )
            )
        )
        facadeNode = 1.asTree()
        facade = mapOf(
            "key1" to facadeNode,
            "key2" to EmptyNode,
            "key4" to mapOf("level2" to facadeNode)
        ).asTree()
        fallbackNode = 2.asTree()
        fallback = mapOf(
            "key1" to EmptyNode,
            "key2" to fallbackNode,
            "key3" to fallbackNode,
            "key4" to mapOf("level2" to fallbackNode)
        ).asTree()
    }

    @Test
    fun testConvertToTree_shouldReturnItself() {
        assertSame(subject.asTree(), subject)
    }

    @Test
    fun testConvertToSource_shouldBeTheTreeInTheSource() {
        assertSame(subject.asSource().tree, subject)
    }

    @Test
    fun testSetWithAnInvalidPath_shouldThrowInvalidPathExceptionOnEmptyPath() {
        assertThrows<PathConflictException> {
            subject[""] = EmptyNode
        }
    }

    @Test
    fun testSetWithAnInvalidPath_shouldThrowInvalidPathExceptionOnInvalidPath() {
        assertThrows<PathConflictException> {
            subject["level1.level2.level3"] = EmptyNode
        }
    }

    @Test
    fun testInfixMinus_shouldReturnAnEmptyNode() {
        assertEquals(EmptyNode, subject - subject)
    }

    @Test
    fun testInfixMinusLeaf_shouldReturnAnEmptyNode() {
        assertEquals(EmptyNode, subject - EmptyNode)
    }

    @Test
    fun testMergeTwoTrees_shouldReturnTheMergedTreeWhenValid() {
        val expectedResult = mapOf(
            "key1" to facadeNode,
            "key2" to EmptyNode,
            "key3" to fallbackNode,
            "key4" to mapOf("level2" to facadeNode)
        ).asTree()
        assertEquals(expectedResult.toHierarchical(), (fallback + facade).toHierarchical())
        assertEquals(expectedResult.toHierarchical(), facade.withFallback(fallback).toHierarchical())
        assertEquals(facade.toHierarchical(), (EmptyNode + facade).toHierarchical())
        assertEquals(EmptyNode.toHierarchical(), (fallback + EmptyNode).toHierarchical())
        val complexMergeResult = mapOf(
            "key1" to mapOf("key2" to EmptyNode),
            "key2" to fallbackNode,
            "key3" to fallbackNode,
            "key4" to mapOf("level2" to fallbackNode)
        ).asTree()
        assertEquals(complexMergeResult.toHierarchical(), (fallback + mapOf("key1" to mapOf("key2" to EmptyNode)).asTree()).toHierarchical())
    }
}