package io.github.nhubbard.konf.source.toml

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object TomlSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.toml.resource("source/source.toml")
        val toml = config.toToml.toText()
        Config {
            addSpec(ConfigForLoad)
        }.from.toml.string(toml)
    }

    itBehavesLike(SourceLoadBaseSpec)
})