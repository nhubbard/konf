package io.github.nhubbard.konf.source.env

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object EnvProviderInJavaSpec : SubjectSpek<EnvProvider>({
    subject { EnvProvider.get() }

    itBehavesLike(EnvProviderSpec)
})