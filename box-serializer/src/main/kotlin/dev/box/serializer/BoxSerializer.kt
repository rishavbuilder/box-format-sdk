package dev.box.serializer

import dev.box.core.constants.BoxConstants
import dev.box.core.model.*
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Binary serializer for writing BOX file structures.
 */
class BoxSerializer(private val file: RandomAccessFile) {

    /**
     * Write the BOX header.
     */
    fun writeHeader(header: BoxHeader) {
        file.seek(0)
        val buffer = ByteBuffer.allocate(BoxConstants.HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)

        // Magic bytes
        buffer.put(BoxConstants.MAGIC_BYTES)

        // Version
        buffer.put(header.formatVersionMajor.toByte())
        buffer.put(header.formatVersionMinor.toByte())
        buffer.put(header.headerVersionMajor.toByte())
        buffer.put(header.headerVersionMinor.toByte())

        // Container ID
        buffer.putLong(header.containerId)

        // Timestamps
        buffer.putLong(header.creationTimestamp)
        buffer.putLong(header.modificationTimestamp)

        // Flags and counts
        buffer.putInt(header.flags)
        buffer.putInt(header.entryCount)
        buffer.putInt(header.fileEntryCount)
        buffer.putInt(header.folderEntryCount)

        // Sizes
        buffer.putLong(header.totalDataSize)
        buffer.putInt(header.compressionAlgorithm)
        buffer.putInt(header.encryptionAlgorithm)
        buffer.putInt(header.checksumAlgorithm)

        // Section offsets and sizes
        buffer.putInt(header.metadataOffset)
        buffer.putInt(header.metadataSize)
        buffer.putInt(header.folderTreeOffset)
        buffer.putInt(header.folderTreeSize)
        buffer.putInt(header.fileIndexOffset)
        buffer.putInt(header.fileIndexSize)
        buffer.putInt(header.chunkTableOffset)
        buffer.putInt(header.chunkTableSize)
        buffer.putInt(header.chunksOffset)
        buffer.putLong(header.chunksSize)
        buffer.putInt(header.extensionSlotsOffset)
        buffer.putInt(header.extensionSlotsSize)
        buffer.putInt(header.footerOffset)
        buffer.putInt(header.footerSize)

        // Header checksum (placeholder - computed after writing)
        buffer.put(header.headerChecksum)

        // Reserved
        buffer.put(ByteArray(88))

        // Header end marker
        buffer.put(BoxConstants.HEADER_END_MARKER)

        file.write(buffer.array())
    }

    /**
     * Update the header checksum.
     */
    fun updateHeaderChecksum() {
        file.seek(0)
        val data = ByteArray(128)
        file.readFully(data)

        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val checksum = digest.digest(data)

        // Write checksum at offset 0x80
        file.seek(0x80)
        file.write(checksum)
    }

    /**
     * Update header offsets.
     */
    fun updateHeaderOffsets(header: BoxHeader) {
        file.seek(0)
        val buffer = ByteBuffer.allocate(0x80).order(ByteOrder.LITTLE_ENDIAN)

        // Magic and version
        buffer.put(BoxConstants.MAGIC_BYTES)
        buffer.put(header.formatVersionMajor.toByte())
        buffer.put(header.formatVersionMinor.toByte())
        buffer.put(header.headerVersionMajor.toByte())
        buffer.put(header.headerVersionMinor.toByte())

        // Container ID
        buffer.putLong(header.containerId)

        // Timestamps
        buffer.putLong(header.creationTimestamp)
        buffer.putLong(header.modificationTimestamp)

        // Flags and counts
        buffer.putInt(header.flags)
        buffer.putInt(header.entryCount)
        buffer.putInt(header.fileEntryCount)
        buffer.putInt(header.folderEntryCount)

        // Sizes
        buffer.putLong(header.totalDataSize)
        buffer.putInt(header.compressionAlgorithm)
        buffer.putInt(header.encryptionAlgorithm)
        buffer.putInt(header.checksumAlgorithm)

        // Section offsets and sizes
        buffer.putInt(header.metadataOffset)
        buffer.putInt(header.metadataSize)
        buffer.putInt(header.folderTreeOffset)
        buffer.putInt(header.folderTreeSize)
        buffer.putInt(header.fileIndexOffset)
        buffer.putInt(header.fileIndexSize)
        buffer.putInt(header.chunkTableOffset)
        buffer.putInt(header.chunkTableSize)
        buffer.putInt(header.chunksOffset)
        buffer.putLong(header.chunksSize)
        buffer.putInt(header.extensionSlotsOffset)
        buffer.putInt(header.extensionSlotsSize)
        buffer.putInt(header.footerOffset)
        buffer.putInt(header.footerSize)

        file.write(buffer.array())
    }

