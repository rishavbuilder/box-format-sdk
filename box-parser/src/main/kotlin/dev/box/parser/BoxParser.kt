package dev.box.parser

import dev.box.core.constants.BoxConstants
import dev.box.core.model.*
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.zip.CRC32

/**
 * Binary parser for reading BOX file structures.
 */
class BoxParser(private val file: RandomAccessFile) {

    /**
     * Read the BOX header from offset 0.
     */
    fun readHeader(): BoxResult<BoxHeader> {
        return try {
            file.seek(0)
            val buffer = ByteArray(BoxConstants.HEADER_SIZE)
            file.readFully(buffer)
            val buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)

            // Validate magic bytes
            val magic = ByteArray(4)
            buf.get(magic)
            if (!magic.contentEquals(BoxConstants.MAGIC_BYTES)) {
                return BoxResult.failure(ErrorCode.INVALID_MAGIC, "Invalid magic bytes: ${magic.joinToString("") { "%02X".format(it) }}")
            }

            val formatVersionMajor = buf.get().toInt() and 0xFF
            val formatVersionMinor = buf.get().toInt() and 0xFF
            val headerVersionMajor = buf.get().toInt() and 0xFF
            val headerVersionMinor = buf.get().toInt() and 0xFF

            // Validate header version
            if (formatVersionMajor > 1) {
                return BoxResult.failure(ErrorCode.UNSUPPORTED_VERSION, "Unsupported format version: $formatVersionMajor.$formatVersionMinor")
            }

            val containerId = buf.getLong()
            val creationTimestamp = buf.getLong()
            val modificationTimestamp = buf.getLong()
            val flags = buf.getInt()
            val entryCount = buf.getInt()
            val fileEntryCount = buf.getInt()
            val folderEntryCount = buf.getInt()
            val totalDataSize = buf.getLong()
            val compressionAlgorithm = buf.getInt()
            val encryptionAlgorithm = buf.getInt()
            val checksumAlgorithm = buf.getInt()
            val metadataOffset = buf.getInt()
            val metadataSize = buf.getInt()
            val folderTreeOffset = buf.getInt()
            val folderTreeSize = buf.getInt()
            val fileIndexOffset = buf.getInt()
            val fileIndexSize = buf.getInt()
            val chunkTableOffset = buf.getInt()
            val chunkTableSize = buf.getInt()
            val chunksOffset = buf.getInt()
            val chunksSize = buf.getLong()
            val extensionSlotsOffset = buf.getInt()
            val extensionSlotsSize = buf.getInt()
            val footerOffset = buf.getInt()
            val footerSize = buf.getInt()

            val headerChecksum = ByteArray(32)
            buf.get(headerChecksum)

            val reserved = ByteArray(88)
            buf.get(reserved)

            val headerEndMarker = ByteArray(8)
            buf.get(headerEndMarker)

            BoxResult.success(BoxHeader(
                formatVersionMajor = formatVersionMajor,
                formatVersionMinor = formatVersionMinor,
                headerVersionMajor = headerVersionMajor,
                headerVersionMinor = headerVersionMinor,
                containerId = containerId,
                creationTimestamp = creationTimestamp,
                modificationTimestamp = modificationTimestamp,
                flags = flags,
                entryCount = entryCount,
                fileEntryCount = fileEntryCount,
                folderEntryCount = folderEntryCount,
                totalDataSize = totalDataSize,
                compressionAlgorithm = compressionAlgorithm,
                encryptionAlgorithm = encryptionAlgorithm,
                checksumAlgorithm = checksumAlgorithm,
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
                extensionSlotsSize = extensionSlotsSize,
                footerOffset = footerOffset,
                footerSize = footerSize,
                headerChecksum = headerChecksum,
                headerEndMarker = headerEndMarker
            ))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read header: ${e.message}", e)
        }
    }

    /**
     * Validate header checksum.
     */
    fun validateHeaderChecksum(header: BoxHeader): BoxResult<Boolean> {
        return try {
            file.seek(0)
            val data = ByteArray(128) // First 128 bytes
            file.readFully(data)

            val digest = MessageDigest.getInstance("SHA-256")
            val computed = digest.digest(data)

            if (computed.contentEquals(header.headerChecksum)) {
                BoxResult.success(true)
            } else {
                BoxResult.failure(ErrorCode.INVALID_CHECKSUM, "Header checksum mismatch")
            }
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to validate header checksum: ${e.message}", e)
        }
    }

    /**
     * Read the footer from the specified offset.
     */
    fun readFooter(offset: Int): BoxResult<BoxFooter> {
        return try {
            file.seek(offset.toLong())
            val buffer = ByteArray(BoxConstants.FOOTER_SIZE)
            file.readFully(buffer)
            val buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)

            val headerChecksumOffset = buf.getLong()
            val containerSize = buf.getLong()
            val reserved = ByteArray(112)
            buf.get(reserved)

            BoxResult.success(BoxFooter(
                headerChecksumOffset = headerChecksumOffset,
                containerSize = containerSize,
                reserved = reserved
            ))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read footer: ${e.message}", e)
        }
    }

    /**
     * Read a section frame at the given offset.
     */
    fun readSectionFrame(offset: Int): BoxResult<SectionFrame> {
        return try {
            file.seek(offset.toLong())
            val buffer = ByteArray(BoxConstants.SECTION_FRAME_SIZE)
            file.readFully(buffer)
            val buf = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)

            val sectionType = ByteArray(4)
            buf.get(sectionType)
            val sectionSize = buf.getLong()
            val sectionCrc32 = buf.getInt()
            val reserved = buf.getInt()

            BoxResult.success(SectionFrame(
                sectionType = sectionType,
                sectionSize = sectionSize,
                sectionCrc32 = sectionCrc32,
                reserved = reserved
            ))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read section frame: ${e.message}", e)
        }
    }

    /**
     * Read metadata entries from the metadata section.
     */
    fun readMetadata(offset: Int, size: Int): BoxResult<Map<Int, String>> {
        return try {
            val dataStart = offset + BoxConstants.SECTION_FRAME_SIZE
            file.seek(dataStart.toLong())
            val data = ByteArray(size - BoxConstants.SECTION_FRAME_SIZE)
            file.readFully(data)
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            val entries = mutableMapOf<Int, String>()

            while (buf.remaining() >= 6) {
                val tagId = buf.getShort().toInt() and 0xFFFF
                val length = buf.getInt()
                if (length < 0 || length > buf.remaining()) break
                val valueBytes = ByteArray(length)
                buf.get(valueBytes)
                val value = String(valueBytes, Charsets.UTF_8)
                entries[tagId] = value
            }

            BoxResult.success(entries)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read metadata: ${e.message}", e)
        }
    }

    /**
     * Read folder entries from the folder tree section.
     */
    fun readFolderEntries(offset: Int, size: Int): BoxResult<List<FolderEntry>> {
        return try {
            val dataStart = offset + BoxConstants.SECTION_FRAME_SIZE
            file.seek(dataStart.toLong())
            val data = ByteArray(size - BoxConstants.SECTION_FRAME_SIZE)
            file.readFully(data)
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            val entries = mutableListOf<FolderEntry>()

            while (buf.remaining() >= BoxConstants.FOLDER_ENTRY_SIZE) {
                val entryId = buf.getInt()
                val parentId = buf.getInt()
                val childCount = buf.getInt()
                val totalDescendantCount = buf.getInt()
                val nameLength = buf.getInt()
                val flags = buf.getInt()
                val reserved = buf.getInt()
                val nameCrc32 = buf.getInt()

                if (nameLength < 0 || nameLength > buf.remaining()) break
                val nameBytes = ByteArray(nameLength)
                buf.get(nameBytes)
                val name = String(nameBytes, Charsets.UTF_8)

                entries.add(FolderEntry(
                    entryId = entryId,
                    parentId = parentId,
                    childCount = childCount,
                    totalDescendantCount = totalDescendantCount,
                    name = name,
                    flags = flags,
                    nameCrc32 = nameCrc32
                ))
            }

            BoxResult.success(entries)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read folder entries: ${e.message}", e)
        }
    }

    /**
     * Read file entries from the file index section.
     */
    fun readFileEntries(offset: Int, size: Int): BoxResult<List<FileEntry>> {
        return try {
            val dataStart = offset + BoxConstants.SECTION_FRAME_SIZE
            file.seek(dataStart.toLong())
            val data = ByteArray(size - BoxConstants.SECTION_FRAME_SIZE)
            file.readFully(data)
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            val entries = mutableListOf<FileEntry>()

            while (buf.remaining() >= BoxConstants.FILE_ENTRY_SIZE) {
                val entryId = buf.getInt()
                val parentFolderEntryId = buf.getInt()
                val nameLength = buf.getInt()
                val flags = buf.getInt()
                val uncompressedSize = buf.getLong()
                val compressedSize = buf.getLong()
                val chunkCount = buf.getInt()
                val firstChunkIndex = buf.getInt()
                val checksumAlgorithm = buf.getInt()
                val fileChecksum = ByteArray(32)
                buf.get(fileChecksum)
                val thumbnailOffset = buf.getInt()
                val thumbnailSize = buf.getInt()
                val mimeTypeLength = buf.getInt()
                val createdTimestamp = buf.getInt()
                val modifiedTimestamp = buf.getInt()
                val nameCrc32 = buf.getInt()
                val reserved = buf.getInt()

                // Read name
                if (nameLength < 0 || nameLength > buf.remaining()) break
                val nameBytes = ByteArray(nameLength)
                buf.get(nameBytes)
                val name = String(nameBytes, Charsets.UTF_8)

                // Read MIME type
                if (mimeTypeLength < 0 || mimeTypeLength > buf.remaining()) break
                val mimeTypeBytes = ByteArray(mimeTypeLength)
                buf.get(mimeTypeBytes)
                val mimeType = String(mimeTypeBytes, Charsets.UTF_8)

                entries.add(FileEntry(
                    entryId = entryId,
                    parentFolderEntryId = parentFolderEntryId,
                    name = name,
                    flags = flags,
                    uncompressedSize = uncompressedSize,
                    compressedSize = compressedSize,
                    chunkCount = chunkCount,
                    firstChunkIndex = firstChunkIndex,
                    checksumAlgorithm = checksumAlgorithm,
                    fileChecksum = fileChecksum,
                    thumbnailOffset = thumbnailOffset,
                    thumbnailSize = thumbnailSize,
                    mimeType = mimeType,
                    createdTimestamp = createdTimestamp,
                    modifiedTimestamp = modifiedTimestamp,
                    nameCrc32 = nameCrc32
                ))
            }

            BoxResult.success(entries)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read file entries: ${e.message}", e)
        }
    }

    /**
     * Read chunk table entries.
     */
    fun readChunkTableEntries(offset: Int, size: Int): BoxResult<List<ChunkTableEntry>> {
        return try {
            val dataStart = offset + BoxConstants.SECTION_FRAME_SIZE
            file.seek(dataStart.toLong())
            val data = ByteArray(size - BoxConstants.SECTION_FRAME_SIZE)
            file.readFully(data)
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            val entries = mutableListOf<ChunkTableEntry>()

            while (buf.remaining() >= BoxConstants.CHUNK_TABLE_ENTRY_SIZE) {
                val chunkId = buf.getLong()
                val entryId = buf.getInt()
                val chunkIndex = buf.getInt()
                val chunkOffset = buf.getLong()
                val chunkPayloadSize = buf.getLong()
                val chunkStoredSize = buf.getLong()
                val chunkFlags = buf.getInt()
                val checksumAlgorithm = buf.getInt()
                val chunkChecksum = ByteArray(32)
                buf.get(chunkChecksum)

                entries.add(ChunkTableEntry(
                    chunkId = chunkId,
                    entryId = entryId,
                    chunkIndex = chunkIndex,
                    chunkOffset = chunkOffset,
                    chunkPayloadSize = chunkPayloadSize,
                    chunkStoredSize = chunkStoredSize,
                    chunkFlags = chunkFlags,
                    checksumAlgorithm = checksumAlgorithm,
                    chunkChecksum = chunkChecksum
                ))
            }

            BoxResult.success(entries)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read chunk table: ${e.message}", e)
        }
    }

    /**
     * Read chunk data (payload) from the chunks region.
     */
    fun readChunkData(offset: Long, size: Long): BoxResult<ByteArray> {
        return try {
            file.seek(offset)
            val data = ByteArray(size.toInt())
            file.readFully(data)
            BoxResult.success(data)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read chunk data: ${e.message}", e)
        }
    }

    /**
     * Validate CRC-32 of a section.
     */
    fun validateSectionCrc32(offset: Int, sectionSize: Long, expectedCrc32: Int): BoxResult<Boolean> {
        return try {
            val dataStart = offset + BoxConstants.SECTION_FRAME_SIZE
            file.seek(dataStart.toLong())
            val data = ByteArray(sectionSize.toInt())
            file.readFully(data)

            val crc32 = CRC32()
            crc32.update(data)
            val computed = crc32.value.toInt()

            if (computed == expectedCrc32) {
                BoxResult.success(true)
            } else {
                BoxResult.failure(ErrorCode.INVALID_CHECKSUM, "Section CRC-32 mismatch")
            }
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to validate section CRC-32: ${e.message}", e)
        }
    }

    /**
     * Read a raw byte range from the file.
     */
    fun readBytes(offset: Long, size: Int): BoxResult<ByteArray> {
        return try {
            file.seek(offset)
            val data = ByteArray(size)
            file.readFully(data)
            BoxResult.success(data)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Failed to read bytes: ${e.message}", e)
        }
    }

    /**
     * Get file size.
     */
    fun fileSize(): Long = file.length()
}
