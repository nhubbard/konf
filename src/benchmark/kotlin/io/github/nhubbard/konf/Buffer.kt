package io.github.nhubbard.konf

class Buffer {
    companion object : ConfigSpec("network.buffer") {
        val name by optional("buffer", description = "name of buffer")
    }
}