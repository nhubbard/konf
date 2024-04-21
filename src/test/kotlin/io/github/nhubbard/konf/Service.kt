package io.github.nhubbard.konf

object Service : ConfigSpec() {
    val name by optional("test")

    object Backend : ConfigSpec() {
        val host by optional("127.0.0.1")
        val port by optional(7777)

        object Login : ConfigSpec() {
            val user by optional("admin")
            val password by optional("123456")
        }
    }

    object UI : ConfigSpec() {
        val host by optional("127.0.0.1")
        val port by optional(8888)
    }
}