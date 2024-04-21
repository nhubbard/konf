package io.github.nhubbard.konf

import org.jetbrains.spek.subject.SubjectSpek

object MultiLayerFacadeDrillDownConfigSpec : SubjectSpek<Config>({
    subject {
        (Config() + Config {
            addSpec(NetworkBuffer)
        }.withLayer("layer1").at("network").withLayer("layer2")).withLayer("layer3")
    }

    configTestSpec("buffer")
})