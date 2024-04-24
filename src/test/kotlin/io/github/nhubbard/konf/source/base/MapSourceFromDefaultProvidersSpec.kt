package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.Feature
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.Source
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import io.github.nhubbard.konf.source.base.helpers.mapSourceLoadContent
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object MapSourceFromDefaultProvidersSpec : SubjectSpek<Config>({

    subject {
        Config {
            addSpec(ConfigForLoad)
            enable(Feature.FAIL_ON_UNKNOWN_PATH)
        }.withSource(Source.from.map.hierarchical(mapSourceLoadContent))
    }

    itBehavesLike(SourceLoadBaseSpec)
})