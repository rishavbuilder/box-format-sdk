package dev.box.writer

import dev.box.api.BoxContainer
import dev.box.api.BoxOptions
import dev.box.api.BoxWriter
import dev.box.core.constants.BoxConstants
import dev.box.core.model.*
import dev.box.serializer.BoxSerializer
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * Default BOX Writer implementation.
 */
class BoxWriterImpl : BoxWriter {

    override fun create(path: String): BoxResult<BoxContainer> {
        return create(path, BoxOptions())
    }

    override fun create(path: String, options: BoxOptions): BoxResult<BoxContainer> {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()

            val raf = RandomAccessFile(file, "rw")
            val serializer = BoxSerializer(raf)

            // Create initial header (offsets will be updated later)
            val header = BoxHeader(
                formatVersionMajor = BoxConstants.FORMAT_VERSION_MAJOR,
                formatVersionMinor = BoxConstants.FORMAT_VERSION_MINOR,
                headerVersionMajor = BoxConstants.HEADER_VERSION_MAJOR,
                headerVersionMinor = BoxConstants.HEADER_VERSION_MINOR,
                containerId = System.currentTimeMillis(),
                creationTimestamp = System.currentTimeMillis(),
                modificationTimestamp = System.currentTimeMillis()
            )

            // Write placeholder header
            serializer.writeHeader(header)

            // Write placeholder extension slots
            val extensionSlotsOffset = BoxConstants.HEADER_SIZE
            serializer.writeExtensionSlots()

            BoxResult.success(BoxWriterContainer(path, raf, serializer, options))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to create BOX: ${e.message}", e)
        }
    }

    override fun close() {
        // No persistent state to clean up
    }
}

/**
 * BOX Container implementation for writing.
 */
