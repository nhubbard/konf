package io.github.nhubbard.konf.source.base

import java.io.Serializable

data class ClassForLoad(
    val stringWithComma: String,
    val emptyList: List<Int>,
    val emptySet: Set<Int>,
    val emptyArray: IntArray,
    val emptyObjectArray: Array<Int>,
    val singleElementList: List<Int>,
    val multipleElementsList: List<Int>
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassForLoad

        if (stringWithComma != other.stringWithComma) return false
        if (emptyList != other.emptyList) return false
        if (emptySet != other.emptySet) return false
        if (!emptyArray.contentEquals(other.emptyArray)) return false
        if (!emptyObjectArray.contentEquals(other.emptyObjectArray)) return false
        if (singleElementList != other.singleElementList) return false
        if (multipleElementsList != other.multipleElementsList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stringWithComma.hashCode()
        result = 31 * result + emptyList.hashCode()
        result = 31 * result + emptySet.hashCode()
        result = 31 * result + emptyArray.contentHashCode()
        result = 31 * result + emptyObjectArray.contentHashCode()
        result = 31 * result + singleElementList.hashCode()
        result = 31 * result + multipleElementsList.hashCode()
        return result
    }
}