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

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ServicingConfig
import io.github.nhubbard.konf.source.helpers.mergeSourcesWithDifferentFeaturesContent
import io.github.nhubbard.konf.source.hocon.hocon
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestMergeSourcesWithDifferentFeatures {
    @Test
    fun testLoadFromMergedSource_itShouldContainTheItem() {
        val config = Config {
            addSpec(ServicingConfig)
        }.withSource(Source.from.hocon.string(mergeSourcesWithDifferentFeaturesContent) + Source.from.env())
        assertEquals("https://service/api", config[ServicingConfig.baseURL])
        assertEquals("https://service/api/index.html", config[ServicingConfig.url])
    }
}