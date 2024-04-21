package io.github.nhubbard.konf

import org.jetbrains.spek.subject.SubjectSpek

object MultiLayerRollUpFallbackConfigSpec : SubjectSpek<Config>({
    subject {
        (
                (
                        Prefix("prefix") +
                                Config { addSpec(NetworkBuffer) }.withLayer("layer1")
                        ).withLayer("layer2") +
                        Config()
                ).withLayer("layer3")
    }

    configTestSpec("prefix.network.buffer")
})