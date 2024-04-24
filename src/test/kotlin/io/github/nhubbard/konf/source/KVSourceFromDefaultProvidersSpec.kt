package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.helpers.kvLoadContent
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object KVSourceFromDefaultProvidersSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
        }.withSource(DefaultMapProviders.kv(kvLoadContent))
    }

    itBehavesLike(SourceLoadBaseSpec)
})