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

import com.fasterxml.jackson.databind.DeserializationFeature
import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigTestReport
import io.github.nhubbard.konf.source.yaml.yaml
import io.github.nhubbard.konf.toValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestMultiLayerConfigToValue {
    //language=YAML
    private val yamlContent = """
db:
  driverClassName: org.h2.Driver
  url: 'jdbc:h2:mem:db;DB_CLOSE_DELAY=-1'
    """.trimIndent()

    val map = mapOf(
        "driverClassName" to "org.h2.Driver",
        "url" to "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"
    )

    @Test
    fun testLoadFromMultipleSources_itShouldCastToValueCorrectly() {
        val config = Config {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
            .from.yaml.string(yamlContent)
            .from.yaml.file(
                System.getenv("SERVICE_CONFIG")
                    ?: "/opt/legacy-event-service/conf/legacy-event-service.yml",
                true
            )
            .from.systemProperties()
            .from.env()
        val db = config.toValue<ConfigTestReport>()
        assertEquals(map, db.db)
    }
}