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

package com.nhubbard.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.sameInstance
import com.natpryce.hamkrest.throws
import com.nhubbard.konf.source.asSource
import com.nhubbard.konf.source.asTree
import com.nhubbard.konf.source.base.toHierarchical
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object TreeNodeSpec : SubjectSpek<TreeNode>({
    subject {
        ContainerNode(
            mutableMapOf(
                "level1" to ContainerNode(
                    mutableMapOf("level2" to EmptyNode)
                )
            )
        )
    }
    given("a tree node") {
        on("convert to tree") {
            it("should return itself") {
                assertThat(subject.asTree(), sameInstance(subject))
            }
        }
        on("convert to source") {
            it("should be the tree in the source") {
                assertThat(subject.asSource().tree, sameInstance(subject))
            }
        }
        on("set with an invalid path") {
            it("should throw InvalidPathException") {
                assertThat(
                    { subject[""] = EmptyNode },
                    throws(has(PathConflictException::path, equalTo("")))
                )
                assertThat(
                    { subject["level1.level2.level3"] = EmptyNode },
                    throws(has(PathConflictException::path, equalTo("level1.level2.level3")))
                )
            }
        }
        on("minus itself") {
            it("should return an empty node") {
                assertThat(subject - subject, equalTo(EmptyNode))
            }
        }
        on("minus a leaf node") {
            it("should return an empty node") {
                assertThat(subject - EmptyNode, equalTo(EmptyNode))
            }
        }
        on("merge two trees") {
            val facadeNode = 1.asTree()
            val facade = mapOf(
                "key1" to facadeNode,
                "key2" to EmptyNode,
                "key4" to mapOf("level2" to facadeNode)
            ).asTree()
            val fallbackNode = 2.asTree()
            val fallback = mapOf(
                "key1" to EmptyNode,
                "key2" to fallbackNode,
                "key3" to fallbackNode,
                "key4" to mapOf("level2" to fallbackNode)
            ).asTree()
            it("should return the merged tree when valid") {
                assertThat(
                    (fallback + facade).toHierarchical(),
                    equalTo(
                        mapOf(
                            "key1" to facadeNode,
                            "key2" to EmptyNode,
                            "key3" to fallbackNode,
                            "key4" to mapOf("level2" to facadeNode)
                        ).asTree().toHierarchical()
                    )
                )
                assertThat(
                    facade.withFallback(fallback).toHierarchical(),
                    equalTo(
                        mapOf(
                            "key1" to facadeNode,
                            "key2" to EmptyNode,
                            "key3" to fallbackNode,
                            "key4" to mapOf("level2" to facadeNode)
                        ).asTree().toHierarchical()
                    )
                )
                assertThat(
                    (EmptyNode + facade).toHierarchical(),
                    equalTo(facade.toHierarchical())
                )
                assertThat(
                    (fallback + EmptyNode).toHierarchical(),
                    equalTo(EmptyNode.toHierarchical())
                )
                assertThat(
                    (fallback + mapOf("key1" to mapOf("key2" to EmptyNode)).asTree()).toHierarchical(),
                    equalTo(
                        mapOf(
                            "key1" to mapOf(
                                "key2" to EmptyNode
                            ),
                            "key2" to fallbackNode,
                            "key3" to fallbackNode,
                            "key4" to mapOf("level2" to fallbackNode)
                        ).asTree().toHierarchical()
                    )
                )
            }
        }
    }
})
