package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.helpers.kvLoadContent
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object SourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.map.kv(kvLoadContent)
        Config {
            addSpec(ConfigForLoad)
        }.from.map.kv(config.toMap())
    }

    itBehavesLike(SourceLoadBaseSpec)
})