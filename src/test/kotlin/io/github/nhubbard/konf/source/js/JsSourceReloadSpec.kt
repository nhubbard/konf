package io.github.nhubbard.konf.source.js

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.ConfigForLoad
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object JsSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.js.resource("source/source.js")
        val js = config.toJs.toText()
        Config {
            addSpec(ConfigForLoad)
        }.from.js.string(js)
    }

    itBehavesLike(SourceLoadBaseSpec)
})