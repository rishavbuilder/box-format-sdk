package dev.box.core.model

/**
 * Chunk table entry.
 * Fixed size: 64 bytes.
 */
data class ChunkTableEntry(
    val chunkId: Long,
    val entryId: Int,
    val chunkIndex: Int,
    val chunkOffset: Long,
    val chunkPayloadSize: Long,
    val chunkStoredSize: Long,
    val chunkFlags: Int = 0,
    val checksumAlgorithm: Int = 0,
    val chunkChecksum: ByteArray = ByteArray(32)
) {
    // Flag bits
    val isCompressed: Boolean get() = chunkFlags and 0x01 != 0
    val isEncrypted: Boolean get() = chunkFlags and 0x02 != 0
    val isLast: Boolean get() = chunkFlags and 0x04 != 0
    val isRecovery: Boolean get() = chunkFlags and 0x08 != 0
    val isThumbnail: Boolean get() = chunkFlags and 0x10 != 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkTableEntry) return false
        return chunkId == other.chunkId && entryId == other.entryId
    }

    override fun hashCode(): Int {
        var result = chunkId.hashCode()
        result = 31 * result + entryId
        return result
    }
}

/**
 * Chunk header in the Chunks region.
 * Fixed size: 32 bytes.
 */
data class ChunkHeader(
    val chunkId: Long,
    val entryId: Int,
    val chunkIndex: Int,
    val chunkFlags: Int,
    val payloadSize: Int
)
