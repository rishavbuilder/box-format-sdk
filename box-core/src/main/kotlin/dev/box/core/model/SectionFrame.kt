package dev.box.core.model

/**
 * Section frame header.
 * Fixed size: 20 bytes.
 */
data class SectionFrame(
    val sectionType: ByteArray, // 4 bytes
    val sectionSize: Long,     // 8 bytes
    val sectionCrc32: Int,     // 4 bytes
    val reserved: Int = 0      // 4 bytes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SectionFrame) return false
        return sectionType.contentEquals(other.sectionType) && sectionSize == other.sectionSize
    }

    override fun hashCode(): Int {
        var result = sectionType.contentHashCode()
        result = 31 * result + sectionSize.hashCode()
        return result
    }
}
