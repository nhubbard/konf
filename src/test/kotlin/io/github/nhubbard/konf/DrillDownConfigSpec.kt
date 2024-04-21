package io.github.nhubbard.konf

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.sameInstance
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.subject.SubjectSpek

object DrillDownConfigSpec : SubjectSpek<Config>({

    subject { Config { addSpec(NetworkBuffer) }.at("network") }

    configTestSpec("buffer")

    on("path is empty string") {
        it("should return itself") {
            assertThat(subject.at(""), sameInstance(subject))
        }
    }
})