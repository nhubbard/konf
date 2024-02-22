package io.github.nhubbard.konf.snippet

import io.github.nhubbard.konf.ConfigSpec

object ServerSpec : ConfigSpec() {
    val host by optional("0.0.0.0")
    val tcpPort by required<Int>()
}