    /**
     * Write a section frame.
     */
    fun writeSectionFrame(frame: SectionFrame) {
        val buffer = ByteBuffer.allocate(BoxConstants.SECTION_FRAME_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(frame.sectionType)
        buffer.putLong(frame.sectionSize)
        buffer.putInt(frame.sectionCrc32)
        buffer.putInt(frame.reserved)
        file.write(buffer.array())
    }

    /**
     * Write metadata TLV entries.
     */
    fun writeMetadata(entries: Map<Int, String>): Int {
        val buffer = ByteBuffer.allocate(entries.values.sumOf { it.toByteArray(Charsets.UTF_8).size + 6 * entries.size }).order(ByteOrder.LITTLE_ENDIAN)

        for ((tagId, value) in entries) {
            val valueBytes = value.toByteArray(Charsets.UTF_8)
            buffer.putShort((tagId and 0xFFFF).toShort())
            buffer.putInt(valueBytes.size)
            buffer.put(valueBytes)
        }

        val data = ByteArray(buffer.position())
        buffer.flip()
        buffer.get(data)

        // Calculate CRC-32
        val crc32 = CRC32()
        crc32.update(data)

        // Write section frame
        writeSectionFrame(SectionFrame(
            sectionType = BoxConstants.SECTION_TYPE_META,
            sectionSize = data.size.toLong(),
            sectionCrc32 = crc32.value.toInt()
        ))

        // Write data
        file.write(data)

        // Return total bytes written (frame + data)
        return BoxConstants.SECTION_FRAME_SIZE + data.size
    }

    /**
     * Write folder entries.
     */
    fun writeFolderEntries(entries: List<FolderEntry>): Int {
        var totalSize = 0
        for (entry in entries) {
            totalSize += BoxConstants.FOLDER_ENTRY_SIZE + entry.name.toByteArray(Charsets.UTF_8).size
        }
        val dataBuffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        for (entry in entries) {
            dataBuffer.putInt(entry.entryId)
            dataBuffer.putInt(entry.parentId)
            dataBuffer.putInt(entry.childCount)
            dataBuffer.putInt(entry.totalDescendantCount)

            val nameBytes = entry.name.toByteArray(Charsets.UTF_8)
            dataBuffer.putInt(nameBytes.size)
            dataBuffer.putInt(entry.flags)
            dataBuffer.putInt(0) // reserved
            dataBuffer.putInt(entry.nameCrc32)
            dataBuffer.put(nameBytes)
        }

        val data = ByteArray(dataBuffer.position())
        dataBuffer.flip()
        dataBuffer.get(data)

        // Calculate CRC-32
        val crc32 = CRC32()
        crc32.update(data)

        // Write section frame
        writeSectionFrame(SectionFrame(
            sectionType = BoxConstants.SECTION_TYPE_FTRE,
            sectionSize = data.size.toLong(),
            sectionCrc32 = crc32.value.toInt()
        ))

        // Write data
        file.write(data)

        return BoxConstants.SECTION_FRAME_SIZE + data.size
    }

    /**
     * Write file entries.
     */
    fun writeFileEntries(entries: List<FileEntry>): Int {
        // Calculate total size needed
        var totalSize = entries.size * BoxConstants.FILE_ENTRY_SIZE
        for (entry in entries) {
            totalSize += entry.name.toByteArray(Charsets.UTF_8).size
            totalSize += entry.mimeType.toByteArray(Charsets.UTF_8).size
        }

        val dataBuffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)

        for (entry in entries) {
            dataBuffer.putInt(entry.entryId)
            dataBuffer.putInt(entry.parentFolderEntryId)

            val nameBytes = entry.name.toByteArray(Charsets.UTF_8)
            dataBuffer.putInt(nameBytes.size)
            dataBuffer.putInt(entry.flags)
            dataBuffer.putLong(entry.uncompressedSize)
            dataBuffer.putLong(entry.compressedSize)
            dataBuffer.putInt(entry.chunkCount)
            dataBuffer.putInt(entry.firstChunkIndex)
            dataBuffer.putInt(entry.checksumAlgorithm)
            dataBuffer.put(entry.fileChecksum)
            dataBuffer.putInt(entry.thumbnailOffset)
            dataBuffer.putInt(entry.thumbnailSize)

            val mimeTypeBytes = entry.mimeType.toByteArray(Charsets.UTF_8)
            dataBuffer.putInt(mimeTypeBytes.size)
            dataBuffer.putInt(entry.createdTimestamp)
            dataBuffer.putInt(entry.modifiedTimestamp)
            dataBuffer.putInt(entry.nameCrc32)
            dataBuffer.putInt(0) // reserved

            dataBuffer.put(nameBytes)
            dataBuffer.put(mimeTypeBytes)
        }

        val data = ByteArray(dataBuffer.position())
        dataBuffer.flip()
        dataBuffer.get(data)

        // Calculate CRC-32
        val crc32 = CRC32()
        crc32.update(data)

        // Write section frame
        writeSectionFrame(SectionFrame(
            sectionType = BoxConstants.SECTION_TYPE_FIDX,
            sectionSize = data.size.toLong(),
            sectionCrc32 = crc32.value.toInt()
        ))

        // Write data
        file.write(data)

        return BoxConstants.SECTION_FRAME_SIZE + data.size
    }

