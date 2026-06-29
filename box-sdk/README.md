# BOX Format SDK v0.1

Official reference implementation of the [BOX File Format](../BOX-FORMAT-SPEC-v1.0.md) specification in Kotlin.

## Overview

BOX is a binary file format designed for efficient storage and retrieval of files with support for:
- Nested folder structures
- Chunked file storage for parallel access
- TLV metadata with extensible tags
- CRC-32 checksums for data integrity

This SDK provides a pure JVM implementation with no external dependencies.

## Project Structure

```
box-sdk/
├── box-core/          # Data models, constants, utilities
├── box-api/           # Public interfaces
├── box-parser/        # Binary format reader
├── box-serializer/    # Binary format writer
├── box-reader/        # High-level BOX file reader
├── box-writer/        # High-level BOX file writer
├── box-validator/     # BOX file validation
├── tests/             # JUnit 5 test suite
└── samples/           # Example applications
```

## Requirements

- Java 17 or later
- Gradle 8.5+ (wrapper included)

## Building

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Run the demo sample
./gradlew :samples:run
```

## Quick Start

### Creating a BOX Archive

```kotlin
import dev.box.api.MetadataTags
import dev.box.writer.BoxWriterImpl

fun main() {
    val writer = BoxWriterImpl()
    
    // Create a new archive
    val container = writer.create("archive.box").value()
    
    // Set metadata
    container.setMetadata(MetadataTags.AUTHOR, "Your Name")
    container.setMetadata(MetadataTags.DESCRIPTION, "My archive")
    
    // Add folders and files
    container.addFolder("/Documents")
    container.addFile("/Documents/readme.txt", "Hello BOX!".toByteArray())
    container.addFile("/data.json", """{"key": "value"}""".toByteArray())
    
    // Save and close
    container.save()
    container.close()
}
```

### Reading a BOX Archive

```kotlin
import dev.box.reader.BoxReaderImpl

fun main() {
    val reader = BoxReaderImpl()
    
    // Open an archive
    val container = reader.open("archive.box").value()
    
    // List contents
    println("Files: ${container.fileCount}")
    println("Folders: ${container.folderCount}")
    
    // Read metadata
    for ((key, value) in container.metadata()) {
        println("$key: $value")
    }
    
    // Extract all files
    container.extractAll("./output")
    
    // Close
    container.close()
}
```

### Validating a BOX Archive

```kotlin
import dev.box.validator.BoxValidatorImpl

fun main() {
    val validator = BoxValidatorImpl()
    val result = validator.validate("archive.box").value()
    
    if (result.isValid) {
        println("Archive is valid!")
    } else {
        println("Errors: ${result.errorCount}")
        for (error in result.errors) {
            println("  - ${error.code}: ${error.message}")
        }
    }
}
```

## API Reference

### BoxWriter

| Method | Description |
|--------|-------------|
| `create(path)` | Create a new BOX archive |
| `open(path)` | Open existing archive for appending |

### BoxReader

| Method | Description |
|--------|-------------|
| `open(path)` | Open a BOX archive for reading |
| `isBoxFile(path)` | Check if file is a valid BOX archive |
| `detectVersion(path)` | Get BOX format version |

### BoxContainer (read)

| Method | Description |
|--------|-------------|
| `entries()` | List all entries |
| `files()` | List all files |
| `folders()` | List all folders |
| `findFile(path)` | Find a file by path |
| `findFolder(path)` | Find a folder by path |
| `metadata()` | Read metadata |
| `extractAll(dest)` | Extract all files |

### BoxContainer (write)

| Method | Description |
|--------|-------------|
| `addFile(path, data)` | Add a file |
| `addFolder(path)` | Create a folder |
| `setMetadata(key, value)` | Set metadata |
| `save()` | Save the archive |

## Format Specification

See [BOX-FORMAT-SPEC-v1.0.md](../BOX-FORMAT-SPEC-v1.0.md) for the complete binary format specification.

See [BOX-SDK-SPEC-v1.0.md](../BOX-SDK-SPEC-v1.0.md) for the SDK API specification.

## License

BOX Format SDK is released under the MIT License.
