package io.github.nhubbard.konf.source

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = SealedClassDeserializer::class)
sealed class SealedClass

data class VariantA(val int: Int) : SealedClass()
data class VariantB(val double: Double) : SealedClass()