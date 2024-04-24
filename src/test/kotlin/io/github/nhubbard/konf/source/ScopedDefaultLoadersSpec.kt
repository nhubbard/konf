package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object ScopedDefaultLoadersSpec : SubjectSpek<DefaultLoaders>({
    subject {
        Config {
            addSpec(DefaultLoadersConfig["source"])
        }.from.scoped("source")
    }

    itBehavesLike(DefaultLoadersSpec)
})