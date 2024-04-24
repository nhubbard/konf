package io.github.nhubbard.konf.source.json

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object JsonProviderInJavaSpec : SubjectSpek<JsonProvider>({
    subject { JsonProvider.get() }

    itBehavesLike(JsonProviderSpec)
})