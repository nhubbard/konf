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

package com.nhubbard.konfig.source.hocon

import com.nhubbard.konfig.Config
import com.nhubbard.konfig.Feature
import com.nhubbard.konfig.source.ConfigForLoad
import com.nhubbard.konfig.source.SourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object HoconSourceLoadSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            enable(Feature.FAIL_ON_UNKNOWN_PATH)
        }.from.hocon.resource("source/source.conf")
    }

    itBehavesLike(SourceLoadBaseSpec)
})

object HoconSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.hocon.resource("source/source.conf")
        val hocon = config.toHocon.toText()
        Config {
            addSpec(ConfigForLoad)
        }.from.hocon.string(hocon)
    }

    itBehavesLike(SourceLoadBaseSpec)
})
