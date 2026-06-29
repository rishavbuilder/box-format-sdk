package dev.box.samples

import dev.box.core.model.MetadataTags
import dev.box.core.model.BoxFolderEntry
import dev.box.core.model.BoxFileEntry
import dev.box.reader.BoxReaderImpl
import dev.box.validator.BoxValidatorImpl
import dev.box.writer.BoxWriterImpl
import java.io.File

/**
 * BOX SDK Demo - Creates a sample .box file and reads it back.
 */
fun main() {
    println("=== BOX Format SDK Demo ===\n")

    val demoBoxPath = "Demo.box"
    val extractPath = "Demo_extracted"

    // Step 1: Create a new .box archive
    println("1. Creating $demoBoxPath...")

    val writer = BoxWriterImpl()
    val createResult = writer.create(demoBoxPath)
    if (createResult.isFailure()) {
        println("   ERROR: ${createResult.error().message}")
        return
    }

    val container = createResult.value()

    // Set metadata
    container.setMetadata(MetadataTags.AUTHOR, "BOX SDK Demo")
    container.setMetadata(MetadataTags.DESCRIPTION, "Sample archive created by Demo.kt")
    container.setMetadata(MetadataTags.TOOL_NAME, "BOX SDK")
    container.setMetadata(MetadataTags.TOOL_VERSION, "0.1.0")

    // Create folder structure
    println("   Adding folder structure...")
    container.addFolder("/Documents")
    container.addFolder("/Documents/Reports")
    container.addFolder("/Images")
    container.addFolder("/Source")

    // Add files
    println("   Adding files...")

    container.addFile(
        "/Documents/readme.txt",
        "Welcome to the BOX Format SDK!\n\nThis is a sample archive created by Demo.kt\n".toByteArray()
    )

    container.addFile(
        "/Documents/Reports/analysis.txt",
        "Sales Analysis Report\n====================\n\nQ1: $10,000\nQ2: $15,000\nQ3: $12,000\nQ4: $18,000\n".toByteArray()
    )

    container.addFile(
        "/Documents/notes.txt",
        "Important Notes:\n- BOX format supports nested folders\n- Files are chunked for efficient access\n- Metadata is stored as key-value pairs\n".toByteArray()
    )

    container.addFile(
        "/Source/hello.kt",
        """fun main() {
    println("Hello from BOX!")
}
""".toByteArray()
    )

    container.addFile(
        "/Source/config.json",
        """{
  "version": "1.0",
  "debug": false,
  "maxRetries": 3
}
""".toByteArray()
    )

    // Create a small binary file
    val binaryData = ByteArray(512) { (it % 256).toByte() }
    container.addFile("/Images/icon.bin", binaryData)

    // Save the archive
    println("   Saving archive...")
    val saveResult = container.save()
    if (saveResult.isFailure()) {
        println("   ERROR: ${saveResult.error().message}")
        return
    }

    container.close()
    println("   ✓ Created $demoBoxPath\n")

    // Step 2: Read and display archive contents
    println("2. Opening $demoBoxPath...")

    val reader = BoxReaderImpl()
    val openResult = reader.open(demoBoxPath)
    if (openResult.isFailure()) {
        println("   ERROR: ${openResult.error().message}")
        return
    }

    val readContainer = openResult.value()

    // Display archive info
    println("   Archive Info:")
    println("   - Files: ${readContainer.fileCount}")
    println("   - Folders: ${readContainer.folderCount}")
    println()

    // Display metadata
    println("   Metadata:")
    val metadata = readContainer.metadata()
    for ((key, value) in metadata) {
        println("   - $key: $value")
    }
    println()

    // Display file tree
    println("   Contents:")
    val entries = readContainer.entries()
    for (entry in entries) {
        val prefix = if (entry is BoxFolderEntry) "\uD83D\uDCC1" else "\uD83D\uDCC4"
        val size = if (entry is BoxFileEntry) " (${entry.size} bytes)" else ""
        println("   $prefix ${entry.path}$size")
    }
    println()

    // Extract all files
    println("3. Extracting to $extractPath...")
    val extractResult = readContainer.extractAll(extractPath)
    if (extractResult.isFailure()) {
        println("   ERROR: ${extractResult.error().message}")
        return
    }
    println("   ✓ Extracted successfully\n")

    // Verify extracted files
    println("4. Verifying extracted files...")
    val extractedDir = File(extractPath)
    if (extractedDir.exists()) {
        extractedDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relativePath = file.relativeTo(extractedDir).path
                val size = file.length()
                println("   ✓ $relativePath ($size bytes)")
            }
        }
    }
    println()

    // Validate the archive
    println("5. Validating archive...")
    val validator = BoxValidatorImpl()
    val validationResult = validator.validate(demoBoxPath)
    if (validationResult.isSuccess()) {
        val result = validationResult.value()
        if (result.isValid) {
            println("   ✓ Archive is valid")
        } else {
            println("   ✗ Archive has ${result.errorCount} errors")
        }
        if (result.warningCount > 0) {
            println("   ⚠ ${result.warningCount} warnings")
        }
    }
    println()

    // Cleanup
    readContainer.close()

    println("=== Demo Complete ===")
    println("Generated files:")
    println("  - $demoBoxPath (${File(demoBoxPath).length()} bytes)")
    println("  - $extractPath/ (extracted files)")
}
