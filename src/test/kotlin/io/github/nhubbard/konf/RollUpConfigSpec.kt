package io.github.nhubbard.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.sameInstance
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object RollUpConfigSpec : SubjectSpek<Config>({

    subject { Prefix("prefix") + Config { addSpec(NetworkBuffer) } }

    configTestSpec("prefix.network.buffer")

    on("prefix is empty string") {
        it("should return itself") {
            assertThat(Prefix() + subject, sameInstance(subject))
        }
    }
})