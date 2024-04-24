package io.github.nhubbard.konf.source.properties

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object PropertiesProviderInJavaSpec : SubjectSpek<PropertiesProvider>({
    subject { PropertiesProvider.get() }

    itBehavesLike(PropertiesProviderSpec)
})