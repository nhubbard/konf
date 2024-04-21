package io.github.nhubbard.konf

import org.jetbrains.spek.subject.SubjectSpek

object MultiLayerRollUpConfigSpec : SubjectSpek<Config>({

    subject { (Prefix("prefix") + Config { addSpec(NetworkBuffer) }).withLayer("multi-layer") }

    configTestSpec("prefix.network.buffer")
})