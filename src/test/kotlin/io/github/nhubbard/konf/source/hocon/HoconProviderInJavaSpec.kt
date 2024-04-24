package io.github.nhubbard.konf.source.hocon

import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object HoconProviderInJavaSpec : SubjectSpek<HoconProvider>({
    subject { HoconProvider.get() }

    itBehavesLike(HoconProviderSpec)
})