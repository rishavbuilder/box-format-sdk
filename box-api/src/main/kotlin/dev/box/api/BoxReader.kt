package dev.box.api

import dev.box.core.model.*

/**
 * BOX Reader interface.
 * Opens and reads existing .box files.
 */
interface BoxReader : AutoCloseable {
    fun open(path: String): BoxResult<BoxContainer>
    fun openReadOnly(path: String): BoxResult<BoxContainer>
    fun isBoxFile(path: String): Boolean
    fun detectVersion(path: String): BoxResult<Pair<Int, Int>>
    override fun close()
}
