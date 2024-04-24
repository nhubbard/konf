package io.github.nhubbard.konf.source.toml

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object TomlProviderInJavaSpec : SubjectSpek<TomlProvider>({
    subject { TomlProvider.get() }

    itBehavesLike(TomlProviderSpec)
})