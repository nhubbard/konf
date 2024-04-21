package io.github.nhubbard.konf

import org.jetbrains.spek.subject.SubjectSpek

object DrillDownMultiLayerConfigSpec : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBuffer) }.withLayer("multi-layer").at("network") }

    configTestSpec("buffer")
})