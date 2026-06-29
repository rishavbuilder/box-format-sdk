package dev.box.reader

import dev.box.api.BoxContainer
import dev.box.api.BoxReader
import dev.box.core.constants.BoxConstants
import dev.box.core.model.*
import dev.box.parser.BoxParser
import java.io.File
import java.io.RandomAccessFile

/**
 * Default BOX Reader implementation.
 */
class BoxReaderImpl : BoxReader {

    override fun open(path: String): BoxResult<BoxContainer> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "File not found: $path")
            }

            val raf = RandomAccessFile(file, "rw")
            val parser = BoxParser(raf)

            // Validate magic bytes
            val headerResult = parser.readHeader()
            if (headerResult.isFailure()) {
                raf.close()
                return BoxResult.failure(headerResult.error())
            }

            val header = headerResult.value()

            // Validate header checksum
            val checksumResult = parser.validateHeaderChecksum(header)
            if (checksumResult.isFailure()) {
                raf.close()
                return BoxResult.failure(checksumResult.error())
            }

            // Read footer
            val footerResult = parser.readFooter(header.footerOffset)
            if (footerResult.isFailure()) {
                raf.close()
                return BoxResult.failure(footerResult.error())
            }

            // Read metadata
            val metadata = if (header.metadataSize > 0) {
                val metadataResult = parser.readMetadata(header.metadataOffset, header.metadataSize)
                if (metadataResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(metadataResult.error())
                }
                metadataResult.value()
            } else {
                emptyMap()
            }

            // Read folder entries
            val folderEntries = if (header.folderTreeSize > 0) {
                val folderResult = parser.readFolderEntries(header.folderTreeOffset, header.folderTreeSize)
                if (folderResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(folderResult.error())
                }
                folderResult.value()
            } else {
                emptyList()
            }

            // Read file entries
            val fileEntries = if (header.fileIndexSize > 0) {
                val fileResult = parser.readFileEntries(header.fileIndexOffset, header.fileIndexSize)
                if (fileResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(fileResult.error())
                }
                fileResult.value()
            } else {
                emptyList()
            }

            // Read chunk table entries
            val chunkEntries = if (header.chunkTableSize > 0) {
                val chunkResult = parser.readChunkTableEntries(header.chunkTableOffset, header.chunkTableSize)
                if (chunkResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(chunkResult.error())
                }
                chunkResult.value()
            } else {
                emptyList()
            }

            val containerData = BoxContainerData(
                header = header,
                footer = footerResult.value(),
                metadata = metadata,
                folderEntries = folderEntries,
                fileEntries = fileEntries,
                chunkEntries = chunkEntries
            )

            BoxResult.success(BoxContainerImpl(path, raf, parser, containerData, readOnly = false))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to open BOX: ${e.message}", e)
        }
    }

    override fun openReadOnly(path: String): BoxResult<BoxContainer> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "File not found: $path")
            }

            val raf = RandomAccessFile(file, "r")
            val parser = BoxParser(raf)

            // Validate magic bytes
            val headerResult = parser.readHeader()
            if (headerResult.isFailure()) {
                raf.close()
                return BoxResult.failure(headerResult.error())
            }

            val header = headerResult.value()

            // Validate header checksum
            val checksumResult = parser.validateHeaderChecksum(header)
            if (checksumResult.isFailure()) {
                raf.close()
                return BoxResult.failure(checksumResult.error())
            }

            // Read footer
            val footerResult = parser.readFooter(header.footerOffset)
            if (footerResult.isFailure()) {
                raf.close()
                return BoxResult.failure(footerResult.error())
            }

            // Read metadata
            val metadata = if (header.metadataSize > 0) {
                val metadataResult = parser.readMetadata(header.metadataOffset, header.metadataSize)
                if (metadataResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(metadataResult.error())
                }
                metadataResult.value()
            } else {
                emptyMap()
            }

            // Read folder entries
            val folderEntries = if (header.folderTreeSize > 0) {
                val folderResult = parser.readFolderEntries(header.folderTreeOffset, header.folderTreeSize)
                if (folderResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(folderResult.error())
                }
                folderResult.value()
            } else {
                emptyList()
            }

            // Read file entries
            val fileEntries = if (header.fileIndexSize > 0) {
                val fileResult = parser.readFileEntries(header.fileIndexOffset, header.fileIndexSize)
                if (fileResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(fileResult.error())
                }
                fileResult.value()
            } else {
                emptyList()
            }

            // Read chunk table entries
            val chunkEntries = if (header.chunkTableSize > 0) {
                val chunkResult = parser.readChunkTableEntries(header.chunkTableOffset, header.chunkTableSize)
                if (chunkResult.isFailure()) {
                    raf.close()
                    return BoxResult.failure(chunkResult.error())
                }
                chunkResult.value()
            } else {
                emptyList()
            }

            val containerData = BoxContainerData(
                header = header,
                footer = footerResult.value(),
                metadata = metadata,
                folderEntries = folderEntries,
                fileEntries = fileEntries,
                chunkEntries = chunkEntries
            )

            BoxResult.success(BoxContainerImpl(path, raf, parser, containerData, readOnly = true))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to open BOX: ${e.message}", e)
        }
    }

    override fun isBoxFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists() || file.length() < BoxConstants.HEADER_SIZE) {
                return false
            }

            val raf = RandomAccessFile(file, "r")
            val magic = ByteArray(4)
            raf.readFully(magic)
            raf.close()

            magic.contentEquals(BoxConstants.MAGIC_BYTES)
        } catch (e: Exception) {
            false
        }
    }

    override fun detectVersion(path: String): BoxResult<Pair<Int, Int>> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "File not found: $path")
            }

            val raf = RandomAccessFile(file, "r")
            val parser = BoxParser(raf)
            val headerResult = parser.readHeader()
            raf.close()

            if (headerResult.isFailure()) {
                return BoxResult.failure(headerResult.error())
            }

            val header = headerResult.value()
            BoxResult.success(Pair(header.formatVersionMajor, header.formatVersionMinor))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to detect version: ${e.message}", e)
        }
    }

    override fun close() {
        // No persistent state to clean up
    }
}

