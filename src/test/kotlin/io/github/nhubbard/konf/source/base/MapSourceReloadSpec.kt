package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import io.github.nhubbard.konf.source.base.helpers.mapSourceLoadContent
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object MapSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.map.hierarchical(mapSourceLoadContent)
        Config {
            addSpec(ConfigForLoad)
        }.from.map.hierarchical(config.toHierarchicalMap())
    }

    itBehavesLike(SourceLoadBaseSpec)
})