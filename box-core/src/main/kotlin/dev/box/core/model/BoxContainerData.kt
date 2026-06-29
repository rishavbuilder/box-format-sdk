package dev.box.core.model

import dev.box.core.constants.BoxConstants

/**
 * Complete parsed BOX container structure.
 */
data class BoxContainerData(
    val header: BoxHeader,
    val footer: BoxFooter,
    val metadata: Map<Int, String> = emptyMap(),
    val folderEntries: List<FolderEntry> = emptyList(),
    val fileEntries: List<FileEntry> = emptyList(),
    val chunkEntries: List<ChunkTableEntry> = emptyList()
) {
    val rootFolder: FolderEntry?
        get() = folderEntries.find { it.entryId == BoxConstants.ROOT_FOLDER_ID.toInt() }

    fun getFolderById(id: Int): FolderEntry? = folderEntries.find { it.entryId == id }

    fun getFileById(id: Int): FileEntry? = fileEntries.find { it.entryId == id }

    fun getChunksForFile(fileEntry: FileEntry): List<ChunkTableEntry> {
        if (fileEntry.chunkCount == 0) return emptyList()
        val startIndex = fileEntry.firstChunkIndex
        return chunkEntries.subList(startIndex, startIndex + fileEntry.chunkCount)
    }

    fun getFolderPath(folderEntry: FolderEntry): String {
        if (folderEntry.parentId == FolderEntry.ROOT_PARENT_ID) {
            return "/${folderEntry.name}"
        }
        val parent = getFolderById(folderEntry.parentId) ?: return "/${folderEntry.name}"
        val parentPath = getFolderPath(parent)
        return if (parentPath.endsWith("/")) "$parentPath${folderEntry.name}" else "$parentPath/${folderEntry.name}"
    }

    fun getFilePath(fileEntry: FileEntry): String {
        if (fileEntry.parentFolderEntryId == FolderEntry.ROOT_PARENT_ID) {
            return "/${fileEntry.name}"
        }
        val parent = getFolderById(fileEntry.parentFolderEntryId) ?: return "/${fileEntry.name}"
        val parentPath = getFolderPath(parent)
        return if (parentPath.endsWith("/")) "$parentPath${fileEntry.name}" else "$parentPath/${fileEntry.name}"
    }

    fun buildPathTree(): Map<Int, String> {
        val pathMap = mutableMapOf<Int, String>()

        // First pass: folders
        for (folder in folderEntries) {
            pathMap[folder.entryId] = getFolderPath(folder)
        }

        // Second pass: files
        for (file in fileEntries) {
            pathMap[file.entryId] = getFilePath(file)
        }

        return pathMap
    }
}
