package dev.box.api

import dev.box.core.model.*

/**
 * BOX Writer interface.
 * Creates new .box files.
 */
interface BoxWriter : AutoCloseable {
    fun create(path: String): BoxResult<BoxContainer>
    fun create(path: String, options: BoxOptions): BoxResult<BoxContainer>
    override fun close()
}

/**
 * Options for BOX creation.
 */
data class BoxOptions(
    val compressionEnabled: Boolean = false,
    val compressionLevel: Int = 3,
    val chunkSize: Long = dev.box.core.constants.BoxConstants.DEFAULT_CHUNK_SIZE
)
