package io.github.nhubbard.konf.source.js

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object JsProviderInJavaSpec : SubjectSpek<JsProvider>({
    subject { JsProvider.get() }

    itBehavesLike(JsProviderSpec)
})