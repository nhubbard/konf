package io.github.nhubbard.konf.source.xml

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object XmlProviderInJavaSpec : SubjectSpek<XmlProvider>({
    subject { XmlProvider.get() }

    itBehavesLike(XmlProviderSpec)
})