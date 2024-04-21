package io.github.nhubbard.konf

data class NetworkBufferForCast(
    val size: Int,
    val maxSize: Int,
    val name: String,
    val type: Type,
    val offset: Int?
) {

    @Suppress("unused")
    enum class Type {
        ON_HEAP, OFF_HEAP
    }
}