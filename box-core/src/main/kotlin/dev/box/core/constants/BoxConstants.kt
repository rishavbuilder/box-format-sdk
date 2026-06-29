package dev.box.core.constants

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * BOX File Format Constants v1.0
 * Official specification constants.
 */
object BoxConstants {

    // Magic bytes: "BXOX"
    val MAGIC_BYTES = byteArrayOf(0x42, 0x58, 0x4F, 0x58)

    // Header end marker: "BXOEND\0"
    val HEADER_END_MARKER = byteArrayOf(0x42, 0x58, 0x4F, 0x45, 0x4E, 0x44, 0x00, 0x00)

    // Section type identifiers (ASCII)
    val SECTION_TYPE_META = "META".toByteArray()
    val SECTION_TYPE_FTRE = "FTRE".toByteArray()
    val SECTION_TYPE_FIDX = "FIDX".toByteArray()
    val SECTION_TYPE_CTAB = "CTAB".toByteArray()
    val SECTION_TYPE_CHNK = "CHNK".toByteArray()

    // Fixed sizes
    const val HEADER_SIZE = 256
    const val FOOTER_SIZE = 128
    const val SECTION_FRAME_SIZE = 20
    const val FOLDER_ENTRY_SIZE = 32
    const val FILE_ENTRY_SIZE = 104
    const val CHUNK_TABLE_ENTRY_SIZE = 80
    const val CHUNK_HEADER_SIZE = 32
    const val EXTENSION_SLOTS_COUNT = 4
    const val EXTENSION_SLOT_SIZE = 256
    const val EXTENSION_SLOTS_TOTAL_SIZE = EXTENSION_SLOTS_COUNT * EXTENSION_SLOT_SIZE

    // Alignment
    const val SECTION_ALIGNMENT = 64
    const val CHUNK_ALIGNMENT = 4096

    // Maximums
    const val MAX_CHUNK_PAYLOAD_SIZE = 128 * 1024 * 1024L // 128 MB
    const val DEFAULT_CHUNK_SIZE = 4 * 1024 * 1024L // 4 MB
    const val MAX_THUMBNAIL_SIZE = 128 * 1024 // 128 KB
    const val MAX_THUMBNAIL_DIMENSION = 256

    // Version
    const val FORMAT_VERSION_MAJOR = 1
    const val FORMAT_VERSION_MINOR = 0
    const val HEADER_VERSION_MAJOR = 1
    const val HEADER_VERSION_MINOR = 0

    // Special Entry IDs
    const val ROOT_FOLDER_ID = 0x00000000u
    const val ROOT_PARENT_ID = 0xFFFFFFFFu

    // File extension
    const val FILE_EXTENSION = ".box"

    // MIME type
    const val MIME_TYPE = "application/box"

    // Checksum sizes
    const val CRC32_SIZE = 4
    const val SHA256_SIZE = 32

    // Checksum algorithm IDs
    const val CHECKSUM_NONE = 0
    const val CHECKSUM_CRC32 = 1
    const val CHECKSUM_XXHASH64 = 2
    const val CHECKSUM_SHA256 = 3
    const val CHECKSUM_BLAKE3 = 4

    // Encryption
    const val ENCRYPTION_IV_SIZE = 16
    const val ENCRYPTION_AUTH_TAG_SIZE = 16

    /**
     * Create a ByteBuffer with little-endian byte order.
     */
    fun littleEndianBuffer(size: Int): ByteBuffer {
        return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN)
    }

    /**
     * Align an offset to the given alignment boundary.
     */
    fun align(offset: Long, alignment: Int): Long {
        val remainder = offset % alignment
        return if (remainder == 0L) offset else offset + (alignment - remainder)
    }

    /**
     * Calculate padding needed to reach alignment.
     */
    fun padding(offset: Long, alignment: Int): Int {
        val remainder = (offset % alignment).toInt()
        return if (remainder == 0) 0 else alignment - remainder
    }
}
