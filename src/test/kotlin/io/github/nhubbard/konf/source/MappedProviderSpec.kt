package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.source.properties.PropertiesProvider
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike

object MappedProviderSpec : SubjectSpek<Provider>({
    subject { PropertiesProvider.map { source -> source.withPrefix("prefix")["prefix"] } }

    itBehavesLike(ProviderSpec)
})