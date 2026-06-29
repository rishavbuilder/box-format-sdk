package dev.box.core.model

/**
 * BOX Container Header.
 * Fixed size: 256 bytes.
 */
data class BoxHeader(
    val formatVersionMajor: Int = 1,
    val formatVersionMinor: Int = 0,
    val headerVersionMajor: Int = 1,
    val headerVersionMinor: Int = 0,
    val containerId: Long = 0,
    val creationTimestamp: Long = 0,
    val modificationTimestamp: Long = 0,
    val flags: Int = 0,
    val entryCount: Int = 0,
    val fileEntryCount: Int = 0,
    val folderEntryCount: Int = 0,
    val totalDataSize: Long = 0,
    val compressionAlgorithm: Int = 0,
    val encryptionAlgorithm: Int = 0,
    val checksumAlgorithm: Int = 0,
    val metadataOffset: Int = 0,
    val metadataSize: Int = 0,
    val folderTreeOffset: Int = 0,
    val folderTreeSize: Int = 0,
    val fileIndexOffset: Int = 0,
    val fileIndexSize: Int = 0,
    val chunkTableOffset: Int = 0,
    val chunkTableSize: Int = 0,
    val chunksOffset: Int = 0,
    val chunksSize: Long = 0,
    val extensionSlotsOffset: Int = 0,
    val extensionSlotsSize: Int = 0,
    val footerOffset: Int = 0,
    val footerSize: Int = 0,
    val headerChecksum: ByteArray = ByteArray(32),
    val headerEndMarker: ByteArray = ByteArray(8)
) {
    // Flag bits
    val isCompressionEnabled: Boolean get() = flags and 0x01 != 0
    val isEncryptionEnabled: Boolean get() = flags and 0x02 != 0
    val isSignatureEnabled: Boolean get() = flags and 0x04 != 0
    val isStreamingMode: Boolean get() = flags and 0x08 != 0
    val isRecoveryMode: Boolean get() = flags and 0x10 != 0
    val isThumbnailsEnabled: Boolean get() = flags and 0x20 != 0
    val isSearchIndexEnabled: Boolean get() = flags and 0x40 != 0

    val formatVersion: String get() = "$formatVersionMajor.$formatVersionMinor"
    val headerVersion: String get() = "$headerVersionMajor.$headerVersionMinor"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoxHeader) return false
        return formatVersionMajor == other.formatVersionMajor &&
                formatVersionMinor == other.formatVersionMinor &&
                containerId == other.containerId
    }

    override fun hashCode(): Int {
        var result = formatVersionMajor
        result = 31 * result + formatVersionMinor
        result = 31 * result + containerId.hashCode()
        return result
    }
}
