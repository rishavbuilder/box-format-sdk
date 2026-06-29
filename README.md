# Box Format SDK

A Kotlin SDK for the Box archive format — a binary container format with built-in checksums, metadata, and extensible section-based design.

[![License](https://img.shields.io/github/license/rishavbuilder/box-sdk)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/rishavbuilder/box-sdk/ci.yml)](https://github.com/rishavbuilder/box-sdk/actions)

## Project status

Current release: **v0.1** — core functionality complete. Not yet stable for production use.

| Feature | Status |
|---------|--------|
| Create/open/save `.box` archives | Done |
| Add/list/extract files and folders | Done |
| Nested folder support | Done |
| Key-value metadata | Done |
| SHA-256 checksums (header, file, chunk) | Done |
| Binary format validation | Done |

Planned: compression, streaming, encryption.

## Quick start

```bash
git clone https://github.com/rishavbuilder/box-sdk.git
cd box-sdk
./gradlew :samples:run
```

### Create a box

```kotlin
import dev.box.writer.BoxWriterImpl

val writer = BoxWriterImpl()
val result = writer.create("archive.box")
val box = result.value()

box.setMetadata(4, "Author Name")
box.addFolder("/docs")
box.addFile("/docs/readme.txt", "Hello".toByteArray())
box.save()
box.close()
```

### Read a box

```kotlin
import dev.box.reader.BoxReaderImpl

val reader = BoxReaderImpl()
val result = reader.open("archive.box")
val box = result.value()

for (entry in box.entries()) {
    println(entry.path)
}
box.extractAll("./output/")
box.close()
```

## Modules

| Module | Purpose |
|--------|---------|
| `box-core` | Data models and constants |
| `box-api` | Public interfaces |
| `box-parser` | Binary file reader |
| `box-serializer` | Binary file writer |
| `box-reader` | High-level read API |
| `box-writer` | High-level write API |
| `box-validator` | Archive validation |
| `samples` | Demo application |
| `tests` | Integration tests |

## Documentation

- [Box Format Specification v1.0](specs/BOX-FORMAT-SPEC-v1.0.md)
- [Box SDK Specification v1.0](specs/BOX-SDK-SPEC-v1.0.md)

## Building

Requires JDK 17+.

```bash
./gradlew build
./gradlew test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

MIT — see [LICENSE](LICENSE).
