package dev.box.core.model

/**
 * Folder entry in the Folder Tree section.
 * Fixed size: 32 bytes.
 */
data class FolderEntry(
    val entryId: Int,
    val parentId: Int,
    val childCount: Int = 0,
    val totalDescendantCount: Int = 0,
    val name: String,
    val flags: Int = 0,
    val nameCrc32: Int = 0
) {
    companion object {
        const val ROOT_PARENT_ID = 0xFFFFFFFF.toInt()
    }
}

/**
 * File entry in the File Index section.
 * Fixed size: 96 bytes (excluding name and MIME type strings).
 */
data class FileEntry(
    val entryId: Int,
    val parentFolderEntryId: Int,
    val name: String,
    val flags: Int = 0,
    val uncompressedSize: Long = 0,
    val compressedSize: Long = 0,
    val chunkCount: Int = 0,
    val firstChunkIndex: Int = 0,
    val checksumAlgorithm: Int = 0,
    val fileChecksum: ByteArray = ByteArray(32),
    val thumbnailOffset: Int = 0,
    val thumbnailSize: Int = 0,
    val mimeType: String = "",
    val createdTimestamp: Int = 0,
    val modifiedTimestamp: Int = 0,
    val nameCrc32: Int = 0
) {
    // Flag bits
    val isSymlink: Boolean get() = flags and 0x01 != 0
    val isHardlink: Boolean get() = flags and 0x02 != 0
    val isExecutable: Boolean get() = flags and 0x04 != 0
    val isHidden: Boolean get() = flags and 0x08 != 0
    val isEncrypted: Boolean get() = flags and 0x10 != 0
    val isCompressed: Boolean get() = flags and 0x20 != 0
    val hasThumbnail: Boolean get() = flags and 0x40 != 0
    val hasCustomMetadata: Boolean get() = flags and 0x80 != 0

    val extension: String?
        get() {
            val dotIndex = name.lastIndexOf('.')
            return if (dotIndex >= 0 && dotIndex < name.length - 1) {
                name.substring(dotIndex + 1)
            } else {
                null
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileEntry) return false
        return entryId == other.entryId && name == other.name
    }

    override fun hashCode(): Int {
        var result = entryId
        result = 31 * result + name.hashCode()
        return result
    }
}