    /**
     * Write chunk table entries.
     */
    fun writeChunkTableEntries(entries: List<ChunkTableEntry>): Int {
        val dataBuffer = ByteBuffer.allocate(entries.size * BoxConstants.CHUNK_TABLE_ENTRY_SIZE).order(ByteOrder.LITTLE_ENDIAN)

        for (entry in entries) {
            dataBuffer.putLong(entry.chunkId)
            dataBuffer.putInt(entry.entryId)
            dataBuffer.putInt(entry.chunkIndex)
            dataBuffer.putLong(entry.chunkOffset)
            dataBuffer.putLong(entry.chunkPayloadSize)
            dataBuffer.putLong(entry.chunkStoredSize)
            dataBuffer.putInt(entry.chunkFlags)
            dataBuffer.putInt(entry.checksumAlgorithm)
            dataBuffer.put(entry.chunkChecksum)
        }

        val data = ByteArray(dataBuffer.position())
        dataBuffer.flip()
        dataBuffer.get(data)

        // Calculate CRC-32
        val crc32 = CRC32()
        crc32.update(data)

        // Write section frame
        writeSectionFrame(SectionFrame(
            sectionType = BoxConstants.SECTION_TYPE_CTAB,
            sectionSize = data.size.toLong(),
            sectionCrc32 = crc32.value.toInt()
        ))

        // Write data
        file.write(data)

        return BoxConstants.SECTION_FRAME_SIZE + data.size
    }

    /**
     * Write a chunk header and payload.
     */
    fun writeChunk(chunkId: Long, entryId: Int, chunkIndex: Int, payload: ByteArray): Long {
        val startPosition = file.filePointer

        // Write chunk header
        val headerBuffer = ByteBuffer.allocate(BoxConstants.CHUNK_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        headerBuffer.put(BoxConstants.SECTION_TYPE_CHNK)
        headerBuffer.putLong(chunkId)
        headerBuffer.putInt(entryId)
        headerBuffer.putInt(chunkIndex)
        headerBuffer.putInt(0) // flags
        headerBuffer.putInt(payload.size)
        headerBuffer.putInt(0) // reserved
        file.write(headerBuffer.array())

        // Write payload
        file.write(payload)

        // Calculate padding to align to CHUNK_ALIGNMENT
        val currentPosition = file.filePointer
        val padding = BoxConstants.padding(currentPosition, BoxConstants.CHUNK_ALIGNMENT)
        if (padding > 0) {
            file.write(ByteArray(padding))
        }

        return startPosition
    }

    /**
     * Write the footer.
     */
    fun writeFooter(footer: BoxFooter) {
        val buffer = ByteBuffer.allocate(BoxConstants.FOOTER_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(footer.headerChecksumOffset)
        buffer.putLong(footer.containerSize)
        buffer.put(footer.reserved)
        file.write(buffer.array())
    }

    /**
     * Write extension slots (zeros).
     */
    fun writeExtensionSlots() {
        file.write(ByteArray(BoxConstants.EXTENSION_SLOTS_TOTAL_SIZE))
    }

    /**
     * Calculate CRC-32 of data.
     */
    fun calculateCrc32(data: ByteArray): Int {
        val crc32 = CRC32()
        crc32.update(data)
        return crc32.value.toInt()
    }
}
