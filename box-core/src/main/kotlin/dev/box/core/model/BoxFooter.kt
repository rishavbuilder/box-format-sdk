package dev.box.core.model

/**
 * BOX Container Footer.
 * Fixed size: 128 bytes.
 */
data class BoxFooter(
    val headerChecksumOffset: Long = 0,
    val containerSize: Long = 0,
    val reserved: ByteArray = ByteArray(112)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoxFooter) return false
        return headerChecksumOffset == other.headerChecksumOffset && containerSize == other.containerSize
    }

    override fun hashCode(): Int {
        var result = headerChecksumOffset.hashCode()
        result = 31 * result + containerSize.hashCode()
        return result
    }
}
