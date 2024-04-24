package io.github.nhubbard.konf.source.yaml

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object YamlProviderInJavaSpec : SubjectSpek<YamlProvider>({
    subject { YamlProvider.get() }

    itBehavesLike(YamlProviderSpec)
})