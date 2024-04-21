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

package io.github.nhubbard.konf.source.xml

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.ConfigForLoad
import io.github.nhubbard.konf.source.base.FlatConfigForLoad
import io.github.nhubbard.konf.source.base.FlatSourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object XmlSourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.xml.resource("source/source.xml")
    }

    itBehavesLike(FlatSourceLoadBaseSpec)
})

object XmlSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.xml.resource("source/source.xml")
        val xml = config.toXml.toText()
        Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.xml.string(xml)
    }

    itBehavesLike(FlatSourceLoadBaseSpec)
})