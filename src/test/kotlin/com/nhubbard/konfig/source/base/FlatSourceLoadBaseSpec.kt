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

package com.nhubbard.konfig.source.base

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhubbard.konfig.Config
import com.nhubbard.konfig.source.SourceLoadBaseSpec
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike
import java.util.*
import kotlin.test.assertTrue

object FlatSourceLoadBaseSpec : SubjectSpek<Config>({
    itBehavesLike(SourceLoadBaseSpec)

    given("a flat source") {
        on("load the source into config") {
            it("should contain every value specified in the source") {
                val classForLoad = ClassForLoad(
                    stringWithComma = "string,with,comma",
                    emptyList = listOf(),
                    emptySet = setOf(),
                    emptyArray = intArrayOf(),
                    emptyObjectArray = arrayOf(),
                    singleElementList = listOf(1),
                    multipleElementsList = listOf(1, 2)
                )
                assertThat(subject[FlatConfigForLoad.emptyList], equalTo(listOf()))
                assertThat(subject[FlatConfigForLoad.emptySet], equalTo(setOf()))
                assertTrue(subject[FlatConfigForLoad.emptyArray].contentEquals(intArrayOf()))
                assertTrue(subject[FlatConfigForLoad.emptyObjectArray].contentEquals(arrayOf()))
                assertThat(subject[FlatConfigForLoad.singleElementList], equalTo(listOf(1)))
                assertThat(subject[FlatConfigForLoad.multipleElementsList], equalTo(listOf(1, 2)))
                assertThat(
                    subject[FlatConfigForLoad.flatClass].stringWithComma,
                    equalTo(classForLoad.stringWithComma)
                )
            }
        }
    }
})