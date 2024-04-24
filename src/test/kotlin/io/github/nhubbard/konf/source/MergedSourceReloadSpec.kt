package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.helpers.mergedSourceFacadeContent
import io.github.nhubbard.konf.source.helpers.mergedSourceFallbackContent
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object MergedSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.withSource(mergedSourceFallbackContent.asSource() + mergedSourceFacadeContent.asSource())
        Config {
            addSpec(ConfigForLoad)
        }.from.map.hierarchical(config.toHierarchicalMap())
    }

    itBehavesLike(SourceLoadBaseSpec)
})