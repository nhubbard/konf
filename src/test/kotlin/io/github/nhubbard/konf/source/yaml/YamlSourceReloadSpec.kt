package io.github.nhubbard.konf.source.yaml

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.SourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object YamlSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.yaml.resource("source/source.yaml")
        val yaml = config.toYaml.toText()
        Config {
            addSpec(ConfigForLoad)
        }.from.yaml.string(yaml)
    }

    itBehavesLike(SourceLoadBaseSpec)
})