internal class BoxWriterContainer(
    override val path: String,
    private val raf: RandomAccessFile,
    private val serializer: BoxSerializer,
    private val options: BoxOptions
) : BoxContainer {

    private var closed = false
    private var modified = false

    // In-memory data structures
    private val folderEntries = mutableListOf<FolderEntry>()
    private val fileEntries = mutableListOf<FileEntry>()
    private val chunkEntries = mutableListOf<ChunkTableEntry>()
    private val metadataEntries = mutableMapOf<Int, String>()

    private var nextFolderId = 1 // 0 is reserved for ROOT_FOLDER_ID
    private var nextFileId = 1
    private var nextChunkId = 0L

    init {
        // Create root folder
        folderEntries.add(FolderEntry(
            entryId = BoxConstants.ROOT_FOLDER_ID.toInt(),
            parentId = FolderEntry.ROOT_PARENT_ID,
            name = "",
            childCount = 0,
            totalDescendantCount = 0
        ))
    }

    override val name: String
        get() = File(path).nameWithoutExtension

    override val isOpen: Boolean get() = !closed

    override val isModified: Boolean get() = modified

    override val fileCount: Int get() = fileEntries.size

    override val folderCount: Int get() = folderEntries.size

    override val totalSize: Long
        get() = fileEntries.sumOf { it.uncompressedSize }

    override val storageSize: Long
        get() = try {
            File(path).length()
        } catch (e: Exception) {
            0L
        }

    override fun find(path: String): BoxResult<BoxEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        val normalizedPath = normalizePath(path)

        // Search in folders
        for (folder in folderEntries) {
            val folderPath = getFolderPath(folder)
            if (folderPath == normalizedPath) {
                return BoxResult.success(BoxFolderEntry(
                    id = folder.entryId,
                    name = folder.name,
                    path = folderPath,
                    childCount = folder.childCount,
                    descendantCount = folder.totalDescendantCount
                ))
            }
        }

        // Search in files
        for (file in fileEntries) {
            val filePath = getFilePath(file)
            if (filePath == normalizedPath) {
                return BoxResult.success(BoxFileEntry(
                    id = file.entryId,
                    name = file.name,
                    path = filePath,
                    size = file.uncompressedSize,
                    compressedSize = file.compressedSize,
                    chunkCount = file.chunkCount,
                    firstChunkIndex = file.firstChunkIndex,
                    mimeType = file.mimeType
                ))
            }
        }

        return BoxResult.failure(ErrorCode.ENTRY_NOT_FOUND, "Entry not found: $path")
    }

    override fun findFile(path: String): BoxResult<BoxFileEntry> {
        val entry = find(path)
        if (entry.isFailure()) return BoxResult.failure(entry.error())
        val value = entry.value()
        if (value !is BoxFileEntry) {
            return BoxResult.failure(ErrorCode.ENTRY_IS_DIRECTORY, "Expected file, got folder: $path")
        }
        return BoxResult.success(value)
    }

    override fun findFolder(path: String): BoxResult<BoxFolderEntry> {
        val entry = find(path)
        if (entry.isFailure()) return BoxResult.failure(entry.error())
        val value = entry.value()
        if (value !is BoxFolderEntry) {
            return BoxResult.failure(ErrorCode.ENTRY_IS_FILE, "Expected folder, got file: $path")
        }
        return BoxResult.success(value)
    }

    override fun exists(path: String): Boolean {
        return find(path).isSuccess()
    }

    override fun isFile(path: String): Boolean {
        return findFile(path).isSuccess()
    }

    override fun isFolder(path: String): Boolean {
        return findFolder(path).isSuccess()
    }

    override fun entries(): List<BoxEntry> {
        if (closed) return emptyList()

        val result = mutableListOf<BoxEntry>()

        for (folder in folderEntries) {
            if (folder.entryId == BoxConstants.ROOT_FOLDER_ID.toInt()) continue
            val folderPath = getFolderPath(folder)
            result.add(BoxFolderEntry(
                id = folder.entryId,
                name = folder.name,
                path = folderPath,
                childCount = folder.childCount,
                descendantCount = folder.totalDescendantCount
            ))
        }

        for (file in fileEntries) {
            val filePath = getFilePath(file)
            result.add(BoxFileEntry(
                id = file.entryId,
                name = file.name,
                path = filePath,
                size = file.uncompressedSize,
                compressedSize = file.compressedSize,
                chunkCount = file.chunkCount,
                firstChunkIndex = file.firstChunkIndex,
                mimeType = file.mimeType
            ))
        }

        return result
    }

    override fun files(): List<BoxFileEntry> {
        if (closed) return emptyList()

        return fileEntries.map { file ->
            BoxFileEntry(
                id = file.entryId,
                name = file.name,
                path = getFilePath(file),
                size = file.uncompressedSize,
                compressedSize = file.compressedSize,
                chunkCount = file.chunkCount,
                firstChunkIndex = file.firstChunkIndex,
                mimeType = file.mimeType
            )
        }
    }

    override fun folders(): List<BoxFolderEntry> {
        if (closed) return emptyList()

        return folderEntries
            .filter { it.entryId != BoxConstants.ROOT_FOLDER_ID.toInt() }
            .map { folder ->
                BoxFolderEntry(
                    id = folder.entryId,
                    name = folder.name,
                    path = getFolderPath(folder),
                    childCount = folder.childCount,
                    descendantCount = folder.totalDescendantCount
                )
            }
    }

    override fun rootChildren(): List<BoxEntry> {
        if (closed) return emptyList()

        val result = mutableListOf<BoxEntry>()
        val rootId = BoxConstants.ROOT_FOLDER_ID.toInt()

        for (folder in folderEntries) {
            if (folder.parentId == rootId && folder.entryId != rootId) {
                result.add(BoxFolderEntry(
                    id = folder.entryId,
                    name = folder.name,
                    path = getFolderPath(folder),
                    childCount = folder.childCount,
                    descendantCount = folder.totalDescendantCount
                ))
            }
        }

        for (file in fileEntries) {
            if (file.parentFolderEntryId == rootId) {
                result.add(BoxFileEntry(
                    id = file.entryId,
                    name = file.name,
                    path = getFilePath(file),
                    size = file.uncompressedSize,
                    compressedSize = file.compressedSize,
                    chunkCount = file.chunkCount,
                    firstChunkIndex = file.firstChunkIndex,
                    mimeType = file.mimeType
                ))
            }
        }

        return result
    }

    override fun addFile(path: String, data: ByteArray): BoxResult<BoxFileEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        val normalizedPath = normalizePath(path)
        val parts = normalizedPath.split("/").filter { it.isNotEmpty() }

        if (parts.isEmpty()) {
            return BoxResult.failure(ErrorCode.INVALID_ARGUMENT, "Invalid file path: $path")
        }

        val fileName = parts.last()
        val parentPath = if (parts.size > 1) "/" + parts.dropLast(1).joinToString("/") else "/"

        // Find or create parent folder
        val parentResult = ensureFolderExists(parentPath)
        if (parentResult.isFailure()) {
            return BoxResult.failure(parentResult.error())
        }
        val parentFolder = parentResult.value()

        // Check if file already exists
        val existingFile = fileEntries.find { it.name == fileName && it.parentFolderEntryId == parentFolder.entryId }
        if (existingFile != null) {
            return BoxResult.failure(ErrorCode.ENTRY_EXISTS, "File already exists: $path")
        }

        // Create file entry
        val fileId = nextFileId++
        val fileEntry = FileEntry(
            entryId = fileId,
            parentFolderEntryId = parentFolder.entryId,
            name = fileName,
            uncompressedSize = data.size.toLong(),
            compressedSize = data.size.toLong(),
            chunkCount = 1,
            firstChunkIndex = chunkEntries.size,
            mimeType = guessMimeType(fileName),
            createdTimestamp = (System.currentTimeMillis() / 1000).toInt(),
            modifiedTimestamp = (System.currentTimeMillis() / 1000).toInt()
        )

        fileEntries.add(fileEntry)

        // We'll write the actual data during save()
        // For now, store it in a temporary map
        pendingFileData[fileId] = data

        modified = true
        return BoxResult.success(BoxFileEntry(
            id = fileId,
            name = fileName,
            path = normalizedPath,
            size = data.size.toLong(),
            mimeType = fileEntry.mimeType
        ))
    }

    override fun addFile(path: String, sourcePath: String): BoxResult<BoxFileEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        val sourceFile = File(sourcePath)
        if (!sourceFile.exists()) {
            return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "Source file not found: $sourcePath")
        }

        val data = sourceFile.readBytes()
        return addFile(path, data)
    }

    override fun remove(path: String): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Remove not supported in v0.1")
    }

    override fun rename(path: String, newName: String): BoxResult<BoxEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Rename not supported in v0.1")
    }

    override fun addFolder(path: String): BoxResult<BoxFolderEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        val normalizedPath = normalizePath(path)
        val result = ensureFolderExists(normalizedPath)
        if (result.isFailure()) {
            return BoxResult.failure(result.error())
        }

        val folder = result.value()
        modified = true
        return BoxResult.success(BoxFolderEntry(
            id = folder.entryId,
            name = folder.name,
            path = normalizedPath,
            childCount = folder.childCount,
            descendantCount = folder.totalDescendantCount
        ))
    }

    override fun extract(path: String, destinationPath: String): BoxResult<Unit> {
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Extract not supported in writer mode")
    }

    override fun extractAll(destinationPath: String): BoxResult<Unit> {
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "ExtractAll not supported in writer mode")
    }

    override fun metadata(): Map<Int, String> {
        if (closed) return emptyMap()
        return metadataEntries.toMap()
    }

    override fun getMetadata(key: Int): String? {
        if (closed) return null
        return metadataEntries[key]
    }

    override fun setMetadata(key: Int, value: String) {
        if (closed) return
        metadataEntries[key] = value
        modified = true
    }

    override fun removeMetadata(key: Int) {
        if (closed) return
        metadataEntries.remove(key)
        modified = true
    }

    override fun save(): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        try {
            // Reset file position
            raf.seek(0)

            // Calculate offsets
            var currentOffset = BoxConstants.HEADER_SIZE.toLong()
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.SECTION_ALIGNMENT)
            val metadataOffset = currentOffset.toInt()

            // Metadata section size: each entry = 2 (tagId) + 4 (length) + value bytes = 6 + value.size
            val metadataSize = if (metadataEntries.isNotEmpty()) {
                val dataSize = metadataEntries.entries.sumOf { 6 + it.value.toByteArray(Charsets.UTF_8).size }
                BoxConstants.SECTION_FRAME_SIZE + dataSize
            } else {
                BoxConstants.SECTION_FRAME_SIZE
            }
            currentOffset += metadataSize
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.SECTION_ALIGNMENT)
            val folderTreeOffset = currentOffset.toInt()

            // Folder tree section size
            var folderTreeDataSize = 0
            for (entry in folderEntries) {
                folderTreeDataSize += BoxConstants.FOLDER_ENTRY_SIZE + entry.name.toByteArray(Charsets.UTF_8).size
            }
            val folderTreeSize = BoxConstants.SECTION_FRAME_SIZE + folderTreeDataSize
            currentOffset += folderTreeSize
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.SECTION_ALIGNMENT)
            val fileIndexOffset = currentOffset.toInt()

            // File index section size
            var fileIndexDataSize = fileEntries.size * BoxConstants.FILE_ENTRY_SIZE
            for (entry in fileEntries) {
                fileIndexDataSize += entry.name.toByteArray(Charsets.UTF_8).size
                fileIndexDataSize += entry.mimeType.toByteArray(Charsets.UTF_8).size
            }
            val fileIndexSize = BoxConstants.SECTION_FRAME_SIZE + fileIndexDataSize
            currentOffset += fileIndexSize
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.SECTION_ALIGNMENT)
            val chunkTableOffset = currentOffset.toInt()

            // Chunk table section size (one chunk per file with data)
            val chunkCount = fileEntries.count { pendingFileData.containsKey(it.entryId) }
            val chunkTableSize = BoxConstants.SECTION_FRAME_SIZE + chunkCount * BoxConstants.CHUNK_TABLE_ENTRY_SIZE
            currentOffset += chunkTableSize
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.CHUNK_ALIGNMENT)
            val chunksOffset = currentOffset.toInt()

            // Seek to chunks offset before writing chunk data
            raf.seek(chunksOffset.toLong())

            // Write chunk data and build chunk table entries
            val chunksStartPosition = currentOffset
            chunkEntries.clear()
            val sha256 = MessageDigest.getInstance("SHA-256")

            for (fileEntry in fileEntries) {
                val fileData = pendingFileData[fileEntry.entryId] ?: ByteArray(0)
                if (fileData.isNotEmpty()) {
                    val chunkId = nextChunkId++

                    // Compute chunk checksum
                    sha256.reset()
                    val chunkChecksum = sha256.digest(fileData)

                    // Write chunk
                    val chunkDataOffset = serializer.writeChunk(
                        chunkId = chunkId,
                        entryId = fileEntry.entryId,
                        chunkIndex = 0,
                        payload = fileData
                    )

                    chunkEntries.add(ChunkTableEntry(
                        chunkId = chunkId,
                        entryId = fileEntry.entryId,
                        chunkIndex = 0,
                        chunkOffset = chunkDataOffset + BoxConstants.CHUNK_HEADER_SIZE,
                        chunkPayloadSize = fileData.size.toLong(),
                        chunkStoredSize = fileData.size.toLong(),
                        chunkFlags = 0,
                        checksumAlgorithm = BoxConstants.CHECKSUM_SHA256,
                        chunkChecksum = chunkChecksum
                    ))
                }
            }

            // Compute file-level checksums and fix firstChunkIndex (was set to 0 at addFile time)
            var chunkIdx = 0
            for (fileEntry in fileEntries) {
                val fileData = pendingFileData[fileEntry.entryId] ?: ByteArray(0)
                if (fileData.isNotEmpty()) {
                    sha256.reset()
                    val fileChecksum = sha256.digest(fileData)
                    val idx = fileEntries.indexOf(fileEntry)
                    fileEntries[idx] = fileEntry.copy(
                        firstChunkIndex = chunkIdx,
                        checksumAlgorithm = BoxConstants.CHECKSUM_SHA256,
                        fileChecksum = fileChecksum
                    )
                    chunkIdx++
                }
            }

            currentOffset = raf.filePointer
            val chunksSize = currentOffset - chunksStartPosition

            // Extension slots
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.SECTION_ALIGNMENT)
            val extensionSlotsOffset = currentOffset.toInt()
            currentOffset += BoxConstants.EXTENSION_SLOTS_TOTAL_SIZE

            // Footer
            currentOffset = BoxConstants.align(currentOffset, BoxConstants.SECTION_ALIGNMENT)
            val footerOffset = currentOffset.toInt()
            val containerSize = footerOffset + BoxConstants.FOOTER_SIZE

            // Build header
            val header = BoxHeader(
                formatVersionMajor = BoxConstants.FORMAT_VERSION_MAJOR,
                formatVersionMinor = BoxConstants.FORMAT_VERSION_MINOR,
                headerVersionMajor = BoxConstants.HEADER_VERSION_MAJOR,
                headerVersionMinor = BoxConstants.HEADER_VERSION_MINOR,
                containerId = System.currentTimeMillis(),
                creationTimestamp = System.currentTimeMillis(),
                modificationTimestamp = System.currentTimeMillis(),
                entryCount = folderEntries.size + fileEntries.size,
                fileEntryCount = fileEntries.size,
                folderEntryCount = folderEntries.size,
                totalDataSize = fileEntries.sumOf { it.uncompressedSize },
                checksumAlgorithm = BoxConstants.CHECKSUM_SHA256,
                metadataOffset = metadataOffset,
                metadataSize = metadataSize,
                folderTreeOffset = folderTreeOffset,
                folderTreeSize = folderTreeSize,
                fileIndexOffset = fileIndexOffset,
                fileIndexSize = fileIndexSize,
                chunkTableOffset = chunkTableOffset,
                chunkTableSize = chunkTableSize,
                chunksOffset = chunksOffset,
                chunksSize = chunksSize,
                extensionSlotsOffset = extensionSlotsOffset,
                extensionSlotsSize = BoxConstants.EXTENSION_SLOTS_TOTAL_SIZE,
                footerOffset = footerOffset,
                footerSize = BoxConstants.FOOTER_SIZE
            )

            // Compute childCount and totalDescendantCount for folder entries
            for (entry in folderEntries) {
                val directChildren = folderEntries.count { it.parentId == entry.entryId } +
                    fileEntries.count { it.parentFolderEntryId == entry.entryId }
                val totalDescendants = computeDescendantCount(entry.entryId)
                val idx = folderEntries.indexOf(entry)
                folderEntries[idx] = entry.copy(
                    childCount = directChildren,
                    totalDescendantCount = totalDescendants
                )
            }

            // Write header (without checksum first)
            raf.seek(0)
            serializer.writeHeader(header)

            // Write metadata
            raf.seek(metadataOffset.toLong())
            if (metadataEntries.isNotEmpty()) {
                serializer.writeMetadata(metadataEntries)
            } else {
                // Write empty metadata section frame
                serializer.writeSectionFrame(SectionFrame(
                    sectionType = BoxConstants.SECTION_TYPE_META,
                    sectionSize = 0L,
                    sectionCrc32 = 0
                ))
            }

            // Write folder entries
            raf.seek(folderTreeOffset.toLong())
            serializer.writeFolderEntries(folderEntries)

            // Write file entries
            raf.seek(fileIndexOffset.toLong())
            serializer.writeFileEntries(fileEntries)

            // Write chunk table entries
            raf.seek(chunkTableOffset.toLong())
            serializer.writeChunkTableEntries(chunkEntries)

            // Write footer
            raf.seek(footerOffset.toLong())
            serializer.writeFooter(BoxFooter(
                headerChecksumOffset = 0,
                containerSize = containerSize.toLong()
            ))

            // Update header with checksum
            raf.seek(0)
            serializer.updateHeaderChecksum()

            // Update header offsets (with checksum already written)
            raf.seek(0)
            serializer.updateHeaderOffsets(header)

            modified = false
            pendingFileData.clear()

            return BoxResult.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            return BoxResult.failure(ErrorCode.IO_FAILED, "Failed to save BOX: ${e.message}", e)
        }
    }

    override fun saveAs(path: String): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        // Create a new writer and copy
        val newWriter = BoxWriterImpl()
        val newContainer = newWriter.create(path)
        if (newContainer.isFailure()) {
            return BoxResult.failure(newContainer.error())
        }

        // Copy all data
        val container = newContainer.value()
        for (folder in folderEntries) {
            if (folder.entryId == BoxConstants.ROOT_FOLDER_ID.toInt()) continue
            container.addFolder(getFolderPath(folder))
        }

        for ((fileId, data) in pendingFileData) {
            val fileEntry = fileEntries.find { it.entryId == fileId }
            if (fileEntry != null) {
                container.addFile(getFilePath(fileEntry), data)
            }
        }

        for ((key, value) in metadataEntries) {
            container.setMetadata(key, value)
        }

        container.save()
        container.close()

        return BoxResult.success(Unit)
    }

    override fun close() {
        if (!closed) {
            closed = true
            try {
                raf.close()
            } catch (e: Exception) {
                // Ignore close errors
            }
        }
    }

    override fun validate(): BoxResult<Boolean> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        return BoxResult.success(true)
    }

    // Private helpers

    private val pendingFileData = mutableMapOf<Int, ByteArray>()

    private fun ensureFolderExists(path: String): BoxResult<FolderEntry> {
        val normalizedPath = normalizePath(path)
        if (normalizedPath == "/") {
            return BoxResult.success(folderEntries.first { it.entryId == BoxConstants.ROOT_FOLDER_ID.toInt() })
        }

        // Check if folder already exists
        val existing = folderEntries.find { getFolderPath(it) == normalizedPath }
        if (existing != null) {
            return BoxResult.success(existing)
        }

        // Create folder hierarchy
        val parts = normalizedPath.split("/").filter { it.isNotEmpty() }
        var currentParentId = BoxConstants.ROOT_FOLDER_ID.toInt()
        var currentPath = ""

        for (part in parts) {
            currentPath = if (currentPath.isEmpty()) "/$part" else "$currentPath/$part"

            val existingFolder = folderEntries.find {
                it.name == part && it.parentId == currentParentId
            }

            if (existingFolder != null) {
                currentParentId = existingFolder.entryId
            } else {
                val folderId = nextFolderId++
                val newFolder = FolderEntry(
                    entryId = folderId,
                    parentId = currentParentId,
                    name = part,
                    childCount = 0,
                    totalDescendantCount = 0
                )
                folderEntries.add(newFolder)
                currentParentId = folderId
            }
        }

        val folder = folderEntries.find { getFolderPath(it) == normalizedPath }
            ?: return BoxResult.failure(ErrorCode.INVALID_STATE, "Failed to create folder: $path")

        return BoxResult.success(folder)
    }

    private fun getFolderPath(folder: FolderEntry): String {
        if (folder.parentId == FolderEntry.ROOT_PARENT_ID) {
            return if (folder.name.isEmpty()) "/" else "/${folder.name}"
        }
        val parent = folderEntries.find { it.entryId == folder.parentId } ?: return "/${folder.name}"
        val parentPath = getFolderPath(parent)
        return if (parentPath.endsWith("/")) "$parentPath${folder.name}" else "$parentPath/${folder.name}"
    }

    private fun computeDescendantCount(folderId: Int): Int {
        var count = 0
        val children = folderEntries.filter { it.parentId == folderId }
        for (child in children) {
            count += 1 + computeDescendantCount(child.entryId)
        }
        count += fileEntries.count { it.parentFolderEntryId == folderId }
        return count
    }

    private fun getFilePath(file: FileEntry): String {
        if (file.parentFolderEntryId == FolderEntry.ROOT_PARENT_ID) {
            return "/${file.name}"
        }
        val parent = folderEntries.find { it.entryId == file.parentFolderEntryId } ?: return "/${file.name}"
        val parentPath = getFolderPath(parent)
        return if (parentPath.endsWith("/")) "$parentPath${file.name}" else "$parentPath/${file.name}"
    }

    private fun normalizePath(path: String): String {
        if (path == "/") return "/"
        return if (path.startsWith("/")) path else "/$path"
    }

    private fun guessMimeType(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "mp3" -> "audio/mpeg"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
    }
}
