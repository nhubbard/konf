package io.github.nhubbard.konf.source.hocon

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

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