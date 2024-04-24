package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object MappedDefaultLoadersSpec : SubjectSpek<DefaultLoaders>({
    subject {
        Config {
            addSpec(DefaultLoadersConfig["source"])
        }.from.mapped { it["source"] }
    }

    itBehavesLike(DefaultLoadersSpec)
})