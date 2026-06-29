<div align="center">

# 📦 BOX Format SDK

**The Universal Open-Source Container Format**

[![GitHub release](https://img.shields.io/github/v/release/rishav/box-sdk?style=for-the-badge)](https://github.com/rishav/box-sdk/releases)
[![License](https://img.shields.io/github/license/rishav/box-sdk?style=for-the-badge)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/rishav/box-sdk/ci.yml?style=for-the-badge)](https://github.com/rishav/box-sdk/actions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Stars](https://img.shields.io/github/stars/rishav/box-sdk?style=for-the-badge)](https://github.com/rishav/box-sdk/stargazers)

---

[Features](#-features) • [Quick Start](#-quick-start) • [Documentation](#-documentation) • [Why BOX](#-why-box) • [Roadmap](#-roadmap)

---

</div>

## 🚀 Features

| Feature | Status |
|---------|--------|
| ✅ Create/Open/Close `.box` archives | ✅ v0.1 |
| ✅ Add/Extract files with full paths | ✅ v0.1 |
| ✅ Nested folder support | ✅ v0.1 |
| ✅ Metadata (Key-Value) | ✅ v0.1 |
| ✅ SHA-256 Checksums (Header, File, Chunk) | ✅ v0.1 |
| ✅ Binary Format Validation | ✅ v0.1 |
| ✅ Multi-module Kotlin SDK | ✅ v0.1 |
| 🔒 Compression (Zstd, LZ4) | 📅 v0.2 |
| 🔒 Encryption (AES-256-GCM) | 📅 v0.2 |
| 🔒 Streaming API | 📅 v0.2 |
| 🔒 Digital Signatures | 📅 v0.3 |

## 📖 Quick Start

### Prerequisites
- JDK 17+
- Terminal (Linux, macOS, Windows)

### Build & Run Demo

```bash
git clone https://github.com/rishav/box-sdk.git
cd box-sdk
./gradlew :samples:run
```

### Create Your Own BOX Archive

```kotlin
import dev.box.writer.BoxWriterImpl

val writer = BoxWriterImpl()
val result = writer.create("my.box")
val box = result.value()

// Add metadata
box.setMetadata(4, "Project Alpha")
box.setMetadata(5, "My first BOX archive")

// Create folders
box.addFolder("/documents")
box.addFolder("/documents/reports")

// Add files
box.addFile("/hello.txt", "Hello BOX!".toByteArray())
box.addFile("/documents/report.pdf", File("report.pdf").readBytes())

// Save
box.save()
box.close()
```

### Read & Extract

```kotlin
import dev.box.reader.BoxReaderImpl

val reader = BoxReaderImpl()
val result = reader.open("my.box")
val box = result.value()

// List contents
for (entry in box.entries()) {
    println("${entry.path}")
}

// Extract to folder
box.extractAll("./output/")
box.close()
```

### Validate

```kotlin
import dev.box.validator.BoxValidatorImpl

val validator = BoxValidatorImpl()
val result = validator.validate("my.box")
if (result.isValid) {
    println("✅ Archive is valid!")
} else {
    println("❌ Found ${result.errorCount} errors")
}
```

## 🎯 Why BOX?

| Aspect | BOX Format | ZIP | TAR |
|--------|-----------|-----|-----|
| **Specification** | ✅ Complete v1.0 Standard | ✅ Well-known | ✅ Well-known |
| **Checksums** | ✅ SHA-256 per chunk + file | ❌ None | ❌ None |
| **Metadata** | ✅ Built-in key-value | ❌ Requires extra files | ❌ Requires extra files |
| **Chunking** | ✅ File-level chunk table | ❌ Whole-file only | ❌ Whole-file only |
| **Modern Design** | ✅ Binary v1.0 spec with room for encryption/compression | Legacy | Legacy |
| **Sections** | ✅ Extensible frame-based structure | Fixed | Fixed |
| **License** | ✅ MIT - Free for everyone | Patented (DEFLATE) | ✅ Free |

BOX is **not** trying to replace ZIP. It is a **modern, extensible container format** designed for applications that need built-in integrity verification, metadata, and future-proof extensibility.

## 📚 Documentation

- 📘 [BOX Format Specification v1.0](specs/BOX-FORMAT-SPEC-v1.0.md) — Binary format standard
- 📘 [BOX SDK Specification v1.0](specs/BOX-SDK-SPEC-v1.0.md) — SDK architecture & API
- [API Reference](#-api-reference) — Module documentation

### Project Modules

| Module | Description |
|--------|-------------|
| `box-core` | Core data models, constants, result types |
| `box-api` | Public API interfaces |
| `box-parser` | Binary format reader (low-level) |
| `box-serializer` | Binary format writer (low-level) |
| `box-reader` | High-level container reader |
| `box-writer` | High-level container writer |
| `box-validator` | Archive validation & verification |

## 🛣️ Roadmap

### v0.1 — ✅ Current
- Create, open, close BOX archives
- Add, list, extract files and folders
- Metadata support
- SHA-256 checksums
- Binary format validation

### v0.2 — 🔜 Next
- Compression (Zstd, LZ4)
- Streaming read/write
- File append/update/delete
- Cross-platform GUI tool

### v0.3 — 🔮 Future
- Encryption (AES-256-GCM)
- Digital signatures
- Progress tracking
- FUSE filesystem mount

### v1.0 — 🎯 Stable Release
- Full spec compliance
- Performance optimizations
- Production-ready API

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing`)
5. Open a Pull Request

## 📋 Project Structure

```
box-sdk/
├── box-api/          # Public API interfaces
├── box-core/         # Core models + constants
├── box-parser/       # Binary reader
├── box-serializer/   # Binary writer
├── box-reader/       # Container reader impl
├── box-writer/       # Container writer impl
├── box-validator/    # Validation logic
├── samples/          # Demo application
├── tests/            # Integration tests
└── specs/            # Format specifications
```

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ for the open-source community**

⭐ Star this repo if you find it useful! ⭐

</div>
