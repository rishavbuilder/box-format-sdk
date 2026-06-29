package dev.box.core.model

/**
 * Metadata TLV (Tag-Length-Value) entry.
 */
data class MetadataEntry(
    val tagId: Int,
    val value: String
) {
    val tagIdBytes: ByteArray
        get() {
            val buf = java.nio.ByteBuffer.allocate(2).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            buf.putInt(tagId)
            return buf.array().take(2).toByteArray()
        }

    val valueBytes: ByteArray
        get() = value.toByteArray(Charsets.UTF_8)

    val length: Int
        get() = valueBytes.size
}

/**
 * Metadata tag IDs.
 */
object MetadataTags {
    const val CONTAINER_NAME = 0x0001
    const val CREATION_TIME = 0x0002
    const val MODIFICATION_TIME = 0x0003
    const val AUTHOR = 0x0004
    const val DESCRIPTION = 0x0005
    const val TAGS = 0x0006
    const val COMMENTS = 0x0007
    const val LANGUAGE = 0x0008
    const val TOOL_NAME = 0x0009
    const val TOOL_VERSION = 0x000A
    const val CONTENT_TYPE = 0x000B

    fun nameForId(id: Int): String = when (id) {
        CONTAINER_NAME -> "CONTAINER_NAME"
        CREATION_TIME -> "CREATION_TIME"
        MODIFICATION_TIME -> "MODIFICATION_TIME"
        AUTHOR -> "AUTHOR"
        DESCRIPTION -> "DESCRIPTION"
        TAGS -> "TAGS"
        COMMENTS -> "COMMENTS"
        LANGUAGE -> "LANGUAGE"
        TOOL_NAME -> "TOOL_NAME"
        TOOL_VERSION -> "TOOL_VERSION"
        CONTENT_TYPE -> "CONTENT_TYPE"
        else -> "CUSTOM_$id"
    }
}
