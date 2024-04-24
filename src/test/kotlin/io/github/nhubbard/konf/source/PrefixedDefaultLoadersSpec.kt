package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.DefaultLoadersConfig
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object PrefixedDefaultLoadersSpec : SubjectSpek<DefaultLoaders>({
    subject {
        Config {
            addSpec(DefaultLoadersConfig.withPrefix("prefix"))
        }.from.prefixed("prefix")
    }

    itBehavesLike(DefaultLoadersSpec)
})