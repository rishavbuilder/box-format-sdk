package dev.box.core.model

/**
 * High-level BOX entry model used by the API.
 * Can represent a file or folder.
 */
sealed class BoxEntry {
    abstract val id: Int
    abstract val name: String
    abstract val path: String
    abstract val type: EntryType
    abstract val createdAt: Long
    abstract val modifiedAt: Long
    abstract val parentPath: String?

    val isFile: Boolean get() = type == EntryType.FILE
    val isFolder: Boolean get() = type == EntryType.FOLDER

    fun childPath(childName: String): String {
        return if (path.endsWith("/")) "$path$childName" else "$path/$childName"
    }
}

/**
 * BOX File entry.
 */
data class BoxFileEntry(
    override val id: Int,
    override val name: String,
    override val path: String,
    override val createdAt: Long = 0,
    override val modifiedAt: Long = 0,
    val size: Long = 0,
    val compressedSize: Long = 0,
    val chunkCount: Int = 0,
    val firstChunkIndex: Int = 0,
    val mimeType: String = "",
    val isEncrypted: Boolean = false,
    val isCompressed: Boolean = false,
    val checksum: String = ""
) : BoxEntry() {
    override val type: EntryType = EntryType.FILE
    override val parentPath: String?
        get() {
            val lastSlash = path.lastIndexOf('/')
            return if (lastSlash >= 0) path.substring(0, lastSlash).ifEmpty { "/" } else null
        }

    val extension: String?
        get() {
            val dotIndex = name.lastIndexOf('.')
            return if (dotIndex >= 0 && dotIndex < name.length - 1) {
                name.substring(dotIndex + 1)
            } else {
                null
            }
        }
}

/**
 * BOX Folder entry.
 */
data class BoxFolderEntry(
    override val id: Int,
    override val name: String,
    override val path: String,
    override val createdAt: Long = 0,
    override val modifiedAt: Long = 0,
    val childCount: Int = 0,
    val descendantCount: Int = 0
) : BoxEntry() {
    override val type: EntryType = EntryType.FOLDER
    override val parentPath: String?
        get() {
            if (path == "/") return null
            val lastSlash = path.lastIndexOf('/')
            return if (lastSlash >= 0) path.substring(0, lastSlash).ifEmpty { "/" } else null
        }
}
