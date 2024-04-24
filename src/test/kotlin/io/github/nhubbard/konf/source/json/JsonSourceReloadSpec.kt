package io.github.nhubbard.konf.source.json

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object JsonSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.json.resource("source/source.json")
        val json = config.toJson.toText()
        Config {
            addSpec(ConfigForLoad)
        }.from.json.string(json)
    }

    itBehavesLike(SourceLoadBaseSpec)
})