/**
 * BOX Container implementation for reading.
 */
internal class BoxContainerImpl(
    override val path: String,
    private val raf: RandomAccessFile,
    private val parser: BoxParser,
    private val data: BoxContainerData,
    private val readOnly: Boolean
) : BoxContainer {

    private var closed = false
    private var modified = false

    override val name: String
        get() = File(path).nameWithoutExtension

    override val isOpen: Boolean get() = !closed

    override val isModified: Boolean get() = modified

    override val fileCount: Int get() = data.fileEntries.size

    override val folderCount: Int get() = data.folderEntries.size

    override val totalSize: Long
        get() = data.fileEntries.sumOf { it.uncompressedSize }

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
        for (folder in data.folderEntries) {
            val folderPath = data.getFolderPath(folder)
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
        for (file in data.fileEntries) {
            val filePath = data.getFilePath(file)
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

        for (folder in data.folderEntries) {
            val folderPath = data.getFolderPath(folder)
            result.add(BoxFolderEntry(
                id = folder.entryId,
                name = folder.name,
                path = folderPath,
                childCount = folder.childCount,
                descendantCount = folder.totalDescendantCount
            ))
        }

        for (file in data.fileEntries) {
            val filePath = data.getFilePath(file)
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

        return data.fileEntries.map { file ->
            BoxFileEntry(
                id = file.entryId,
                name = file.name,
                path = data.getFilePath(file),
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

        return data.folderEntries.map { folder ->
            BoxFolderEntry(
                id = folder.entryId,
                name = folder.name,
                path = data.getFolderPath(folder),
                childCount = folder.childCount,
                descendantCount = folder.totalDescendantCount
            )
        }
    }

    override fun rootChildren(): List<BoxEntry> {
        if (closed) return emptyList()

        val result = mutableListOf<BoxEntry>()

        val rootId = BoxConstants.ROOT_FOLDER_ID.toInt()

        for (folder in data.folderEntries) {
            if (folder.parentId == rootId && folder.entryId != rootId) {
                result.add(BoxFolderEntry(
                    id = folder.entryId,
                    name = folder.name,
                    path = data.getFolderPath(folder),
                    childCount = folder.childCount,
                    descendantCount = folder.totalDescendantCount
                ))
            }
        }

        for (file in data.fileEntries) {
            if (file.parentFolderEntryId == rootId) {
                result.add(BoxFileEntry(
                    id = file.entryId,
                    name = file.name,
                    path = data.getFilePath(file),
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
        if (readOnly) return BoxResult.failure(ErrorCode.READ_ONLY, "Container is read-only")
        // Not implemented in v0.1 read mode
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Add file not supported in read mode")
    }

    override fun addFile(path: String, sourcePath: String): BoxResult<BoxFileEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        if (readOnly) return BoxResult.failure(ErrorCode.READ_ONLY, "Container is read-only")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Add file not supported in read mode")
    }

    override fun remove(path: String): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        if (readOnly) return BoxResult.failure(ErrorCode.READ_ONLY, "Container is read-only")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Remove not supported in v0.1")
    }

    override fun rename(path: String, newName: String): BoxResult<BoxEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        if (readOnly) return BoxResult.failure(ErrorCode.READ_ONLY, "Container is read-only")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Rename not supported in v0.1")
    }

    override fun addFolder(path: String): BoxResult<BoxFolderEntry> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        if (readOnly) return BoxResult.failure(ErrorCode.READ_ONLY, "Container is read-only")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Add folder not supported in read mode")
    }

    override fun extract(path: String, destinationPath: String): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        val entry = find(path)
        if (entry.isFailure()) return BoxResult.failure(entry.error())

        val destFile = File(destinationPath)
        val entryValue = entry.value()

        return when (entryValue) {
            is BoxFileEntry -> {
                val internalFile = data.fileEntries.find { it.entryId == entryValue.id }
                    ?: return BoxResult.failure(ErrorCode.ENTRY_NOT_FOUND, "Internal entry not found: $path")
                val result = extractFile(internalFile, destFile)
                if (result.isFailure()) {
                    result
                } else {
                    BoxResult.success(Unit)
                }
            }
            is BoxFolderEntry -> {
                extractFolder(entryValue, destFile)
            }
        }
    }

    override fun extractAll(destinationPath: String): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")

        val destDir = File(destinationPath)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        for (folder in data.folderEntries) {
            if (folder.entryId == BoxConstants.ROOT_FOLDER_ID.toInt()) continue
            val folderPath = data.getFolderPath(folder)
            val destFolder = File(destDir, folderPath.removePrefix("/"))
            if (!destFolder.exists()) {
                destFolder.mkdirs()
            }
        }

        for (file in data.fileEntries) {
            val filePath = data.getFilePath(file)
            val destFile = File(destDir, filePath.removePrefix("/"))
            val result = extractFile(file, destFile)
            if (result.isFailure()) {
                return result
            }
        }

        return BoxResult.success(Unit)
    }

    private fun extractFile(fileEntry: FileEntry, destFile: File): BoxResult<Unit> {
        try {
            if (fileEntry.chunkCount == 0) {
                // Empty file
                destFile.parentFile?.mkdirs()
                destFile.createNewFile()
                return BoxResult.success(Unit)
            }

            destFile.parentFile?.mkdirs()
            destFile.createNewFile()

            val chunks = data.getChunksForFile(fileEntry)
            destFile.outputStream().use { output ->
                for (chunk in chunks) {
                    val chunkData = parser.readChunkData(chunk.chunkOffset, chunk.chunkStoredSize)
                    if (chunkData.isFailure()) {
                        return BoxResult.failure(chunkData.error())
                    }
                    output.write(chunkData.value())
                }
            }

            return BoxResult.success(Unit)
        } catch (e: Exception) {
            return BoxResult.failure(ErrorCode.IO_FAILED, "Failed to extract file: ${e.message}", e)
        }
    }

    private fun extractFolder(folderEntry: BoxFolderEntry, destDir: File): BoxResult<Unit> {
        try {
            destDir.mkdirs()

            for (folder in data.folderEntries) {
                if (folder.parentId == folderEntry.id) {
                    val subFolder = File(destDir, folder.name)
                    val result = extractFolder(BoxFolderEntry(
                        id = folder.entryId,
                        name = folder.name,
                        path = data.getFolderPath(folder),
                        childCount = folder.childCount,
                        descendantCount = folder.totalDescendantCount
                    ), subFolder)
                    if (result.isFailure()) return result
                }
            }

            for (file in data.fileEntries) {
                if (file.parentFolderEntryId == folderEntry.id) {
                    val destFile = File(destDir, file.name)
                    val result = extractFile(file, destFile)
                    if (result.isFailure()) return result
                }
            }

            return BoxResult.success(Unit)
        } catch (e: Exception) {
            return BoxResult.failure(ErrorCode.IO_FAILED, "Failed to extract folder: ${e.message}", e)
        }
    }

    override fun metadata(): Map<Int, String> {
        if (closed) return emptyMap()
        return data.metadata
    }

    override fun getMetadata(key: Int): String? {
        if (closed) return null
        return data.metadata[key]
    }

    override fun setMetadata(key: Int, value: String) {
        if (closed || readOnly) return
        // Not implemented in v0.1 read mode
    }

    override fun removeMetadata(key: Int) {
        if (closed || readOnly) return
        // Not implemented in v0.1 read mode
    }

    override fun save(): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        if (readOnly) return BoxResult.failure(ErrorCode.READ_ONLY, "Container is read-only")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "Save not supported in read mode")
    }

    override fun saveAs(path: String): BoxResult<Unit> {
        if (closed) return BoxResult.failure(ErrorCode.CONTAINER_CLOSED, "Container is closed")
        return BoxResult.failure(ErrorCode.NOT_IMPLEMENTED, "SaveAs not supported in v0.1")
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

        // Validate header checksum
        val checksumResult = parser.validateHeaderChecksum(data.header)
        if (checksumResult.isFailure()) {
            return BoxResult.failure(checksumResult.error())
        }

        return BoxResult.success(true)
    }

    private fun normalizePath(path: String): String {
        if (path == "/") return "/"
        return if (path.startsWith("/")) path else "/$path"
    }
}
