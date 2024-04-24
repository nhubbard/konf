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

package io.github.nhubbard.konf.source

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.CustomDeserializerConfig
import io.github.nhubbard.konf.source.helpers.VariantA
import io.github.nhubbard.konf.source.helpers.VariantB
import io.github.nhubbard.konf.source.helpers.customDeserializerLoadContent
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object CustomDeserializerSpec : SubjectSpek<Config>({
    subject {
        Config {
            addSpec(CustomDeserializerConfig)
        }.from.map.kv(customDeserializerLoadContent)
    }
    given("a source") {
        on("load the source into config") {
            it("should contain every value specified in the source") {
                val variantA = VariantA(1)
                val variantB = VariantB(2.0)
                assertThat(subject[CustomDeserializerConfig.variantA], equalTo(variantA))
                assertThat(subject[CustomDeserializerConfig.variantB], equalTo(variantB))
            }
        }
    }
})
