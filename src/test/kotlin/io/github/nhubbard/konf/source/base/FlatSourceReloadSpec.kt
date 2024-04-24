package io.github.nhubbard.konf.source.base

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.base.helpers.FlatConfigForLoad
import io.github.nhubbard.konf.source.base.helpers.flatSourceLoadContent
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object FlatSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.map.flat(flatSourceLoadContent)
        Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.map.flat(config.toFlatMap())
    }

    itBehavesLike(FlatSourceLoadBaseSpec)
})