package dev.box.api

import dev.box.core.model.*

/**
 * BOX Container interface.
 * Represents a .box file that can be read or modified.
 */
interface BoxContainer : AutoCloseable {
    val path: String
    val name: String
    val isOpen: Boolean
    val isModified: Boolean

    // Properties
    val entryCount: Int get() = fileCount + folderCount
    val fileCount: Int
    val folderCount: Int
    val totalSize: Long
    val storageSize: Long

    // Navigation
    fun find(path: String): BoxResult<BoxEntry>
    fun findFile(path: String): BoxResult<BoxFileEntry>
    fun findFolder(path: String): BoxResult<BoxFolderEntry>
    fun exists(path: String): Boolean
    fun isFile(path: String): Boolean
    fun isFolder(path: String): Boolean

    // Listing
    fun entries(): List<BoxEntry>
    fun files(): List<BoxFileEntry>
    fun folders(): List<BoxFolderEntry>
    fun rootChildren(): List<BoxEntry>

    // File operations
    fun addFile(path: String, data: ByteArray): BoxResult<BoxFileEntry>
    fun addFile(path: String, sourcePath: String): BoxResult<BoxFileEntry>
    fun remove(path: String): BoxResult<Unit>
    fun rename(path: String, newName: String): BoxResult<BoxEntry>

    // Folder operations
    fun addFolder(path: String): BoxResult<BoxFolderEntry>

    // Extraction
    fun extract(path: String, destinationPath: String): BoxResult<Unit>
    fun extractAll(destinationPath: String): BoxResult<Unit>

    // Metadata
    fun metadata(): Map<Int, String>
    fun getMetadata(key: Int): String?
    fun setMetadata(key: Int, value: String)
    fun removeMetadata(key: Int)

    // Lifecycle
    fun save(): BoxResult<Unit>
    fun saveAs(path: String): BoxResult<Unit>
    override fun close()

    // Validation
    fun validate(): BoxResult<Boolean>
}
