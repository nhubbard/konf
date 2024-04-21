package io.github.nhubbard.konf

import org.jetbrains.spek.subject.SubjectSpek

object MultiLayerDrillDownConfigSpec : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBuffer) }.at("network").withLayer("multi-layer") }

    configTestSpec("buffer")
})