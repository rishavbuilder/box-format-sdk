package dev.box.tests

import dev.box.api.BoxContainer
import dev.box.core.constants.BoxConstants
import dev.box.core.model.*
import dev.box.reader.BoxReaderImpl
import dev.box.validator.BoxValidatorImpl
import dev.box.writer.BoxWriterImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class BoxTests {

    @TempDir
    lateinit var tempDir: File

    private lateinit var writer: BoxWriterImpl
    private lateinit var reader: BoxReaderImpl
    private lateinit var validator: BoxValidatorImpl

    @BeforeEach
    fun setup() {
        writer = BoxWriterImpl()
        reader = BoxReaderImpl()
        validator = BoxValidatorImpl()
    }

    @Test
    fun `test create and read empty BOX`() {
        val boxPath = File(tempDir, "empty.box").absolutePath

        // Create
        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess(), "Create should succeed")
        val container = createResult.value()
        container.save()
        container.close()

        // Verify file exists
        assertTrue(File(boxPath).exists(), "BOX file should exist")

        // Verify magic bytes
        val bytes = File(boxPath).readBytes()
        assertTrue(bytes.sliceArray(0..3).contentEquals(BoxConstants.MAGIC_BYTES), "Magic bytes should be BXOX")

        // Read
        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess(), "Open should succeed")
        val readContainer = openResult.value()

        assertEquals(0, readContainer.fileCount, "Should have 0 files")
        assertEquals(1, readContainer.folderCount, "Should have 1 folder (root only)")

        readContainer.close()
    }

    @Test
    fun `test create BOX with files`() {
        val boxPath = File(tempDir, "files.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        // Add files
        val file1Result = container.addFile("/hello.txt", "Hello, BOX!".toByteArray())
        assertTrue(file1Result.isSuccess(), "Add file 1 should succeed")

        val file2Result = container.addFile("/data.json", """{"key": "value"}""".toByteArray())
        assertTrue(file2Result.isSuccess(), "Add file 2 should succeed")

        // Save
        val saveResult = container.save()
        assertTrue(saveResult.isSuccess(), "Save should succeed")

        container.close()

        // Read and verify
        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        assertEquals(2, readContainer.fileCount, "Should have 2 files")

        val files = readContainer.files()
        assertEquals(2, files.size)

        val fileNames = files.map { it.name }.toSet()
        assertTrue(fileNames.contains("hello.txt"), "Should contain hello.txt")
        assertTrue(fileNames.contains("data.json"), "Should contain data.json")

        readContainer.close()
    }

    @Test
    fun `test create BOX with folders`() {
        val boxPath = File(tempDir, "folders.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        // Add folders
        val folder1Result = container.addFolder("/Documents")
        assertTrue(folder1Result.isSuccess(), "Add folder 1 should succeed")

        val folder2Result = container.addFolder("/Documents/Projects")
        assertTrue(folder2Result.isSuccess(), "Add folder 2 should succeed")

        val folder3Result = container.addFolder("/Images")
        assertTrue(folder3Result.isSuccess(), "Add folder 3 should succeed")

        // Add files to folders
        container.addFile("/Documents/readme.md", "# README".toByteArray())
        container.addFile("/Documents/Projects/app.kt", "fun main() {}".toByteArray())
        container.addFile("/Images/photo.jpg", ByteArray(100))

        // Save
        container.save()
        container.close()

        // Read and verify
        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        assertEquals(3, readContainer.fileCount, "Should have 3 files")

        val folders = readContainer.folders()
        assertTrue(folders.isNotEmpty(), "Should have folders")

        // Verify folder structure
        val folderPaths = folders.map { it.path }.toSet()
        assertTrue(folderPaths.contains("/Documents"), "Should contain /Documents")
        assertTrue(folderPaths.contains("/Documents/Projects"), "Should contain /Documents/Projects")
        assertTrue(folderPaths.contains("/Images"), "Should contain /Images")

        readContainer.close()
    }

    @Test
    fun `test extract files`() {
        val boxPath = File(tempDir, "extract.box").absolutePath
        val extractDir = File(tempDir, "extracted")

        // Create BOX
        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        container.addFolder("/docs")
        container.addFile("/docs/readme.txt", "This is a readme".toByteArray())
        container.addFile("/data.bin", ByteArray(256) { it.toByte() })

        container.save()
        container.close()

        // Open and extract
        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        val extractResult = readContainer.extractAll(extractDir.absolutePath)
        assertTrue(extractResult.isSuccess(), "Extract should succeed")

        // Verify extracted files
        val readmeFile = File(extractDir, "docs/readme.txt")
        assertTrue(readmeFile.exists(), "readme.txt should exist")
        assertEquals("This is a readme", readmeFile.readText())

        val dataFile = File(extractDir, "data.bin")
        assertTrue(dataFile.exists(), "data.bin should exist")
        assertEquals(256, dataFile.length().toInt())

        // Verify content matches
        val originalData = ByteArray(256) { it.toByte() }
        assertArrayEquals(originalData, dataFile.readBytes())

        readContainer.close()
    }

    @Test
    fun `test metadata`() {
        val boxPath = File(tempDir, "metadata.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        // Set metadata
        container.setMetadata(MetadataTags.AUTHOR, "John Doe")
        container.setMetadata(MetadataTags.DESCRIPTION, "Test archive")
        container.setMetadata(MetadataTags.TOOL_NAME, "BOX SDK")

        container.save()
        container.close()

        // Read and verify
        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        val metadata = readContainer.metadata()
        assertEquals("John Doe", metadata[MetadataTags.AUTHOR])
        assertEquals("Test archive", metadata[MetadataTags.DESCRIPTION])
        assertEquals("BOX SDK", metadata[MetadataTags.TOOL_NAME])

        readContainer.close()
    }

    @Test
    fun `test validation`() {
        val boxPath = File(tempDir, "validate.box").absolutePath

        // Create valid BOX
        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()
        container.addFile("/test.txt", "test".toByteArray())
        container.save()
        container.close()

        // Validate
        val validationResult = validator.validate(boxPath)
        assertTrue(validationResult.isSuccess(), "Validation should succeed")
        assertTrue(validationResult.value().isValid, "BOX should be valid")
        assertEquals(0, validationResult.value().errorCount)
    }

    @Test
    fun `test invalid magic bytes`() {
        val boxPath = File(tempDir, "invalid.box").absolutePath

        // Create file with invalid magic bytes
        val file = File(boxPath)
        file.writeBytes(ByteArray(256))

        val openResult = reader.open(boxPath)
        assertTrue(openResult.isFailure(), "Open should fail for invalid magic")
        assertEquals(ErrorCode.INVALID_MAGIC, openResult.error().code)
    }

    @Test
    fun `test missing file`() {
        val openResult = reader.open("/nonexistent/file.box")
        assertTrue(openResult.isFailure(), "Open should fail for missing file")
        assertEquals(ErrorCode.FILE_NOT_FOUND, openResult.error().code)
    }

    @Test
    fun `test isBoxFile detection`() {
        val validBox = File(tempDir, "valid.box").absolutePath
        val invalidFile = File(tempDir, "invalid.txt").absolutePath

        // Create valid BOX
        val createResult = writer.create(validBox)
        assertTrue(createResult.isSuccess())
        createResult.value().save()
        createResult.value().close()

        // Create invalid file
        File(invalidFile).writeText("Not a BOX file")

        assertTrue(reader.isBoxFile(validBox), "Should detect valid BOX")
        assertFalse(reader.isBoxFile(invalidFile), "Should not detect invalid file")
        assertFalse(reader.isBoxFile("/nonexistent.box"), "Should not detect nonexistent file")
    }

    @Test
    fun `test version detection`() {
        val boxPath = File(tempDir, "version.box").absolutePath

        // Create BOX
        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        createResult.value().save()
        createResult.value().close()

        // Detect version
        val versionResult = reader.detectVersion(boxPath)
        assertTrue(versionResult.isSuccess())
        assertEquals(1, versionResult.value().first, "Major version should be 1")
        assertEquals(0, versionResult.value().second, "Minor version should be 0")
    }

    @Test
    fun `test large file`() {
        val boxPath = File(tempDir, "large.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        // Create 1MB file
        val largeData = ByteArray(1024 * 1024) { (it % 256).toByte() }
        container.addFile("/large.bin", largeData)

        container.save()
        container.close()

        // Read and verify
        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        val file = readContainer.findFile("/large.bin")
        assertTrue(file.isSuccess())
        assertEquals(1024 * 1024L, file.value().size)

        // Extract and verify
        val extractDir = File(tempDir, "large_extracted")
        readContainer.extractAll(extractDir.absolutePath)

        val extractedFile = File(extractDir, "large.bin")
        assertTrue(extractedFile.exists())
        assertArrayEquals(largeData, extractedFile.readBytes())

        readContainer.close()
    }

    @Test
    fun `test find entries`() {
        val boxPath = File(tempDir, "find.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        container.addFolder("/docs")
        container.addFile("/docs/readme.txt", "readme".toByteArray())
        container.addFile("/data.json", "{}".toByteArray())

        container.save()
        container.close()

        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        // Find file
        val file = readContainer.findFile("/docs/readme.txt")
        assertTrue(file.isSuccess())
        assertEquals("readme.txt", file.value().name)

        // Find folder
        val folder = readContainer.findFolder("/docs")
        assertTrue(folder.isSuccess())
        assertEquals("docs", folder.value().name)

        // Find root file
        val rootFile = readContainer.findFile("/data.json")
        assertTrue(rootFile.isSuccess())
        assertEquals("data.json", rootFile.value().name)

        // Non-existent
        val missing = readContainer.find("/missing.txt")
        assertTrue(missing.isFailure())

        readContainer.close()
    }

    @Test
    fun `test empty container`() {
        val boxPath = File(tempDir, "empty.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        container.save()
        container.close()

        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        assertEquals(0, readContainer.fileCount)
        assertEquals(1, readContainer.entries().size) // root folder only

        readContainer.close()
    }

    @Test
    fun `test entry count`() {
        val boxPath = File(tempDir, "count.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        container.addFile("/a.txt", "a".toByteArray())
        container.addFile("/b.txt", "b".toByteArray())
        container.addFolder("/docs")
        container.addFile("/docs/c.txt", "c".toByteArray())

        container.save()
        container.close()

        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        assertEquals(3, readContainer.fileCount)
        assertTrue(readContainer.folderCount >= 1) // At least /docs

        readContainer.close()
    }

    @Test
    fun `test root children`() {
        val boxPath = File(tempDir, "root.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()

        container.addFile("/file1.txt", "1".toByteArray())
        container.addFile("/file2.txt", "2".toByteArray())
        container.addFolder("/folder1")

        container.save()
        container.close()

        val openResult = reader.open(boxPath)
        assertTrue(openResult.isSuccess())
        val readContainer = openResult.value()

        val rootChildren = readContainer.rootChildren()
        assertTrue(rootChildren.isNotEmpty(), "Root should have children")

        val names = rootChildren.map { it.name }.toSet()
        assertTrue(names.contains("file1.txt"))
        assertTrue(names.contains("file2.txt"))
        assertTrue(names.contains("folder1"))

        readContainer.close()
    }

    @Test
    fun `test binary format structure`() {
        val boxPath = File(tempDir, "structure.box").absolutePath

        val createResult = writer.create(boxPath)
        assertTrue(createResult.isSuccess())
        val container = createResult.value()
        container.addFile("/test.txt", "test".toByteArray())
        container.save()
        container.close()

        val bytes = File(boxPath).readBytes()

        // Verify magic bytes
        assertEquals(0x42.toByte(), bytes[0]) // B
        assertEquals(0x58.toByte(), bytes[1]) // X
        assertEquals(0x4F.toByte(), bytes[2]) // O
        assertEquals(0x58.toByte(), bytes[3]) // X

        // Verify version
        assertEquals(0x01.toByte(), bytes[4]) // Major version
        assertEquals(0x00.toByte(), bytes[5]) // Minor version

        // Verify header end marker at offset 0xF8
        val endMarker = bytes.sliceArray(0xF8..0xFF)
        assertEquals(0x42.toByte(), endMarker[0]) // B
        assertEquals(0x58.toByte(), endMarker[1]) // X
        assertEquals(0x4F.toByte(), endMarker[2]) // O
        assertEquals(0x45.toByte(), endMarker[3]) // E
        assertEquals(0x4E.toByte(), endMarker[4]) // N
        assertEquals(0x44.toByte(), endMarker[5]) // D
        assertEquals(0x00.toByte(), endMarker[6]) // \0
        assertEquals(0x00.toByte(), endMarker[7]) // \0
    }
}
