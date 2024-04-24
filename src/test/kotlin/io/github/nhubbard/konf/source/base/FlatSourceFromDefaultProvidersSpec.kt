package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.ConfigForLoad
import io.github.nhubbard.konf.source.Source
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object FlatSourceFromDefaultProvidersSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.withSource(Source.from.map.flat(flatSourceLoadContent))
    }

    itBehavesLike(FlatSourceLoadBaseSpec)
})