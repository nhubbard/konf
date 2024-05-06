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
import io.github.nhubbard.konf.source.helpers.DefaultLoadersConfig
import io.github.nhubbard.konf.source.helpers.multipleDefaultLoadersJsonContent
import io.github.nhubbard.konf.source.helpers.propertiesContent
import io.github.nhubbard.konf.source.hocon.hocon
import io.github.nhubbard.konf.source.hocon.hoconContent
import io.github.nhubbard.konf.source.toml.toml
import io.github.nhubbard.konf.source.toml.tomlContent
import io.github.nhubbard.konf.source.xml.xml
import io.github.nhubbard.konf.source.xml.xmlContent
import io.github.nhubbard.konf.source.yaml.yaml
import io.github.nhubbard.konf.source.yaml.yamlContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestMultipleDefaultLoaders {
    @Test
    fun testLoadFromMultipleSources_itShouldLoadTheCorrespondingValueInEachLayer() {
        val config = Config {
            addSpec(DefaultLoadersConfig)
        }
        val item = DefaultLoadersConfig.type
        val afterLoadEnv = config.from.env()
        System.setProperty(config.nameOf(DefaultLoadersConfig.type), "system")
        val afterLoadSystemProperties = afterLoadEnv.from.systemProperties()
        val afterLoadHocon = afterLoadSystemProperties.from.hocon.string(hoconContent)
        val afterLoadJson = afterLoadHocon.from.json.string(multipleDefaultLoadersJsonContent)
        val afterLoadProperties = afterLoadJson.from.properties.string(propertiesContent)
        val afterLoadToml = afterLoadProperties.from.toml.string(tomlContent)
        val afterLoadXml = afterLoadToml.from.xml.string(xmlContent)
        val afterLoadYaml = afterLoadXml.from.yaml.string(yamlContent)
        val afterLoadFlat = afterLoadYaml.from.map.flat(mapOf("source.test.type" to "flat"))
        val afterLoadKv = afterLoadFlat.from.map.kv(mapOf("source.test.type" to "kv"))
        val afterLoadHierarchical = afterLoadKv.from.map.hierarchical(
            mapOf(
                "source" to mapOf(
                    "test" to mapOf("type" to "hierarchical")
                )
            )
        )
        assertEquals("env", afterLoadEnv[item])
        assertEquals("system", afterLoadSystemProperties[item])
        assertEquals("conf", afterLoadHocon[item])
        assertEquals("json", afterLoadJson[item])
        assertEquals("properties", afterLoadProperties[item])
        assertEquals("toml", afterLoadToml[item])
        assertEquals("xml", afterLoadXml[item])
        assertEquals("yaml", afterLoadYaml[item])
        assertEquals("flat", afterLoadFlat[item])
        assertEquals("kv", afterLoadKv[item])
        assertEquals("hierarchical", afterLoadHierarchical[item])
    }
}