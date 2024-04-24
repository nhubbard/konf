package io.github.nhubbard.konf.source.properties

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.ConfigForLoad
import io.github.nhubbard.konf.source.base.FlatConfigForLoad
import io.github.nhubbard.konf.source.base.FlatSourceLoadBaseSpec
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object PropertiesSourceReloadSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.properties.resource("source/source.properties")
        val properties = config.toProperties.toText()
        Config {
            addSpec(ConfigForLoad)
            addSpec(FlatConfigForLoad)
        }.from.properties.string(properties)
    }

    itBehavesLike(FlatSourceLoadBaseSpec)
})