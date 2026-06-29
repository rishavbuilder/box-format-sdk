# BOX SDK Specification v1.0

**Official SDK API Specification**

**Document Status:** Proposed Standard  
**Version:** 1.0.0  
**Date:** 2026-06-29  
**Depends on:** BOX File Format Standard v1.0  
**Classification:** Open SDK Standard

---

## Copyright Notice

This specification is released under the Open File Format License (OFFL). Any developer may implement BOX SDKs without royalty obligations.

---

## Abstract

This document specifies the official SDK interface for the BOX File Format Standard v1.0. The SDK abstracts all binary format complexity behind a language-neutral object model that exposes container manipulation as filesystem-like operations. Every programming language implementing BOX support MUST conform to the interfaces defined herein or clearly document deviations.

---

## Table of Contents

- [Part I — Foundation](#part-i--foundation)
  - [Section 1 — Introduction](#section-1--introduction)
  - [Section 2 — Design Philosophy](#section-2--design-philosophy)
  - [Section 3 — Terminology](#section-3--terminology)
  - [Section 4 — Conformance](#section-4--conformance)
  - [Section 5 — Type System](#section-5--type-system)
- [Part II — Object Model](#part-ii--object-model)
  - [Section 6 — Object Hierarchy](#section-6--object-hierarchy)
  - [Section 7 — BOXEntry](#section-7--boxentry)
  - [Section 8 — BOXFile](#section-8--boxfile)
  - [Section 9 — BOXFolder](#section-9--boxfolder)
  - [Section 10 — BOXContainer](#section-10--boxcontainer)
  - [Section 11 — BOXMetadata](#section-11--boxmetadata)
  - [Section 12 — BOXStream](#section-12--boxstream)
  - [Section 13 — BOXChunk](#section-13--boxchunk)
  - [Section 14 — BOXIndex](#section-14--boxindex)
  - [Section 15 — BOXEncryption](#section-15--boxencryption)
  - [Section 16 — BOXSignature](#section-16--boxsignature)
  - [Section 17 — BOXStatistics](#section-17--boxstatistics)
  - [Section 18 — BOXValidator](#section-18--boxvalidator)
  - [Section 19 — BOXRepair](#section-19--boxrepair)
  - [Section 20 — BOXSearchResult](#section-20--boxsearchresult)
  - [Section 21 — BOXConfiguration](#section-21--boxconfiguration)
  - [Section 22 — BOXOptions](#section-22--boxoptions)
  - [Section 23 — BOXBuilder](#section-23--boxbuilder)
  - [Section 24 — BOXSession](#section-24--boxsession)
  - [Section 25 — BOXPath](#section-25--boxpath)
- [Part III — Static API](#part-iii--static-api)
  - [Section 26 — BOX Entry Point](#section-26--box-entry-point)
  - [Section 27 — Creation API](#section-27--creation-api)
  - [Section 28 — Opening API](#section-28--opening-api)
  - [Section 29 — Closing API](#section-29--closing-api)
  - [Section 30 — Saving API](#section-30--saving-api)
  - [Section 31 — Navigation API](#section-31--navigation-api)
  - [Section 32 — File Operations API](#section-32--file-operations-api)
  - [Section 33 — Folder Operations API](#section-33--folder-operations-api)
  - [Section 34 — Metadata Operations API](#section-34--metadata-operations-api)
  - [Section 35 — Streaming API](#section-35--streaming-api)
  - [Section 36 — Search API](#section-36--search-api)
  - [Section 37 — Encryption API](#section-37--encryption-api)
  - [Section 38 — Integrity API](#section-38--integrity-api)
  - [Section 39 — Validation API](#section-39--validation-api)
  - [Section 40 — Repair API](#section-40--repair-api)
  - [Section 41 — Statistics API](#section-41--statistics-api)
  - [Section 42 — Inspector API](#section-42--inspector-api)
- [Part IV — Builder Pattern](#part-iv--builder-pattern)
  - [Section 43 — Builder Flow](#section-43--builder-flow)
  - [Section 44 — Fluent API Rules](#section-44--fluent-api-rules)
- [Part V — Streaming Deep Dive](#part-v--streaming-deep-dive)
  - [Section 45 — Stream Types](#section-45--stream-types)
  - [Section 46 — Stream Lifecycle](#section-46--stream-lifecycle)
  - [Section 47 — Stream Protocols](#section-47--stream-protocols)
- [Part VI — Event System](#part-vi--event-system)
  - [Section 48 — Event Architecture](#section-48--event-architecture)
  - [Section 49 — Event Types](#section-49--event-types)
  - [Section 50 — Event Lifecycle](#section-50--event-lifecycle)
- [Part VII — Error System](#part-vii--error-system)
  - [Section 51 — Error Architecture](#section-51--error-architecture)
  - [Section 52 — Error Hierarchy](#section-52--error-hierarchy)
  - [Section 53 — Error Codes](#section-53--error-codes)
  - [Section 54 — Error Recovery](#section-54--error-recovery)
- [Part VIII — Thread Safety](#part-viii--thread-safety)
  - [Section 55 — Concurrency Model](#section-55--concurrency-model)
  - [Section 56 — Thread Safety Guarantees](#section-56--thread-safety-guarantees)
- [Part IX — Performance](#part-ix--performance)
  - [Section 57 — Performance Model](#section-57--performance-model)
  - [Section 58 — Lazy Loading](#section-58--lazy-loading)
  - [Section 59 — Memory Management](#section-59--memory-management)
  - [Section 60 — Large File Handling](#section-60--large-file-handling)
- [Part X — Cross-Platform](#part-x--cross-platform)
  - [Section 61 — Platform Independence](#section-61--platform-independence)
  - [Section 62 — Language Mapping Rules](#section-62--language-mapping-rules)
  - [Section 63 — Minimum Implementation](#section-63--minimum-implementation)
- [Part XI — Documentation](#part-xi--documentation)
  - [Section 64 — Object Relationship Diagram](#section-64--object-relationship-diagram)
  - [Section 65 — Lifecycle Diagrams](#section-65--lifecycle-diagrams)
  - [Section 66 — Sequence Diagrams](#section-66--sequence-diagrams)
  - [Section 67 — Usage Examples](#section-67--usage-examples)
  - [Section 68 — Best Practices](#section-68--best-practices)
  - [Section 69 — Migration Strategy](#section-69--migration-strategy)
- [Part XII — Appendices](#part-xii--appendices)
  - [Appendix A — Complete Type Reference](#appendix-a--complete-type-reference)
  - [Appendix B — Error Code Registry](#appendix-b--error-code-registry)
  - [Appendix C — Event Name Registry](#appendix-c--event-name-registry)

---

# Part I — Foundation

---

# Section 1 — Introduction

## 1.1 Purpose

This document specifies the official SDK interface for the BOX File Format. It defines every type, method, property, event, error code, and behavior that a conforming SDK implementation MUST expose.

## 1.2 Scope

This specification covers:

- The complete object model
- All public API surfaces
- Builder patterns and fluent APIs
- Streaming protocols
- Event systems
- Error hierarchies
- Thread safety contracts
- Performance requirements
- Cross-platform mapping rules

This specification does NOT cover:

- Internal implementation details
- Binary format parsing (covered in BOX File Format Standard v1.0)
- Specific language idioms (language adapters translate as needed)
- UI or visualization concerns

## 1.3 Intended Audience

- SDK implementers (one per target language)
- Application developers using the SDK
- Platform integrators embedding BOX support
- Conformance testers

## 1.4 Relationship to BOX Format Standard

This SDK specification depends on the BOX File Format Standard v1.0. All binary format details (magic bytes, section layouts, chunk structures) are defined in the format standard. This SDK specification defines the programmatic interface only.

## 1.5 Notation

- Type names use PascalCase: `BOXContainer`
- Method names use camelCase: `addFile()`
- Constants use SCREAMING_SNAKE_CASE: `ERROR_NOT_FOUND`
- Optional parameters are marked with `[optional]`
- Parameters with defaults show: `name: Type = defaultValue`
- Return types follow the signature: `method(params): ReturnType`
- Void returns are not annotated
- Properties show: `name: Type { get, set }` or `{ get }` for read-only

---

# Section 2 — Design Philosophy

## 2.1 Folder Abstraction

The fundamental design principle: a BOX container behaves like a folder. Developers think in terms of paths, files, and directories. They do not think in terms of chunks, indices, or binary offsets.

## 2.2 Familiarity

The API mirrors established filesystem APIs:

| Platform | Reference API |
|----------|---------------|
| Java/Kotlin | `java.nio.file` |
| Python | `pathlib.Path`, `os` |
| JavaScript | `fs` module |
| .NET | `System.IO` |
| Rust | `std::fs` |
| Go | `os`, `filepath` |
| C++ | `std::filesystem` |

## 2.3 Progressive Complexity

The SDK supports three usage levels:

1. **Simple**: 3-line create-add-save workflow
2. **Standard**: Full CRUD operations with metadata
3. **Advanced**: Streaming, encryption, chunk control, repair

## 2.4 Fail-Safe Defaults

Every operation has sensible defaults:

- Compression: enabled
- Encryption: disabled
- Checksum: SHA-256
- Chunk size: 4 MB
- Streaming: disabled

## 2.5 Immutability Where Possible

- `BOXEntry` is immutable after creation (modifications create new objects)
- `BOXMetadata` supports copy-on-modify
- `BOXConfiguration` is immutable once applied

## 2.6 Resource Management

The SDK uses the host language's resource management pattern:

- **Java/Kotlin**: `Closeable` / `AutoCloseable`
- **C++**: RAII / `std::unique_ptr`
- **Python**: Context manager (`with` statement)
- **JavaScript**: `Disposable` / `AsyncDisposable`
- **Rust**: `Drop` trait
- **Go**: `io.Closer`

---

# Section 3 — Terminology

## 3.1 Container

A `.box` file. The physical file on disk or in memory.

## 3.2 Entry

Any item within a container. Entries are either files or folders.

## 3.3 File Entry

An entry representing a regular file with data.

## 3.4 Folder Entry

An entry representing a directory. No data payload.

## 3.5 Path

A string identifying an entry within the container using `/` as the separator. Always starts with `/` for absolute paths.

## 3.6 Relative Path

A path not starting with `/`. Resolved relative to a given working directory within the container.

## 3.7 Absolute Path

A path starting with `/`. Unambiguous location within the container.

## 3.8 Entry ID

A numeric identifier assigned to each entry. Unique within the container.

## 3.9 Chunk

A contiguous block of file data. Large files are split into chunks for random access.

## 3.10 Metadata

Key-value pairs describing the container or its entries.

## 3.11 Tag

A single key-value pair in metadata.

## 3.12 Thumbnail

A small preview image associated with a file or container.

## 3.13 Stream

A sequential data channel for reading or writing file data without loading the entire file into memory.

## 3.14 Session

An active connection to a container, managing locks, transactions, and state.

## 3.15 Builder

A fluent configuration object for constructing containers or complex operations.

## 3.16 Result

A typed outcome of an operation, containing either a success value or an error.

## 3.17 Query

A structured search expression for finding entries.

---

# Section 4 — Conformance

## 4.1 Conformance Levels

| Level | Name | Requirements |
|-------|------|-------------|
| 1 | Basic | Create, open, read, write, close containers |
| 2 | Standard | + Metadata, folders, search, compression |
| 3 | Advanced | + Encryption, signatures, streaming, repair |
| 4 | Full | + All optional features, performance optimizations |

## 4.2 MUST / SHOULD / MAY

- MUST: Implementation MUST provide this behavior.
- SHOULD: Implementation SHOULD provide this behavior (with documented exception).
- MAY: Implementation MAY provide this behavior.

## 4.3 Deviation Reporting

Implementations that deviate from this specification MUST publish a conformance statement documenting all deviations.

---

# Section 5 — Type System

## 5.1 Primitive Types

| Type | Description | Size |
|------|-------------|------|
| `Byte` | Unsigned 8-bit integer | 1 byte |
| `Int32` | Signed 32-bit integer | 4 bytes |
| `Int64` | Signed 64-bit integer | 8 bytes |
| `UInt32` | Unsigned 32-bit integer | 4 bytes |
| `UInt64` | Unsigned 64-bit integer | 8 bytes |
| `Float64` | Double-precision float | 8 bytes |
| `Boolean` | True or false | 1 byte |
| `String` | UTF-8 encoded text | Variable |
| `Bytes` | Raw byte sequence | Variable |
| `Timestamp` | Unix milliseconds since epoch | 8 bytes |

## 5.2 Generic Types

| Type | Description |
|------|-------------|
| `Result<T>` | Success value of type T or an error |
| `List<T>` | Ordered collection of T |
| `Map<K, V>` | Key-value mapping |
| `Optional<T>` | Value that may be absent |
| `Pair<A, B>` | Tuple of two values |
| `Iterator<T>` | Sequential access to a collection |

## 5.3 Type Conversions

Implementations MUST support implicit conversion between:

- `String` and platform-native string type
- `Bytes` and platform-native byte array type
- `Timestamp` and platform-native date/time type
- `List<T>` and platform-native array/list type

---

# Part II — Object Model

---

# Section 6 — Object Hierarchy

## 6.1 Core Hierarchy

```
BOX (static entry point)
├── BOXContainer          (represents a .box file)
│   ├── BOXEntry          (abstract base for entries)
│   │   ├── BOXFile       (file entry)
│   │   └── BOXFolder     (folder entry)
│   ├── BOXMetadata       (container metadata)
│   ├── BOXIndex          (entry lookup index)
│   ├── BOXChunk          (data chunk reference)
│   ├── BOXEncryption     (encryption context)
│   ├── BOXSignature      (digital signature)
│   └── BOXStatistics     (container statistics)
├── BOXStream             (data streaming)
│   ├── BOXReadStream     (read-only stream)
│   └── BOXWriteStream    (write-only stream)
├── BOXBuilder            (fluent container builder)
├── BOXSession            (active container session)
├── BOXPath               (path abstraction)
├── BOXQuery              (search query builder)
├── BOXValidator          (validation engine)
├── BOXRepair             (repair engine)
├── BOXInspector          (binary inspector)
└── BOXConfiguration      (global configuration)
```

## 6.2 Composition Relationships

```
BOXContainer 1──* BOXEntry
BOXContainer 1──1 BOXMetadata
BOXContainer 1──1 BOXIndex
BOXContainer 0..1──1 BOXEncryption
BOXContainer 0..*──1 BOXSignature
BOXContainer 1──* BOXChunk
BOXFile 1──* BOXChunk
BOXEntry 0..1──1 BOXMetadata
BOXSession 1──1 BOXContainer
BOXBuilder 1──1 BOXContainer
BOXSearchResult *──1 BOXEntry
BOXValidator 1──0..1 BOXContainer
BOXRepair 1──0..1 BOXContainer
```

## 6.3 Ownership Rules

1. A `BOXContainer` owns all `BOXEntry` objects within it.
2. A `BOXEntry` is owned by exactly one `BOXContainer`.
3. A `BOXChunk` is owned by exactly one `BOXFile`.
4. `BOXMetadata` can exist independently (e.g., for copying between containers).
5. A `BOXSession` borrows a `BOXContainer` (does not own it).
6. A `BOXStream` borrows a `BOXFile` or `BOXContainer`.

---

# Section 7 — BOXEntry

## 7.1 Purpose

Abstract base type representing any item (file or folder) within a container.

## 7.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `id` | `UInt32` | get | Unique entry identifier |
| `name` | `String` | get | Entry name (filename or folder name) |
| `path` | `BOXPath` | get | Absolute path within container |
| `parent` | `Optional<BOXFolder>` | get | Parent folder (null for root) |
| `type` | `EntryType` | get | FILE or FOLDER |
| `createdAt` | `Timestamp` | get | Creation timestamp |
| `modifiedAt` | `Timestamp` | get | Last modification timestamp |
| `size` | `UInt64` | get | Size in bytes (0 for folders) |
| `checksum` | `Optional<String>` | get | SHA-256 checksum (if computed) |
| `isEncrypted` | `Boolean` | get | Whether entry data is encrypted |
| `isCompressed` | `Boolean` | get | Whether entry data is compressed |
| `isSymlink` | `Boolean` | get | Whether entry is a symbolic link |
| `isHidden` | `Boolean` | get | Whether entry is hidden |
| `mimeType` | `Optional<String>` | get | MIME type (if detected) |
| `extension` | `Optional<String>` | get | File extension (without dot) |
| `tags` | `List<String>` | get | User-assigned tags |
| `metadata` | `BOXMetadata` | get | Entry-specific metadata |

## 7.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `rename(newName: String): Result<BOXEntry>` | `Result<BOXEntry>` | Rename this entry |
| `move(destination: BOXFolder): Result<BOXEntry>` | `Result<BOXEntry>` | Move to another folder |
| `copy(destination: BOXFolder): Result<BOXEntry>` | `Result<BOXEntry>` | Copy within container |
| `delete(): Result<void>` | `Result<void>` | Remove from container |
| `addTag(tag: String): void` | `void` | Add a tag |
| `removeTag(tag: String): void` | `void` | Remove a tag |
| `hasTag(tag: String): Boolean` | `Boolean` | Check tag existence |
| `thumbnail(): Optional<Bytes>` | `Optional<Bytes>` | Get thumbnail image bytes |
| `setThumbnail(image: Bytes, format: ImageFormat): void` | `void` | Set thumbnail |

## 7.4 EntryType Enum

| Value | Description |
|-------|-------------|
| `FILE` | Regular file |
| `FOLDER` | Directory |
| `SYMLINK` | Symbolic link |

## 7.5 Immutability Rules

- `id`, `path`, `type` are immutable after creation.
- `name` is immutable (use `rename()` for changes).
- `size`, `checksum` are computed values (immutable).
- `tags`, `metadata` are mutable in-place.

---

# Section 8 — BOXFile

## 8.1 Purpose

Represents a file entry within a container. Extends `BOXEntry`.

## 8.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| (inherits all BOXEntry properties) | | | |
| `uncompressedSize` | `UInt64` | get | Original size before compression |
| `compressedSize` | `UInt64` | get | Size on disk (0 if not compressed) |
| `chunkCount` | `UInt32` | get | Number of data chunks |
| `isStreaming` | `Boolean` | get | Whether file is in streaming mode |
| `encoding` | `Optional<String>` | get | Text encoding (e.g., "UTF-8") |

## 8.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `read(): Result<Bytes>` | `Result<Bytes>` | Read entire file into memory |
| `read(offset: UInt64, length: UInt64): Result<Bytes>` | `Result<Bytes>` | Read byte range |
| `readChunk(index: UInt32): Result<Bytes>` | `Result<Bytes>` | Read specific chunk |
| `write(data: Bytes): Result<void>` | `Result<void>` | Write/replace file data |
| `append(data: Bytes): Result<void>` | `Result<void>` | Append to file data |
| `openReadStream(): Result<BOXReadStream>` | `Result<BOXReadStream>` | Open streaming reader |
| `openWriteStream(): Result<BOXWriteStream>` | `Result<BOXWriteStream>` | Open streaming writer |
| `chunks(): List<BOXChunk>` | `List<BOXChunk>` | List all chunks |
| `chunk(index: UInt32): Optional<BOXChunk>` | `Optional<BOXChunk>` | Get specific chunk |
| `extractTo(destinationPath: String): Result<void>` | `Result<void>` | Extract to filesystem |
| `setEncryption(key: BOXEncryption): void` | `void` | Set per-file encryption |
| `removeEncryption(): void` | `void` | Remove per-file encryption |

## 8.4 Read Operations

Reading is lazy by default:

```
file.read()           -- reads entire file (loads into memory)
file.read(0, 1024)    -- reads first 1024 bytes
file.readChunk(0)     -- reads first chunk only
file.openReadStream() -- opens a streaming reader (no data loaded yet)
```

## 8.5 Write Operations

Writing replaces existing data:

```
file.write(data)      -- replaces entire file content
file.append(data)     -- appends to existing content
```

Write operations are NOT atomic. If the process crashes mid-write, the file MAY be in a partial state. Use transactions for atomic multi-file updates (future extension).

---

# Section 9 — BOXFolder

## 9.1 Purpose

Represents a folder (directory) entry within a container. Extends `BOXEntry`.

## 9.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| (inherits all BOXEntry properties) | | | |
| `childCount` | `UInt32` | get | Direct child count |
| `descendantCount` | `UInt32` | get | Total descendant count |
| `isEmpty` | `Boolean` | get | True if no children |

## 9.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `children(): List<BOXEntry>` | `List<BOXEntry>` | List direct children |
| `files(): List<BOXFile>` | `List<BOXFile>` | List only file children |
| `folders(): List<BOXFolder>` | `List<BOXFolder>` | List only folder children |
| `find(name: String): Optional<BOXEntry>` | `Optional<BOXEntry>` | Find child by name |
| `addFile(name: String, data: Bytes): Result<BOXFile>` | `Result<BOXFile>` | Add file to this folder |
| `addFile(name: String, sourcePath: String): Result<BOXFile>` | `Result<BOXFile>` | Add file from filesystem |
| `addFolder(name: String): Result<BOXFolder>` | `Result<BOXFolder>` | Create subfolder |
| `remove(name: String): Result<void>` | `Result<void>` | Remove child by name |
| `renameChild(name: String, newName: String): Result<BOXEntry>` | `Result<BOXEntry>` | Rename a child |
| `walk(): Iterator<BOXEntry>` | `Iterator<BOXEntry>` | Recursive depth-first traversal |
| `walkFiles(): Iterator<BOXFile>` | `Iterator<BOXFile>` | Recursive file-only traversal |
| `extractTo(destinationPath: String): Result<void>` | `Result<void>` | Extract folder to filesystem |

## 9.4 Traversal Patterns

```
folder.children()         -- direct children only
folder.walk()             -- depth-first recursive
folder.walkFiles()        -- files only, recursive
folder.files()            -- direct file children only
folder.folders()          -- direct folder children only
```

---

# Section 10 — BOXContainer

## 10.1 Purpose

Represents a complete `.box` file. The primary object through which all operations are performed.

## 10.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `path` | `String` | get | Filesystem path to .box file |
| `name` | `String` | get | Container name |
| `id` | `UInt64` | get | Unique container identifier |
| `formatVersion` | `Pair<Int32, Int32>` | get | Format version (major, minor) |
| `createdAt` | `Timestamp` | get | Creation timestamp |
| `modifiedAt` | `Timestamp` | get | Last modification timestamp |
| `isModified` | `Boolean` | get | True if unsaved changes exist |
| `isEncrypted` | `Boolean` | get | True if container-level encryption active |
| `isCompressed` | `Boolean` | get | True if compression enabled |
| `isStreaming` | `Boolean` | get | True if in streaming mode |
| `isOpen` | `Boolean` | get | True if container is open |
| `entryCount` | `UInt32` | get | Total entry count |
| `fileCount` | `UInt32` | get | File entry count |
| `folderCount` | `UInt32` | get | Folder entry count |
| `totalSize` | `UInt64` | get | Total uncompressed data size |
| `storageSize` | `UInt64` | get | Actual file size on disk |
| `compressionRatio` | `Float64` | get | Compression ratio (0.0–1.0) |
| `root` | `BOXFolder` | get | Root folder |
| `metadata` | `BOXMetadata` | get | Container metadata |
| `statistics` | `BOXStatistics` | get | Container statistics |
| `configuration` | `BOXConfiguration` | get | Active configuration |

## 10.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `close(): Result<void>` | `Result<void>` | Close container and release resources |
| `save(): Result<void>` | `Result<void>` | Save changes to disk |
| `saveAs(path: String): Result<void>` | `Result<void>` | Save to new location |
| `flush(): Result<void>` | `Result<void>` | Flush pending writes to disk |
| `path(name: String): BOXPath` | `BOXPath` | Create a BOXPath |
| `find(path: String): Result<BOXEntry>` | `Result<BOXEntry>` | Find entry by path |
| `findFile(path: String): Result<BOXFile>` | `Result<BOXFile>` | Find file by path |
| `findFolder(path: String): Result<BOXFolder>` | `Result<BOXFolder>` | Find folder by path |
| `exists(path: String): Boolean` | `Boolean` | Check if path exists |
| `isFile(path: String): Boolean` | `Boolean` | Check if path is a file |
| `isFolder(path: String): Boolean` | `Boolean` | Check if path is a folder |
| `entries(): List<BOXEntry>` | `List<BOXEntry>` | List all entries |
| `files(): List<BOXFile>` | `List<BOXFile>` | List all files |
| `folders(): List<BOXFolder>` | `List<BOXFolder>` | List all folders |
| `search(query: String): List<BOXSearchResult>` | `List<BOXSearchResult>` | Search entries |
| `query(): BOXQuery` | `BOXQuery` | Create advanced query builder |
| `addFile(path: String, data: Bytes): Result<BOXFile>` | `Result<BOXFile>` | Add file at path |
| `addFile(path: String, sourcePath: String): Result<BOXFile>` | `Result<BOXFile>` | Add file from filesystem |
| `addFile(path: String, stream: BOXReadStream): Result<BOXFile>` | `Result<BOXFile>` | Add file from stream |
| `addFolder(path: String): Result<BOXFolder>` | `Result<BOXFolder>` | Create folder at path |
| `remove(path: String): Result<void>` | `Result<void>` | Remove entry at path |
| `rename(path: String, newName: String): Result<BOXEntry>` | `Result<BOXEntry>` | Rename entry |
| `move(source: String, destination: String): Result<BOXEntry>` | `Result<BOXEntry>` | Move entry |
| `copy(source: String, destination: String): Result<BOXEntry>` | `Result<BOXEntry>` | Copy entry |
| `extract(path: String, destinationPath: String): Result<void>` | `Result<void>` | Extract to filesystem |
| `extractAll(destinationPath: String): Result<void>` | `Result<void>` | Extract entire container |
| `stream(path: String): Result<BOXStream>` | `Result<BOXStream>` | Open stream for entry |
| `encrypt(key: BOXEncryption): Result<void>` | `Result<void>` | Encrypt container |
| `decrypt(key: BOXEncryption): Result<void>` | `Result<void>` | Decrypt container |
| `sign(privateKey: Bytes): Result<BOXSignature>` | `Result<BOXSignature>` | Sign container |
| `verify(): Result<Boolean>` | `Result<Boolean>` | Verify all checksums |
| `validate(): Result<BOXValidator>` | `Result<BOXValidator>` | Run full validation |
| `repair(): Result<BOXRepair>` | `Result<BOXRepair>` | Run repair |
| `inspect(): Result<BOXInspector>` | `Result<BOXInspector>` | Inspect binary structure |
| `history(): List<BOXHistoryEntry>` | `List<BOXHistoryEntry>` | Get modification history |
| `thumbnail(): Optional<Bytes>` | `Optional<Bytes>` | Get container thumbnail |
| `setThumbnail(image: Bytes, format: ImageFormat): void` | `void` | Set container thumbnail |

---

# Section 11 — BOXMetadata

## 11.1 Purpose

Stores key-value metadata for containers and entries.

## 11.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `tags` | `Map<String, String>` | get | All metadata tags |
| `tagCount` | `Int32` | get | Number of tags |
| `isEmpty` | `Boolean` | get | True if no tags |

## 11.3 Standard Tags

| Tag Key | Type | Description |
|---------|------|-------------|
| `name` | String | Container or entry name |
| `description` | String | Description text |
| `author` | String | Author name |
| `created` | Timestamp | Creation time |
| `modified` | Timestamp | Modification time |
| `tags` | String | Comma-separated tags |
| `language` | String | ISO 639-1 language code |
| `tool` | String | Software name |
| `toolVersion` | String | Software version |
| `contentType` | String | MIME type |
| `comment` | String | Free-form comment |

## 11.4 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `get(key: String): Optional<String>` | `Optional<String>` | Get tag value |
| `set(key: String, value: String): void` | `void` | Set tag value |
| `remove(key: String): Boolean` | `Boolean` | Remove tag (returns true if existed) |
| `has(key: String): Boolean` | `Boolean` | Check tag existence |
| `keys(): List<String>` | `List<String>` | List all tag keys |
| `values(): List<String>` | `List<String>` | List all tag values |
| `entries(): Map<String, String>` | `Map<String, String>` | Get all key-value pairs |
| `clear(): void` | `void` | Remove all tags |
| `merge(other: BOXMetadata): void` | `void` | Merge another metadata (overwrites) |
| `copy(): BOXMetadata` | `BOXMetadata` | Create a copy |
| `toJson(): String` | `String` | Export as JSON |
| `fromJson(json: String): void` | `void` | Import from JSON |
| `toMap(): Map<String, String>` | `Map<String, String>` | Convert to map |

---

# Section 12 — BOXStream

## 12.1 Purpose

Provides streaming access to file data without loading the entire file into memory.

## 12.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `isOpen` | `Boolean` | get | True if stream is open |
| `position` | `UInt64` | get | Current read/write position |
| `length` | `UInt64` | get | Total stream length |
| `isSeekable` | `Boolean` | get | True if random access supported |
| `isEof` | `Boolean` | get | True if at end of stream |

## 12.3 BOXReadStream Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `read(buffer: Bytes): Result<Int32>` | `Result<Int32>` | Read into buffer, returns bytes read |
| `read(maxBytes: UInt64): Result<Bytes>` | `Result<Bytes>` | Read up to N bytes |
| `readAll(): Result<Bytes>` | `Result<Bytes>` | Read entire stream |
| `readLine(): Result<String>` | `Result<String>` | Read one line (text mode) |
| `seek(position: UInt64): Result<void>` | `Result<void>` | Seek to position |
| `skip(n: UInt64): Result<void>` | `Result<void>` | Skip N bytes |
| `peek(): Result<Byte>` | `Result<Byte>` | Read without advancing |
| `close(): Result<void>` | `Result<void>` | Close stream |
| `iterator(): Iterator<Bytes>` | `Iterator<Bytes>` | Iterate in chunk-sized blocks |

## 12.4 BOXWriteStream Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `write(data: Bytes): Result<Int32>` | `Result<Int32>` | Write data, returns bytes written |
| `writeLine(text: String): Result<void>` | `Result<void>` | Write text line |
| `flush(): Result<void>` | `Result<void>` | Flush buffered data |
| `seek(position: UInt64): Result<void>` | `Result<void>` | Seek to position |
| `truncate(): Result<void>` | `Result<void>` | Truncate at current position |
| `close(): Result<void>` | `Result<void>` | Close stream and flush |

## 12.5 Streaming Modes

| Mode | Description |
|------|-------------|
| `SEQUENTIAL` | Forward-only reading/writing (default) |
| `RANDOM_ACCESS` | Seeking supported (requires chunk alignment) |
| `APPEND` | Write at end of stream |

---

# Section 13 — BOXChunk

## 13.1 Purpose

Represents a single data chunk within a file entry.

## 13.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `id` | `UInt64` | get | Unique chunk identifier |
| `index` | `UInt32` | get | Position within file (0-based) |
| `entryId` | `UInt32` | get | ID of owning file entry |
| `offset` | `UInt64` | get | Byte offset in container |
| `uncompressedSize` | `UInt64` | get | Payload size before compression |
| `storedSize` | `UInt64` | get | Actual size on disk |
| `isCompressed` | `Boolean` | get | Whether chunk is compressed |
| `isEncrypted` | `Boolean` | get | Whether chunk is encrypted |
| `isLast` | `Boolean` | get | Whether this is the last chunk for file |
| `checksum` | `Optional<String>` | get | Chunk checksum |

## 13.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `read(): Result<Bytes>` | `Result<Bytes>` | Read chunk data |
| `readDecompressed(): Result<Bytes>` | `Result<Bytes>` | Read and decompress |
| `verify(): Result<Boolean>` | `Result<Boolean>` | Verify chunk checksum |
| `next(): Optional<BOXChunk>` | `Optional<BOXChunk>` | Get next chunk in file |

---

# Section 14 — BOXIndex

## 14.1 Purpose

Provides fast lookup for entries by ID or path.

## 14.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `entryCount` | `UInt32` | get | Total entries indexed |
| `isStale` | `Boolean` | get | True if index needs rebuild |

## 14.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `findById(id: UInt32): Optional<BOXEntry>` | `Optional<BOXEntry>` | Lookup by entry ID |
| `findByPath(path: String): Optional<BOXEntry>` | `Optional<BOXEntry>` | Lookup by absolute path |
| `findByName(name: String): List<BOXEntry>` | `List<BOXEntry>` | Find entries by name |
| `findByExtension(ext: String): List<BOXFile>` | `List<BOXFile>` | Find files by extension |
| `findByMime(mimeType: String): List<BOXFile>` | `List<BOXFile>` | Find files by MIME type |
| `rebuild(): void` | `void` | Rebuild the index |
| `entries(): Iterator<BOXEntry>` | `Iterator<BOXEntry>` | Iterate all entries |

---

# Section 15 — BOXEncryption

## 15.1 Purpose

Manages encryption context for containers and files.

## 15.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `algorithm` | `EncryptionAlgorithm` | get | Encryption algorithm |
| `keySize` | `Int32` | get | Key size in bits |
| `hasKey` | `Boolean` | get | True if key is loaded |

## 15.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `fromPassword(password: String, algorithm: EncryptionAlgorithm): BOXEncryption` | `BOXEncryption` | Create from password (static) |
| `fromKey(key: Bytes, algorithm: EncryptionAlgorithm): BOXEncryption` | `BOXEncryption` | Create from raw key (static) |
| `fromKeyFile(keyFilePath: String): BOXEncryption` | `BOXEncryption` | Create from key file (static) |
| `generateKey(algorithm: EncryptionAlgorithm): BOXEncryption` | `BOXEncryption` | Generate random key (static) |
| `exportKey(): Result<Bytes>` | `Result<Bytes>` | Export raw key |
| `saveKeyFile(path: String): Result<void>` | `Result<void>` | Save key to file |
| `changePassword(oldPassword: String, newPassword: String): Result<void>` | `Result<void>` | Change password |

## 15.4 EncryptionAlgorithm Enum

| Value | Description |
|-------|-------------|
| `NONE` | No encryption |
| `AES_256_GCM` | AES-256 in GCM mode |
| `AES_256_CBC` | AES-256 in CBC mode |
| `CHACHA20_POLY1305` | ChaCha20-Poly1305 |
| `XCHACHA20_POLY1305` | XChaCha20-Poly1305 |

---

# Section 16 — BOXSignature

## 16.1 Purpose

Represents a digital signature applied to a container or entry.

## 16.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `algorithm` | `SignatureAlgorithm` | get | Signing algorithm |
| `keyReference` | `String` | get | Key identifier (not the key itself) |
| `signedAt` | `Timestamp` | get | Signature creation time |
| `isValid` | `Boolean` | get | True if signature verified successfully |
| `signedDataSize` | `UInt64` | get | Size of signed data |

## 16.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `verify(publicKey: Bytes): Result<Boolean>` | `Result<Boolean>` | Verify signature |
| `verify(trustedKeys: List<Bytes>): Result<Boolean>` | `Result<Boolean>` | Verify against multiple keys |

## 16.4 SignatureAlgorithm Enum

| Value | Description |
|-------|-------------|
| `ED25519` | Ed25519 |
| `ECDSA_P256` | ECDSA with P-256 curve |
| `ECDSA_P384` | ECDSA with P-384 curve |
| `RSA_2048` | RSA 2048-bit |
| `RSA_4096` | RSA 4096-bit |

---

# Section 17 — BOXStatistics

## 17.1 Purpose

Provides aggregate statistics about a container.

## 17.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `entryCount` | `UInt32` | get | Total entries |
| `fileCount` | `UInt32` | get | File entries |
| `folderCount` | `UInt32` | get | Folder entries |
| `totalUncompressedSize` | `UInt64` | get | Total uncompressed bytes |
| `totalCompressedSize` | `UInt64` | get | Total compressed bytes |
| `compressionRatio` | `Float64` | get | Compression ratio |
| `chunkCount` | `UInt32` | get | Total chunks |
| `averageChunkSize` | `UInt64` | get | Average chunk size |
| `largestFile` | `Optional<BOXFile>` | get | Largest file entry |
| `smallestFile` | `Optional<BOXFile>` | get | Smallest file entry |
| `deepestFolder` | `Int32` | get | Maximum folder depth |
| `encryptionAlgorithm` | `EncryptionAlgorithm` | get | Encryption algorithm used |
| `checksumAlgorithm` | `String` | get | Checksum algorithm used |
| `formatVersion` | `Pair<Int32, Int32>` | get | Format version |
| `entryTypeDistribution` | `Map<String, UInt32>` | get | Count by extension |
| `mimeTypeDistribution` | `Map<String, UInt32>` | get | Count by MIME type |
| `sizeDistribution` | `Map<String, UInt64>` | get | Size by extension |

---

# Section 18 — BOXValidator

## 18.1 Purpose

Performs comprehensive validation of a BOX container.

## 18.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `isValid` | `Boolean` | get | True if no errors found |
| `errorCount` | `Int32` | get | Number of errors |
| `warningCount` | `Int32` | get | Number of warnings |
| `errors` | `List<BOXValidationError>` | get | All errors |
| `warnings` | `List<BOXValidationWarning>` | get | All warnings |

## 18.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `validateHeader(): Result<Boolean>` | `Result<Boolean>` | Validate header structure |
| `validateSections(): Result<Boolean>` | `Result<Boolean>` | Validate all sections |
| `validateChecksums(): Result<Boolean>` | `Result<Boolean>` | Verify all checksums |
| `validateSignatures(): Result<Boolean>` | `Result<Boolean>` | Verify all signatures |
| `validateIntegrity(): Result<Boolean>` | `Result<Boolean>` | Full integrity check |
| `report(): BOXValidationReport` | `BOXValidationReport` | Generate detailed report |

---

# Section 19 — BOXRepair

## 19.1 Purpose

Repairs corrupted BOX containers.

## 19.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `corruptionFound` | `Boolean` | get | True if corruption detected |
| `repairable` | `Boolean` | get | True if auto-repair possible |
| `repairCount` | `Int32` | get | Number of repairs performed |

## 19.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `scan(): Result<BOXCorruptionReport>` | `Result<BOXCorruptionReport>` | Scan for corruption |
| `repair(): Result<BOXRepairReport>` | `Result<BOXRepairReport>` | Attempt auto-repair |
| `repairTo(outputPath: String): Result<BOXRepairReport>` | `Result<BOXRepairReport>` | Repair to new file |

## 19.4 BOXCorruptionReport

| Property | Type | Description |
|----------|------|-------------|
| `corruptedChunks` | `List<UInt64>` | IDs of corrupted chunks |
| `corruptedSections` | `List<String>` | Names of corrupted sections |
| `missingMetadata` | `Boolean` | True if metadata section missing |
| `brokenIndex` | `Boolean` | True if index section corrupted |
| `recoverableDataSize` | `UInt64` | Bytes that can be recovered |

---

# Section 20 — BOXSearchResult

## 20.1 Purpose

Represents a single search result.

## 20.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `entry` | `BOXEntry` | get | Matched entry |
| `relevance` | `Float64` | get | Relevance score (0.0–1.0) |
| `matchField` | `String` | get | Which field matched |
| `matchContext` | `Optional<String>` | get | Surrounding text (for text search) |

---

# Section 21 — BOXConfiguration

## 21.1 Purpose

Global SDK configuration.

## 21.2 Properties

| Property | Type | Access | Default | Description |
|----------|------|--------|---------|-------------|
| `defaultChunkSize` | `UInt64` | get, set | 4194304 | Default chunk size in bytes |
| `defaultCompression` | `CompressionAlgorithm` | get, set | ZSTD | Default compression algorithm |
| `compressionLevel` | `Int32` | get, set | 3 | Compression level (0–5) |
| `defaultChecksum` | `ChecksumAlgorithm` | get, set | SHA256 | Default checksum algorithm |
| `verifyOnRead` | `Boolean` | get, set | true | Verify checksums when reading |
| `verifyOnWrite` | `Boolean` | get, set | true | Compute checksums when writing |
| `autoFlush` | `Boolean` | get, set | true | Auto-flush after writes |
| `tempDirectory` | `String` | get, set | system temp | Temporary file directory |
| `maxOpenFiles` | `Int32` | get, set | 64 | Max concurrent open streams |
| `readBufferSize` | `UInt64` | get, set | 65536 | Read buffer size |
| `writeBufferSize` | `UInt64` | get, set | 65536 | Write buffer size |
| `eventHandler` | `Optional<BOXEventHandler>` | get, set | null | Global event handler |

## 21.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `copy(): BOXConfiguration` | `BOXConfiguration` | Create a copy |
| `merge(other: BOXConfiguration): BOXConfiguration` | `BOXConfiguration` | Merge configurations |
| `apply(container: BOXContainer): void` | `void` | Apply to container |

---

# Section 22 — BOXOptions

## 22.1 Purpose

Per-operation configuration options.

## 22.2 Properties

| Property | Type | Access | Default | Description |
|----------|------|--------|---------|-------------|
| `compression` | `Optional<CompressionAlgorithm>` | get, set | null | Override compression |
| `compressionLevel` | `Optional<Int32>` | get, set | null | Override level |
| `encryption` | `Optional<BOXEncryption>` | get, set | null | Set encryption |
| `chunkSize` | `Optional<UInt64>` | get, set | null | Override chunk size |
| `verify` | `Boolean` | get, set | true | Verify integrity |
| `overwrite` | `Boolean` | get, set | false | Overwrite existing |
| `createParents` | `Boolean` | get, set | true | Create parent folders |
| `preserveTimestamps` | `Boolean` | get, set | true | Keep original timestamps |
| `followSymlinks` | `Boolean` | get, set | false | Follow symbolic links |
| `recursive` | `Boolean` | get, set | false | Recursive operations |
| `filter` | `Optional<BOXEntryFilter>` | get, set | null | Entry filter function |
| `metadata` | `Optional<BOXMetadata>` | get, set | null | Metadata to apply |
| `tags` | `List<String>` | get, set | [] | Tags to apply |
| `thumbnail` | `Optional<Bytes>` | get, set | null | Thumbnail image |
| `encoding` | `Optional<String>` | get, set | null | Text encoding |
| `progressHandler` | `Optional<BOXProgressHandler>` | get, set | null | Progress callback |
| `cancelToken` | `Optional<BOXCancelToken>` | get, set | null | Cancellation signal |

## 22.3 Static Factory Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `default(): BOXOptions` | `BOXOptions` | Create with defaults |
| `compressed(algorithm: CompressionAlgorithm): BOXOptions` | `BOXOptions` | Compressed options |
| `encrypted(encryption: BOXEncryption): BOXOptions` | `BOXOptions` | Encrypted options |
| `fast(): BOXOptions` | `BOXOptions` | Optimized for speed |
| `small(): BOXOptions` | `BOXOptions` | Optimized for size |

## 22.4 BOXEntryFilter Interface

| Method | Returns | Description |
|--------|---------|-------------|
| `accept(entry: BOXEntry): Boolean` | `Boolean` | Return true to include entry |

## 22.5 BOXProgressHandler Interface

| Method | Returns | Description |
|--------|---------|-------------|
| `onProgress(current: UInt64, total: UInt64, message: String): void` | `void` | Progress update |

## 22.6 BOXCancelToken Interface

| Method | Returns | Description |
|--------|---------|-------------|
| `isCancelled(): Boolean` | `Boolean` | Check if cancelled |
| `cancel(): void` | `void` | Request cancellation |

---

# Section 23 — BOXBuilder

## 23.1 Purpose

Fluent builder for constructing BOX containers.

## 23.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `isBuilt` | `Boolean` | get | True if build() already called |

## 23.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `name(name: String): BOXBuilder` | `BOXBuilder` | Set container name |
| `metadata(key: String, value: String): BOXBuilder` | `BOXBuilder` | Set metadata tag |
| `metadata(metadata: BOXMetadata): BOXBuilder` | `BOXBuilder` | Set all metadata |
| `thumbnail(image: Bytes, format: ImageFormat): BOXBuilder` | `BOXBuilder` | Set thumbnail |
| `compression(algorithm: CompressionAlgorithm): BOXBuilder` | `BOXBuilder` | Set compression |
| `compressionLevel(level: Int32): BOXBuilder` | `BOXBuilder` | Set compression level |
| `encryption(encryption: BOXEncryption): BOXBuilder` | `BOXBuilder` | Set encryption |
| `chunkSize(size: UInt64): BOXBuilder` | `BOXBuilder` | Set chunk size |
| `options(options: BOXOptions): BOXBuilder` | `BOXBuilder` | Set options |
| `addFile(path: String, data: Bytes): BOXBuilder` | `BOXBuilder` | Add file from bytes |
| `addFile(path: String, sourcePath: String): BOXBuilder` | `BOXBuilder` | Add file from filesystem |
| `addFile(path: String, sourcePath: String, options: BOXOptions): BOXBuilder` | `BOXBuilder` | Add file with options |
| `addFolder(path: String): BOXBuilder` | `BOXBuilder` | Add folder |
| `addFiles(sourceDir: String, filter: BOXEntryFilter): BOXBuilder` | `BOXBuilder` | Add directory tree |
| `addFiles(sourceDir: String): BOXBuilder` | `BOXBuilder` | Add directory tree (no filter) |
| `build(): Result<BOXContainer>` | `Result<BOXContainer>` | Build and write container |

## 23.4 Static Factory

| Method | Returns | Description |
|--------|---------|-------------|
| `create(outputPath: String): BOXBuilder` | `BOXBuilder` | Start new container |
| `from(sourceDir: String, outputPath: String): BOXBuilder` | `BOXBuilder` | Create from directory |

---

# Section 24 — BOXSession

## 24.1 Purpose

Manages an active connection to a container with state tracking.

## 24.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `container` | `BOXContainer` | get | Associated container |
| `isOpen` | `Boolean` | get | True if session active |
| `isReadOnly` | `Boolean` | get | True if opened read-only |
| `transactionActive` | `Boolean` | get | True if in transaction |
| `modifiedEntries` | `List<BOXEntry>` | get | Entries modified in this session |

## 24.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `beginTransaction(): Result<void>` | `Result<void>` | Start transaction |
| `commit(): Result<void>` | `Result<void>` | Commit transaction |
| `rollback(): Result<void>` | `Result<void>` | Rollback transaction |
| `checkpoint(): Result<void>` | `Result<void>` | Create savepoint |
| `refresh(): Result<void>` | `Result<void>` | Reload from disk |
| `close(): Result<void>` | `Result<void>` | Close session |

---

# Section 25 — BOXPath

## 25.1 Purpose

Represents and manipulates paths within a BOX container.

## 25.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `path` | `String` | get | Normalized path string |
| `parent` | `Optional<BOXPath>` | get | Parent path |
| `name` | `String` | get | Final component name |
| `extension` | `Optional<String>` | get | File extension (without dot) |
| `isAbsolute` | `Boolean` | get | True if starts with / |
| `isRoot` | `Boolean` | get | True if path is "/" |
| `depth` | `Int32` | get | Number of path components |

## 25.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `resolve(relative: String): BOXPath` | `BOXPath` | Resolve relative path |
| `resolve(other: BOXPath): BOXPath` | `BOXPath` | Resolve another path |
| `relativize(base: BOXPath): BOXPath` | `BOXPath` | Compute relative path |
| `parent(count: Int32): Optional<BOXPath>` | `Optional<BOXPath>` | Get ancestor |
| `startsWith(other: BOXPath): Boolean` | `Boolean` | Check prefix |
| `endsWith(name: String): Boolean` | `Boolean` | Check suffix |
| `normalize(): BOXPath` | `BOXPath` | Remove . and .. |
| `toString(): String` | `String` | Convert to string |

## 25.4 Static Factory

| Method | Returns | Description |
|--------|---------|-------------|
| `of(path: String): BOXPath` | `BOXPath` | Create from string |
| `root(): BOXPath` | `BOXPath` | Create root path |
| `join(parts: List<String>): BOXPath` | `BOXPath` | Join path parts |

---

# Part III — Static API

---

# Section 26 — BOX Entry Point

## 26.1 Purpose

The `BOX` type is the static entry point for all SDK operations. It provides factory methods and utility functions.

## 26.2 Properties

| Property | Type | Access | Description |
|----------|------|--------|-------------|
| `version` | `String` | get | SDK version string |
| `formatVersion` | `Pair<Int32, Int32>` | get | Supported format version |
| `configuration` | `BOXConfiguration` | get | Global configuration |

## 26.3 Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `create(path: String): BOXBuilder` | `BOXBuilder` | Start creating a container |
| `create(path: String, options: BOXOptions): BOXBuilder` | `BOXBuilder` | Create with options |
| `open(path: String): Result<BOXContainer>` | `Result<BOXContainer>` | Open existing container |
| `open(path: String, options: BOXOptions): Result<BOXContainer>` | `Result<BOXContainer>` | Open with options |
| `openReadOnly(path: String): Result<BOXContainer>` | `Result<BOXContainer>` | Open read-only |
| `openSession(path: String): Result<BOXSession>` | `Result<BOXSession>` | Open with session |
| `openSession(path: String, readOnly: Boolean): Result<BOXSession>` | `Result<BOXSession>` | Open session with mode |
| `validate(path: String): Result<BOXValidator>` | `Result<BOXValidator>` | Validate container |
| `validate(path: String, options: BOXOptions): Result<BOXValidator>` | `Result<BOXValidator>` | Validate with options |
| `repair(path: String): Result<BOXRepair>` | `Result<BOXRepair>` | Repair container |
| `repair(path: String, outputPath: String): Result<BOXRepair>` | `Result<BOXRepair>` | Repair to new file |
| `inspect(path: String): Result<BOXInspector>` | `Result<BOXInspector>` | Inspect binary |
| `info(path: String): Result<BOXStatistics>` | `Result<BOXStatistics>` | Get statistics |
| `info(path: String, quick: Boolean): Result<BOXStatistics>` | `Result<BOXStatistics>` | Get statistics (quick mode) |
| `isBOX(path: String): Boolean` | `Boolean` | Check if file is BOX format |
| `detectVersion(path: String): Result<Pair<Int32, Int32>>` | `Result<Pair<Int32, Int32>>` | Detect format version |
| `configure(config: BOXConfiguration): void` | `void` | Set global configuration |
| `setEventHandler(handler: BOXEventHandler): void` | `void` | Set global event handler |

---

# Section 27 — Creation API

## 27.1 Purpose

Creating new BOX containers.

## 27.2 Simple Creation

```
BOX.create("output.box")
    .addFile("/readme.txt", readmeBytes)
    .addFolder("/images")
    .addFile("/images/photo.jpg", photoBytes)
    .build()
```

## 27.3 Creation with Options

```
BOX.create("output.box")
    .name("My Archive")
    .metadata("author", "John")
    .compression(ZSTD)
    .encryption(BOXEncryption.fromPassword("secret"))
    .addFile("/data.csv", csvBytes)
    .build()
```

## 27.4 Creation from Directory

```
BOX.create("output.box")
    .addFiles("/home/user/project")
    .build()
```

## 27.5 Builder Contract

1. `build()` MUST be called exactly once.
2. After `build()`, the builder is consumed and MUST NOT be reused.
3. If `build()` fails, the output file MUST be cleaned up.
4. `build()` is atomic — either the entire container is written or nothing is.

---

# Section 28 — Opening API

## 28.1 Purpose

Opening existing BOX containers for reading or modification.

## 28.2 Read-Write Opening

```
Result<BOXContainer> result = BOX.open("archive.box")
BOXContainer container = result.value()
// ... perform operations ...
container.save()
container.close()
```

## 28.3 Read-Only Opening

```
Result<BOXContainer> result = BOX.openReadOnly("archive.box")
BOXContainer container = result.value()
// ... read operations only ...
container.close()
```

## 28.4 Opening with Session

```
Result<BOXSession> result = BOX.openSession("archive.box")
BOXSession session = result.value()
session.beginTransaction()
// ... modify ...
session.commit()
session.close()
```

## 28.5 Opening Failures

If opening fails, the Result contains a BOXError with:

- `ERROR_FILE_NOT_FOUND` — file does not exist
- `ERROR_INVALID_FORMAT` — not a valid BOX file
- `ERROR_UNSUPPORTED_VERSION` — format version too new
- `ERROR_PERMISSION_DENIED` — insufficient permissions
- `ERROR_CORRUPTED` — header checksum failed

---

# Section 29 — Closing API

## 29.1 Purpose

Releasing resources associated with a container.

## 29.2 Close Contract

1. `close()` MUST flush all pending writes.
2. `close()` MUST release all file handles.
3. `close()` MUST be idempotent (safe to call multiple times).
4. After `close()`, all operations on the container MUST fail with `ERROR_CLOSED`.
5. If the container has unsaved changes, `close()` SHOULD prompt or auto-save based on configuration.

## 29.3 Resource Management

Implementations MUST support the host language's resource management:

```
// Java/Kotlin
try (BOXContainer box = BOX.open("file.box").value()) {
    // work with box
}

// Python
with BOX.open("file.box") as box:
    # work with box

// JavaScript
await using box = await BOX.open("file.box")
// work with box

// Rust
let box = BOX::open("file.box")?;
// box dropped here
```

---

# Section 30 — Saving API

## 30.1 Purpose

Persisting changes to disk.

## 30.2 Save Operations

| Method | Description |
|--------|-------------|
| `save()` | Save changes to original file |
| `saveAs(path)` | Save to new file |
| `flush()` | Flush pending writes without closing |

## 30.3 Save Semantics

1. `save()` MUST be atomic: write to temp file, then rename.
2. `save()` MUST update the header timestamps.
3. `save()` MUST recompute all checksums.
4. `save()` MUST update the section offsets.
5. `save()` SHOULD preserve the original file on failure.

## 30.4 Auto-Save

If `autoFlush` is enabled in configuration, every write operation automatically triggers a save. This impacts performance but ensures consistency.

---

# Section 31 — Navigation API

## 31.1 Purpose

Browsing and finding entries within a container.

## 31.2 Path-Based Navigation

```
BOXEntry entry = container.find("/Documents/readme.md").value()
BOXFile file = container.findFile("/Documents/readme.md").value()
BOXFolder folder = container.findFolder("/Documents").value()
Boolean exists = container.exists("/Documents")
Boolean isFile = container.isFile("/Documents/readme.md")
```

## 31.3 Listing Operations

```
List<BOXEntry> all = container.entries()
List<BOXFile> files = container.files()
List<BOXFolder> folders = container.folders()
```

## 31.4 Folder Navigation

```
BOXFolder root = container.root
List<BOXEntry> children = root.children()
List<BOXFile> files = root.files()
List<BOXFolder> subfolders = root.folders()
Optional<BOXEntry> child = root.find("readme.md")
```

## 31.5 Recursive Traversal

```
for (entry in container.root.walk()) {
    // depth-first traversal
}

for (file in container.root.walkFiles()) {
    // files only, recursive
}
```

---

# Section 32 — File Operations API

## 32.1 Purpose

Adding, reading, modifying, and removing files.

## 32.2 Adding Files

```
// From bytes
BOXFile file = container.addFile("/data.txt", bytes).value()

// From filesystem
BOXFile file = container.addFile("/photo.jpg", "/home/user/photo.jpg").value()

// From stream
BOXReadStream source = openLocalFile("/large.bin")
BOXFile file = container.addFile("/archive/large.bin", source).value()

// With options
BOXOptions opts = BOXOptions.compressed(ZSTD)
BOXFile file = container.addFile("/data.txt", bytes, opts).value()
```

## 32.3 Reading Files

```
// Read entire file
Bytes data = file.read().value()

// Read byte range
Bytes partial = file.read(0, 1024).value()

// Read specific chunk
Bytes chunk = file.readChunk(0).value()

// Open stream
BOXReadStream stream = file.openReadStream().value()
Bytes block = stream.read(65536).value()
stream.close()
```

## 32.4 Modifying Files

```
// Replace content
file.write(newBytes).value()

// Append content
file.append(extraBytes).value()

// Write to stream
BOXWriteStream stream = file.openWriteStream().value()
stream.write(chunk1).value()
stream.write(chunk2).value()
stream.close()
```

## 32.5 Removing Files

```
Result<void> result = file.delete()
// or
Result<void> result = container.remove("/data.txt")
```

## 32.6 Copying and Moving

```
// Copy within container
BOXFolder dest = container.findFolder("/backup").value()
BOXFile copy = file.copy(dest).value()

// Move within container
BOXFolder dest = container.findFolder("/archive").value()
BOXFile moved = file.move(dest).value()

// Rename
BOXFile renamed = file.rename("newname.txt").value()

// Copy to filesystem
file.extractTo("/home/user/extracted").value()
```

---

# Section 33 — Folder Operations API

## 33.1 Purpose

Creating, listing, and managing folders.

## 33.2 Creating Folders

```
BOXFolder folder = container.addFolder("/Documents/Projects").value()
```

Parent folders are created automatically if `createParents` is true (default).

## 33.3 Listing Folder Contents

```
BOXFolder folder = container.findFolder("/Documents").value()
List<BOXEntry> children = folder.children()
Int32 count = folder.childCount
Int32 total = folder.descendantCount
Boolean empty = folder.isEmpty
```

## 33.4 Extracting Folders

```
// Extract to filesystem
folder.extractTo("/home/user/extracted/Documents").value()

// Extract entire container
container.extractAll("/home/user/extracted").value()

// Extract specific path
container.extract("/Documents", "/home/user/backup").value()
```

---

# Section 34 — Metadata Operations API

## 34.1 Purpose

Reading and writing metadata.

## 34.2 Container Metadata

```
BOXMetadata meta = container.metadata

// Read
Optional<String> author = meta.get("author")
String name = meta.get("name").value()

// Write
meta.set("author", "John Doe")
meta.set("description", "Project archive")

// Remove
meta.remove("comment")

// Bulk
meta.merge(otherMetadata)
meta.clear()
```

## 34.3 Entry Metadata

```
BOXMetadata meta = file.metadata
meta.set("category", "documentation")
meta.set("priority", "high")
```

## 34.4 Tags

```
file.addTag("important")
file.addTag("draft")
Boolean hasTag = file.hasTag("important")
file.removeTag("draft")
```

## 34.5 Export/Import

```
// Export to JSON
String json = meta.toJson()

// Import from JSON
meta.fromJson(jsonString)

// Convert to map
Map<String, String> map = meta.toMap()
```

## 34.6 Thumbnails

```
// Get thumbnail
Optional<Bytes> thumb = file.thumbnail()

// Set thumbnail
Bytes thumbImage = loadThumb()
file.setThumbnail(thumbImage, IMAGE_FORMAT_JPEG)

// Container thumbnail
Optional<Bytes> containerThumb = container.thumbnail()
container.setThumbnail(thumbImage, IMAGE_FORMAT_PNG)
```

---

# Section 35 — Streaming API

## 35.1 Purpose

Reading and writing file data without full memory load.

## 35.2 Opening Streams

```
// Stream from file entry
BOXStream stream = container.stream("/video.mp4").value()

// Stream from file entry (typed)
BOXReadStream readStream = file.openReadStream().value()
BOXWriteStream writeStream = file.openWriteStream().value()
```

## 35.3 Reading Streams

```
BOXReadStream stream = file.openReadStream().value()

// Read blocks
while (!stream.isEof) {
    Bytes block = stream.read(65536).value()
    process(block)
}

// Read all at once (loads into memory)
Bytes all = stream.readAll().value()

// Read lines (text)
String line = stream.readLine().value()

// Seek
stream.seek(1024).value()

// Iterate in chunks
for (Bytes chunk : stream) {
    process(chunk)
}

stream.close()
```

## 35.4 Writing Streams

```
BOXWriteStream stream = file.openWriteStream().value()

stream.write(headerBytes).value()
stream.write(bodyBytes).value()
stream.flush().value()
stream.close()
```

## 35.5 Streaming Modes

| Mode | Use Case |
|------|----------|
| `SEQUENTIAL` | Video/audio playback, log files |
| `RANDOM_ACCESS` | Database files, random seek |
| `APPEND` | Log accumulation |

## 35.6 Large File Streaming

For files larger than available memory:

```
BOXReadStream stream = file.openReadStream().value()
UInt64 totalSize = stream.length

while (!stream.isEof) {
    Bytes block = stream.read(1048576).value()  // 1 MB blocks
    writeToFile(block)
}

stream.close()
```

---

# Section 36 — Search API

## 36.1 Purpose

Finding entries matching criteria.

## 36.2 Simple Search

```
List<BOXSearchResult> results = container.search("readme")
```

Simple search matches against file names, paths, tags, and metadata values.

## 36.3 Advanced Query Builder

```
BOXQuery query = container.query()
    .nameContains("report")
    .extension("pdf")
    .minSize(1024)
    .maxSize(10485760)
    .createdAfter(2026-01-01)
    .modifiedBefore(2026-12-31)
    .tag("important")
    .metadata("author", "John")
    .mimeType("application/pdf")
    .inFolder("/Documents")
    .recursive(true)

List<BOXSearchResult> results = container.search(query)
```

## 36.4 Search by Name

```
List<BOXEntry> entries = container.index().findByName("readme")
List<BOXFile> files = container.index().findByExtension("jpg")
List<BOXFile> pdfs = container.index().findByMime("application/pdf")
```

## 36.5 BOXQuery Methods

| Method | Returns | Description |
|--------|---------|-------------|
| `nameContains(substring: String)` | `BOXQuery` | Name contains substring |
| `nameStartsWith(prefix: String)` | `BOXQuery` | Name starts with prefix |
| `nameEndsWith(suffix: String)` | `BOXQuery` | Name ends with suffix |
| `nameEquals(name: String)` | `BOXQuery` | Exact name match |
| `nameMatches(pattern: String)` | `BOXQuery` | Regex pattern match |
| `extension(ext: String)` | `BOXQuery` | Filter by extension |
| `mimeType(mime: String)` | `BOXQuery` | Filter by MIME type |
| `minSize(size: UInt64)` | `BOXQuery` | Minimum size |
| `maxSize(size: UInt64)` | `BOXQuery` | Maximum size |
| `createdAfter(timestamp: Timestamp)` | `BOXQuery` | Created after date |
| `createdBefore(timestamp: Timestamp)` | `BOXQuery` | Created before date |
| `modifiedAfter(timestamp: Timestamp)` | `BOXQuery` | Modified after date |
| `modifiedBefore(timestamp: Timestamp)` | `BOXQuery` | Modified before date |
| `tag(tag: String)` | `BOXQuery` | Has tag |
| `metadata(key: String, value: String)` | `BOXQuery` | Metadata key=value |
| `inFolder(path: String)` | `BOXQuery` | Within folder |
| `recursive(enabled: Boolean)` | `BOXQuery` | Recursive search |
| `type(type: EntryType)` | `BOXQuery` | Filter by type |
| `encrypted()` | `BOXQuery` | Only encrypted entries |
| `compressed()` | `BOXQuery` | Only compressed entries |
| `build(): List<BOXEntry>` | `List<BOXEntry>` | Execute query |

---

# Section 37 — Encryption API

## 37.1 Purpose

Encrypting and decrypting containers and files.

## 37.2 Container-Level Encryption

```
// Encrypt entire container
BOXEncryption enc = BOXEncryption.fromPassword("myPassword")
container.encrypt(enc).value()

// Decrypt
container.decrypt(enc).value()
```

## 37.3 File-Level Encryption

```
BOXFile file = container.findFile("/secret.txt").value()
BOXEncryption fileEnc = BOXEncryption.fromPassword("filePassword")
file.setEncryption(fileEnc)

// Remove encryption
file.removeEncryption()
```

## 37.4 Creating Encryption Contexts

```
// From password
BOXEncryption enc = BOXEncryption.fromPassword("password", EncryptionAlgorithm.AES_256_GCM)

// From raw key
BOXEncryption enc = BOXEncryption.fromKey(keyBytes, EncryptionAlgorithm.CHACHA20_POLY1305)

// From key file
BOXEncryption enc = BOXEncryption.fromKeyFile("/path/to/keyfile")

// Generate random key
BOXEncryption enc = BOXEncryption.generateKey(EncryptionAlgorithm.AES_256_GCM)
Bytes key = enc.exportKey().value()
enc.saveKeyFile("/path/to/save-key").value()
```

## 37.5 Password Change

```
container.encryption().changePassword("oldPass", "newPass").value()
```

---

# Section 38 — Integrity API

## 38.1 Purpose

Verifying container integrity and authenticity.

## 38.2 Checksum Verification

```
// Verify all checksums
Result<Boolean> valid = container.verify()

// Verify single file
Boolean fileValid = file.verify().value()

// Verify single chunk
Boolean chunkValid = chunk.verify().value()
```

## 38.3 Digital Signatures

```
// Sign container
BOXSignature sig = container.sign(privateKeyBytes).value()

// Verify signature
Boolean valid = sig.verify(publicKeyBytes).value()

// Verify against multiple trusted keys
Boolean valid = sig.verify(trustedKeysList).value()
```

---

# Section 39 — Validation API

## 39.1 Purpose

Comprehensive container validation.

## 39.2 Running Validation

```
BOXValidator validator = container.validate().value()

if (validator.isValid) {
    // container is valid
} else {
    for (error in validator.errors) {
        log(error.message)
    }
}
```

## 39.3 Targeted Validation

```
Boolean headerValid = validator.validateHeader().value()
Boolean sectionsValid = validator.validateSections().value()
Boolean checksumsValid = validator.validateChecksums().value()
Boolean signaturesValid = validator.validateSignatures().value()
Boolean integrityValid = validator.validateIntegrity().value()
```

## 39.4 Validation Report

```
BOXValidationReport report = validator.report()
// Contains detailed error descriptions, affected offsets, repair suggestions
```

---

# Section 40 — Repair API

## 40.1 Purpose

Detecting and repairing corruption.

## 40.2 Scanning

```
BOXRepair repair = container.repair().value()
BOXCorruptionReport scan = repair.scan().value()

if (scan.corruptedChunks.size > 0) {
    log("Found ${scan.corruptedChunks.size} corrupted chunks")
}
```

## 40.3 Repairing

```
// Repair in place
Result<BOXRepairReport> result = repair.repair()

// Repair to new file (safer)
Result<BOXRepairReport> result = repair.repairTo("/repaired/output.box")
```

## 40.4 Repair Report

```
BOXRepairReport report = result.value()
log("Repaired ${report.repairCount} issues")
log("Recovered ${report.recoveredBytes} bytes")
```

---

# Section 41 — Statistics API

## 41.1 Purpose

Container statistics and analytics.

## 41.2 Getting Statistics

```
BOXStatistics stats = container.statistics

UInt32 files = stats.fileCount
UInt64 totalSize = stats.totalUncompressedSize
Float64 ratio = stats.compressionRatio
Int32 depth = stats.deepestFolder
```

## 41.3 Distribution Analysis

```
Map<String, UInt32> byExt = stats.entryTypeDistribution
Map<String, UInt32> byMime = stats.mimeTypeDistribution
Map<String, UInt64> bySize = stats.sizeDistribution

// Find largest/smallest
Optional<BOXFile> largest = stats.largestFile
Optional<BOXFile> smallest = stats.smallestFile
```

---

# Section 42 — Inspector API

## 42.1 Purpose

Binary-level inspection for debugging and diagnostics.

## 42.2 Inspecting

```
BOXInspector inspector = container.inspect().value()

// View header
HeaderInfo header = inspector.header()
log("Magic: ${header.magic}")
log("Version: ${header.formatVersion}")

// View sections
List<SectionInfo> sections = inspector.sections()
for (section in sections) {
    log("${section.type}: ${section.offset} (${section.size} bytes)")
}

// View chunks
List<ChunkInfo> chunks = inspector.chunks()
```

---

# Part IV — Builder Pattern

---

# Section 43 — Builder Flow

## 43.1 Standard Flow

```
BOX.create("output.box")
    .name("My Archive")
    .compression(ZSTD)
    .metadata("author", "John")
    .addFolder("/Documents")
    .addFile("/Documents/readme.md", readmeBytes)
    .addFile("/Documents/data.csv", csvBytes)
    .addFolder("/Images")
    .addFile("/Images/photo.jpg", photoBytes)
    .thumbnail(thumbnailBytes, IMAGE_FORMAT_JPEG)
    .build()
```

## 43.2 Flow Diagram

```
BOX.create(path)
    │
    ├── .name(name)
    ├── .metadata(key, value)
    ├── .thumbnail(image, format)
    ├── .compression(algorithm)
    ├── .compressionLevel(level)
    ├── .encryption(encryption)
    ├── .chunkSize(size)
    ├── .options(options)
    ├── .addFile(path, data)
    ├── .addFile(path, sourcePath)
    ├── .addFile(path, sourcePath, options)
    ├── .addFolder(path)
    ├── .addFiles(sourceDir)
    ├── .addFiles(sourceDir, filter)
    │
    └── .build() ──► Result<BOXContainer>
```

## 43.3 Builder Rules

1. All configuration methods MUST return `this` (the builder).
2. Configuration can be set in any order.
3. `build()` MUST be called exactly once.
4. After `build()`, the builder is consumed.
5. `build()` MUST NOT modify existing files.
6. If any error occurs during `build()`, the output file MUST be cleaned up.

## 43.4 Incremental Build

For very large containers, builders MAY support incremental mode:

```
BOXBuilder builder = BOX.create("large.box")
builder.addFile("/chunk1.bin", data1)
builder.flush()  // intermediate flush
builder.addFile("/chunk2.bin", data2)
builder.build()
```

---

# Section 44 — Fluent API Rules

## 44.1 Method Naming

- Configuration methods: noun or adjective (`name()`, `compressed()`, `encrypted()`)
- Action methods: verb (`addFile()`, `addFolder()`, `build()`)
- Query methods: verb (`find()`, `exists()`, `verify()`)

## 44.2 Parameter Rules

- Required parameters come first
- Optional parameters use builder methods
- Boolean parameters use positive naming (`recursive(true)` not `recursive(false)`)

## 44.3 Chaining Limits

- Maximum chain depth: 100 method calls
- Implementations SHOULD warn at depth > 50

---

# Part V — Streaming Deep Dive

---

# Section 45 — Stream Types

## 45.1 BOXReadStream

Read-only sequential or random access to file data.

Properties: `isOpen`, `position`, `length`, `isSeekable`, `isEof`

Methods: `read()`, `readAll()`, `readLine()`, `seek()`, `skip()`, `peek()`, `close()`, `iterator()`

## 45.2 BOXWriteStream

Write-only sequential or random access for file data.

Properties: `isOpen`, `position`, `isSeekable`

Methods: `write()`, `writeLine()`, `flush()`, `seek()`, `truncate()`, `close()`

## 45.3 BOXStream

Unified stream type that can be read-only, write-only, or bidirectional. Determined at open time.

---

# Section 46 — Stream Lifecycle

## 46.1 Lifecycle States

```
Created
    │
    ├── open() ──► Open
    │                │
    │                ├── read()/write() ──► Active
    │                │        │
    │                │        ├── close() ──► Closed
    │                │        │
    │                │        └── error ──► Error
    │                │
    │                └── close() ──► Closed
    │
    └── error ──► Error
```

## 46.2 State Transitions

| From | To | Trigger |
|------|-----|---------|
| Created | Open | `open()` |
| Created | Error | `open()` failure |
| Open | Active | First `read()` or `write()` |
| Open | Closed | `close()` |
| Active | Active | `read()` or `write()` |
| Active | Closed | `close()` |
| Active | Error | I/O error |
| Closed | (terminal) | — |
| Error | (terminal) | — |

## 46.3 Close Contract

1. `close()` MUST flush all buffers.
2. `close()` MUST release OS resources.
3. `close()` MUST be idempotent.
4. After `close()`, all operations MUST fail with `ERROR_STREAM_CLOSED`.
5. If stream was opened for writing, `close()` SHOULD auto-flush.

---

# Section 47 — Stream Protocols

## 47.1 Read Protocol

```
stream = file.openReadStream()
while (!stream.isEof):
    data = stream.read(chunkSize)
    process(data)
stream.close()
```

## 47.2 Write Protocol

```
stream = file.openWriteStream()
stream.write(data1)
stream.write(data2)
stream.flush()
stream.close()
```

## 47.3 Seek Protocol

```
stream = file.openReadStream()
stream.seek(1024)
data = stream.read(512)
stream.close()
```

## 47.4 Iterator Protocol

```
stream = file.openReadStream()
for chunk in stream:
    process(chunk)
stream.close()
```

## 47.5 Backpressure

When reading from slow sources or writing to slow destinations:

1. Streams SHOULD use internal buffering.
2. `read()` SHOULD block until data available or EOF.
3. `write()` SHOULD block until data written or error.
4. Implementations MAY support non-blocking mode via separate API.

---

# Part VI — Event System

---

# Section 48 — Event Architecture

## 48.1 Event Handler Interface

```
BOXEventHandler:
    handle(event: BOXEvent): void
```

## 48.2 BOXEvent Properties

| Property | Type | Description |
|----------|------|-------------|
| `name` | `String` | Event type name |
| `timestamp` | `Timestamp` | When event occurred |
| `source` | `Object` | Object that emitted the event |
| `data` | `Map<String, Any>` | Event-specific data |
| `isCancellable` | `Boolean` | Whether event can be cancelled |

## 48.3 Registering Handlers

```
// Global
BOX.setEventHandler(handler)

// Per-container
container.on("entryAdded", handler)

// Per-operation
options.onProgress(handler)
```

## 48.4 Event Dispatch

Events are dispatched synchronously by default. The operation waits for all handlers to complete before continuing.

Implementations MAY support asynchronous event dispatch via configuration.

---

# Section 49 — Event Types

## 49.1 Container Events

| Event Name | Data | Description |
|------------|------|-------------|
| `container.created` | path | New container created |
| `container.opened` | path, readOnly | Container opened |
| `container.closed` | path | Container closed |
| `container.saved` | path, size | Container saved |
| `container.validated` | isValid, errorCount | Validation complete |
| `container.repaired` | repairCount | Repair complete |
| `container.encrypted` | algorithm | Encryption applied |
| `container.decrypted` | — | Decryption complete |
| `container.signed` | algorithm | Container signed |
| `container.verified` | isValid | Signature verified |

## 49.2 Entry Events

| Event Name | Data | Description |
|------------|------|-------------|
| `entry.added` | entry | Entry added |
| `entry.removed` | entry | Entry removed |
| `entry.renamed` | entry, oldName | Entry renamed |
| `entry.moved` | entry, oldPath | Entry moved |
| `entry.copied` | entry, sourcePath | Entry copied |
| `entry.modified` | entry | Entry data modified |
| `entry.metadataChanged` | entry, key | Metadata updated |
| `entry.thumbnailSet` | entry | Thumbnail set |
| `entry.encrypted` | entry | Entry encrypted |
| `entry.decrypted` | entry | Entry decrypted |

## 49.3 Stream Events

| Event Name | Data | Description |
|------------|------|-------------|
| `stream.opened` | streamType, path | Stream opened |
| `stream.closed` | streamType, path | Stream closed |
| `stream.progress` | position, total | Read/write progress |
| `stream.completed` | streamType | Stream operation done |
| `stream.error` | error | Stream error occurred |

## 49.4 Search Events

| Event Name | Data | Description |
|------------|------|-------------|
| `search.started` | query | Search initiated |
| `search.progress` | matched, scanned | Search progress |
| `search.completed` | resultCount | Search complete |

## 49.5 Validation Events

| Event Name | Data | Description |
|------------|------|-------------|
| `validation.started` | — | Validation started |
| `validation.progress` | section, percentage | Validation progress |
| `validation.completed` | isValid, errorCount | Validation complete |

## 49.6 Repair Events

| Event Name | Data | Description |
|------------|------|-------------|
| `repair.started` | corruptionCount | Repair started |
| `repair.progress` | repaired, total | Repair progress |
| `repair.completed` | repairCount | Repair complete |
| `repair.failed` | error | Repair failed |

## 49.7 Encryption Events

| Event Name | Data | Description |
|------------|------|-------------|
| `encryption.started` | algorithm, target | Encryption started |
| `encryption.progress` | processed, total | Encryption progress |
| `encryption.completed` | — | Encryption complete |
| `decryption.started` | target | Decryption started |
| `decryption.progress` | processed, total | Decryption progress |
| `decryption.completed` | — | Decryption complete |

---

# Section 50 — Event Lifecycle

## 50.1 Event Emission Order

For a file addition:

```
1. entry.adding        (before file is written)
2. stream.opened       (write stream opened)
3. stream.progress     (during write, multiple)
4. stream.completed    (write finished)
5. stream.closed       (write stream closed)
6. entry.added         (after file is registered in index)
```

## 50.2 Cancellable Events

If `isCancellable` is true, handlers MAY call `event.cancel()`. The operation MUST be aborted and an appropriate error returned.

## 50.3 Error Events

Error events contain the error in `data["error"]`. If an error event handler does not suppress the error, it propagates to the caller.

---

# Part VII — Error System

---

# Section 51 — Error Architecture

## 51.1 Result Type

All operations that can fail return `Result<T>`:

```
Result<T>:
    isSuccess: Boolean
    isFailure: Boolean
    value: T           // throws if isFailure
    error: BOXError    // throws if isSuccess
    getOrNull(): T?    // null if failure
    getOrDefault(default: T): T
    map(function): Result<U>
    flatMap(function): Result<U>
    recover(function): T
}
```

## 51.2 Error Propagation

Errors propagate upward unless explicitly caught:

```
Result<BOXContainer> result = BOX.open("missing.box")
if (result.isFailure) {
    BOXError error = result.error
    // handle error
}
```

---

# Section 52 — Error Hierarchy

## 52.1 BOXError Base

| Property | Type | Description |
|----------|------|-------------|
| `code` | `ErrorCode` | Error code enum |
| `message` | `String` | Developer-facing message |
| `userMessage` | `String` | User-facing message |
| `severity` | `ErrorSeverity` | Error severity |
| `recoverable` | `Boolean` | Whether operation can be retried |
| `source` | `String` | Component that generated the error |
| `cause` | `Optional<BOXError>` | Underlying cause |
| `context` | `Map<String, String>` | Additional context |
| `timestamp` | `Timestamp` | When error occurred |

## 52.2 ErrorSeverity Enum

| Value | Description |
|-------|-------------|
| `INFO` | Informational, not an error |
| `WARNING` | Non-critical issue |
| `ERROR` | Operation failed |
| `CRITICAL` | System-level failure |

## 52.3 Error Subtypes

```
BOXError
├── BOXValidationError
│   ├── HeaderValidationError
│   ├── SectionValidationError
│   ├── ChecksumValidationError
│   └── SignatureValidationError
├──BOXCorruptionError
│   ├── ChunkCorruptionError
│   ├── SectionCorruptionError
│   └── IndexCorruptionError
├── BOXEncryptionError
│   ├── InvalidKeyError
│   ├── DecryptionFailedError
│   ├── AuthenticationError
│   └── KeyDerivationError
├── BOXStreamingError
│   ├── StreamClosedError
│   ├── SeekError
│   ├── BufferOverflowError
│   └── TimeoutError
├── BOXVersionError
│   ├── UnsupportedVersionError
│   ├── DeprecatedFeatureError
│   └── ForwardCompatibilityError
├── BOXIOError
│   ├── FileNotFoundError
│   ├── PermissionDeniedError
│   ├── DiskFullError
│   └── IOError
└── BOXIntegrityError
    ├── ChecksumMismatchError
    ├── SignatureInvalidError
    └── TamperingDetectedError
```

---

# Section 53 — Error Codes

## 53.1 General Errors

| Code | Name | Severity | Recoverable | User Message |
|------|------|----------|-------------|--------------|
| 1000 | ERROR_UNKNOWN | CRITICAL | false | An unknown error occurred |
| 1001 | ERROR_NOT_IMPLEMENTED | ERROR | false | This feature is not implemented |
| 1002 | ERROR_INVALID_ARGUMENT | ERROR | false | Invalid argument provided |
| 1003 | ERROR_INVALID_STATE | ERROR | false | Operation not valid in current state |
| 1004 | ERROR_OUT_OF_MEMORY | CRITICAL | true | Insufficient memory |
| 1005 | ERROR_CANCELLED | INFO | true | Operation was cancelled |

## 53.2 File/IO Errors

| Code | Name | Severity | Recoverable | User Message |
|------|------|----------|-------------|--------------|
| 2000 | ERROR_FILE_NOT_FOUND | ERROR | false | File not found |
| 2001 | ERROR_PERMISSION_DENIED | ERROR | false | Permission denied |
| 2002 | ERROR_DISK_FULL | ERROR | true | Disk is full |
| 2003 | ERROR_FILE_EXISTS | ERROR | false | File already exists |
| 2004 | ERROR_PATH_TOO_LONG | ERROR | false | File path too long |
| 2005 | ERROR_IO_FAILED | ERROR | true | I/O operation failed |
| 2006 | ERROR_TOO_MANY_OPEN_FILES | ERROR | true | Too many open files |

## 53.3 Format Errors

| Code | Name | Severity | Recoverable | User Message |
|------|------|----------|-------------|--------------|
| 3000 | ERROR_INVALID_MAGIC | ERROR | false | Not a valid BOX file |
| 3001 | ERROR_UNSUPPORTED_VERSION | ERROR | false | Unsupported format version |
| 3002 | ERROR_INVALID_HEADER | ERROR | false | Corrupted header |
| 3003 | ERROR_INVALID_SECTION | ERROR | true | Corrupted section |
| 3004 | ERROR_INVALID_CHUNK | ERROR | true | Corrupted chunk |
| 3005 | ERROR_INVALID_INDEX | ERROR | true | Corrupted index |
| 3006 | ERROR_INVALID_CHECKSUM | WARNING | true | Checksum mismatch |
| 3007 | ERROR_INVALID_SIGNATURE | ERROR | false | Invalid signature |

## 53.4 Operation Errors

| Code | Name | Severity | Recoverable | User Message |
|------|------|----------|-------------|--------------|
| 4000 | ERROR_ENTRY_NOT_FOUND | ERROR | false | Entry not found |
| 4001 | ERROR_ENTRY_EXISTS | ERROR | false | Entry already exists |
| 4002 | ERROR_ENTRY_IS_DIRECTORY | ERROR | false | Expected file, got folder |
| 4003 | ERROR_ENTRY_IS_FILE | ERROR | false | Expected folder, got file |
| 4004 | ERROR_CONTAINER_FULL | ERROR | false | Container cannot accept more entries |
| 4005 | ERROR_CONTAINER_CLOSED | ERROR | false | Container is closed |
| 4006 | ERROR_READ_ONLY | ERROR | false | Container is read-only |
| 4007 | ERROR_STREAM_CLOSED | ERROR | false | Stream is closed |
| 4008 | ERROR_SEEK_FAILED | ERROR | true | Seek operation failed |

## 53.5 Encryption Errors

| Code | Name | Severity | Recoverable | User Message |
|------|------|----------|-------------|--------------|
| 5000 | ERROR_INVALID_KEY | ERROR | false | Invalid encryption key |
| 5001 | ERROR_DECRYPTION_FAILED | ERROR | false | Decryption failed |
| 5002 | ERROR_AUTHENTICATION_FAILED | ERROR | false | Authentication tag mismatch |
| 5003 | ERROR_KEY_DERIVATION_FAILED | ERROR | false | Key derivation failed |
| 5004 | ERROR_UNSUPPORTED_ALGORITHM | ERROR | false | Unsupported encryption algorithm |

## 53.6 Integrity Errors

| Code | Name | Severity | Recoverable | User Message |
|------|------|----------|-------------|--------------|
| 6000 | ERROR_CHECKSUM_MISMATCH | WARNING | true | Data integrity check failed |
| 6001 | ERROR_SIGNATURE_INVALID | ERROR | false | Digital signature invalid |
| 6002 | ERROR_TAMPERING_DETECTED | CRITICAL | false | Data tampering detected |

---

# Section 54 — Error Recovery

## 54.1 Recovery Strategy

Each error specifies `recoverable: Boolean`. If true, the caller MAY retry the operation. If false, retrying is futile.

## 54.2 Retry Guidelines

| Error Type | Recommendation |
|------------|----------------|
| IO Errors | Retry with backoff |
| Checksum Errors | Retry read, try next chunk |
| Stream Errors | Reopen stream |
| Encryption Errors | Check key, do not retry |
| Format Errors | Do not retry |
| Memory Errors | Reduce buffer size, retry |

## 54.3 Error Context

The `context` map provides diagnostic information:

```
{
    "path": "/Documents/large.bin",
    "chunkId": "42",
    "offset": "1048576",
    "expectedChecksum": "abc123...",
    "actualChecksum": "def456..."
}
```

---

# Part VIII — Thread Safety

---

# Section 55 — Concurrency Model

## 55.1 Threading Model

The SDK does NOT mandate a specific threading model. Implementations MUST document their threading behavior.

## 55.2 Object-Level Thread Safety

| Object | Thread Safe | Notes |
|--------|-------------|-------|
| `BOX` (static) | Yes | All static methods are thread-safe |
| `BOXContainer` | No | External synchronization required |
| `BOXEntry` | Yes | Immutable after creation |
| `BOXFile` | No | External synchronization required |
| `BOXFolder` | No | External synchronization required |
| `BOXMetadata` | No | External synchronization required |
| `BOXStream` | No | One thread at a time |
| `BOXBuilder` | No | Single-threaded construction |
| `BOXSession` | No | Single-threaded session |
| `BOXConfiguration` | Yes | Immutable once created |
| `BOXOptions` | No | Mutable per-operation |
| `BOXQuery` | No | Single-threaded construction |
| `BOXValidator` | Yes | Read-only after creation |
| `BOXRepair` | No | Exclusive access required |
| `BOXStatistics` | Yes | Read-only after creation |
| `BOXSearchResult` | Yes | Immutable |
| `BOXPath` | Yes | Immutable |
| `BOXChunk` | Yes | Read-only |
| `BOXIndex` | No | External synchronization |
| `BOXSignature` | Yes | Read-only |
| `BOXEncryption` | No | Key material must be protected |

## 55.3 Container Locking

Implementations SHOULD provide an optional file-level lock:

```
container = BOX.open("file.box", options.withLock(LOCK_SHARED))
// or
container = BOX.open("file.box", options.withLock(LOCK_EXCLUSIVE))
```

## 55.4 Concurrent Read

Multiple threads MAY read from a container concurrently if no writes are occurring.

## 55.5 Concurrent Write

Writes to a container MUST be serialized. Implementations SHOULD use internal locking.

## 55.6 Read-Write Exclusion

Reads and writes MUST NOT occur concurrently on the same container without explicit synchronization.

---

# Section 56 — Thread Safety Guarantees

## 56.1 Static Methods

All `BOX` static methods are thread-safe. Multiple threads MAY call `BOX.open()`, `BOX.validate()`, etc. concurrently.

## 56.2 Entry Immutability

`BOXEntry` objects are immutable after creation. They can be shared between threads without synchronization.

## 56.3 Stream Exclusivity

A stream MUST NOT be used from multiple threads simultaneously. Implementations MUST detect and reject concurrent access.

## 56.4 Session Exclusivity

A session MUST be used from a single thread. Implementations MUST detect and reject multi-threaded session use.

---

# Part IX — Performance

---

# Section 57 — Performance Model

## 57.1 Performance Characteristics

| Operation | Expected Complexity | Notes |
|-----------|-------------------|-------|
| Open container | O(1) | Header parse only |
| List directory | O(n) | n = children in directory |
| Find by path | O(log n) | Binary search in index |
| Find by ID | O(1) | Direct array lookup |
| Read file | O(1) + O(chunks) | Seek + chunk reads |
| Write file | O(1) + O(chunks) | Index update + chunk writes |
| Search | O(n) | n = total entries |
| Validate | O(n + chunks) | Full scan |
| Repair | O(n + chunks) | Full scan + writes |

## 57.2 Memory Budget

Implementations SHOULD operate within these memory budgets:

| Operation | Max Memory | Notes |
|-----------|-----------|-------|
| Open | 1 MB | Header + index |
| List | 1 MB | Directory cache |
| Read small file | File size | Full file in memory |
| Read large file | Chunk size | Streaming |
| Search | 1 MB | Index scan |
| Validate | 1 MB | Per-section validation |

## 57.3 I/O Optimization

Implementations SHOULD:

1. Use buffered I/O for all file operations.
2. Align reads to 4096-byte boundaries.
3. Minimize seeks during sequential operations.
4. Prefetch next chunk during streaming.
5. Batch metadata updates.

---

# Section 58 — Lazy Loading

## 58.1 Lazy Entry Loading

Entries SHOULD be loaded lazily:

1. On container open, load only the Header and File Index.
2. Load chunk tables on demand (when file data is accessed).
3. Load folder trees on demand (when folder is browsed).
4. Load metadata on demand (when metadata is accessed).

## 58.2 Lazy Properties

Entry properties that require I/O SHOULD be computed lazily:

- `checksum`: computed on first access
- `thumbnail`: loaded on first access
- `metadata`: loaded on first access

## 58.3 Eager Loading Option

For bulk operations, implementations SHOULD support eager loading:

```
container.eagerLoad(loadingOptions)
```

---

# Section 59 — Memory Management

## 59.1 Buffer Reuse

Implementations SHOULD support buffer reuse for streaming:

```
Bytes buffer = allocate(65536)
while (!stream.isEof) {
    Int32 bytesRead = stream.read(buffer).value()
    process(buffer, bytesRead)
}
```

## 59.2 Memory-Mapped I/O

For read-heavy workloads, implementations MAY use memory-mapped I/O:

```
container = BOX.open("large.box", options.withMemoryMapping(true))
```

## 59.3 Streaming Memory

Streaming operations MUST NOT load the entire file into memory. Implementations MUST enforce a maximum buffer size (configurable via `BOXConfiguration.readBufferSize`).

---

# Section 60 — Large File Handling

## 60.1 64-bit Support

All size and offset fields use 64-bit integers. The format supports files up to 2^63 bytes (approximately 9.2 exabytes).

## 60.2 Chunked Processing

Large files MUST be processed in chunks. Implementations MUST NOT attempt to load entire large files into memory.

## 60.3 Progress Reporting

Long-running operations on large files SHOULD report progress:

```
BOXOptions opts = BOXOptions.default()
    .onProgress((current, total, message) -> {
        log("${current * 100 / total}% - ${message}")
    })
```

## 60.4 Incremental Checksum

For files larger than 1 GB, implementations SHOULD compute checksums incrementally (chunk-by-chunk) rather than loading the entire file.

---

# Part X — Cross-Platform

---

# Section 61 — Platform Independence

## 61.1 Language Neutrality

The SDK specification is language-neutral. Implementations MUST NOT depend on:

- Platform-specific classes (e.g., `java.io.File`)
- Platform-specific APIs (e.g., Windows API, POSIX API)
- Platform-specific types (e.g., `int` size assumptions)

## 61.2 Type Mapping

Implementations MUST map SDK types to native types:

| SDK Type | Java | Kotlin | Python | JavaScript | Rust | Go | C++ |
|----------|------|--------|--------|------------|------|-----|-----|
| String | String | String | str | string | String | string | std::string |
| Bytes | byte[] | ByteArray | bytes | Uint8Array | Vec<u8> | []byte | std::vector<uint8_t> |
| Int32 | int | Int | int | number | i32 | int32 | int32_t |
| UInt64 | long | Long | int | bigint | u64 | uint64 | uint64_t |
| Boolean | boolean | Boolean | bool | boolean | bool | bool | bool |
| List<T> | List<T> | List<T> | list | Array<T> | Vec<T> | []T | std::vector<T> |
| Map<K,V> | Map<K,V> | Map<K,V> | dict | Map<K,V> | HashMap<K,V> | map[K]V | std::map<K,V> |
| Optional<T> | Optional<T> | T? | Optional[T] | T \| null | Option<T> | (*T, bool) | std::optional<T> |
| Result<T> | Result<T> | Result<T> | Result[T] | Result<T,E> | Result<T,E> | (T, error) | std::expected<T,E> |
| Timestamp | long | Long | int | number | u64 | int64 | int64_t |

## 61.3 Error Handling Mapping

| SDK Pattern | Java | Kotlin | Python | JavaScript | Rust | Go | C++ |
|-------------|------|--------|--------|------------|------|-----|-----|
| Result<T> | Result<T> | Result<T> | Result[T] | throw / Result | Result<T,E> | (T, error) | std::expected |
| Exception | Exception | Exception | Exception | Error | — | error | std::exception |

## 61.4 Resource Management Mapping

| SDK Pattern | Java | Kotlin | Python | JavaScript | Rust | Go | C++ |
|-------------|------|--------|--------|------------|------|-----|-----|
| Closeable | Closeable | Closeable | __enter__/__exit__ | Disposable | Drop | io.Closer | RAII |
| try-with-resources | try-with | use | with | await using | drop scope | defer | scoped_ptr |

## 61.5 Async Support

Implementations MAY provide async versions of blocking operations:

| Operation | Sync Signature | Async Signature |
|-----------|---------------|-----------------|
| open | `open(path): Result<BOXContainer>` | `openAsync(path): Future<Result<BOXContainer>>` |
| save | `save(): Result<void>` | `saveAsync(): Future<Result<void>>` |
| read | `read(): Result<Bytes>` | `readAsync(): Future<Result<Bytes>>` |
| write | `write(data): Result<void>` | `writeAsync(data): Future<Result<void>>` |
| extract | `extract(path, dest): Result<void>` | `extractAsync(path, dest): Future<Result<void>>` |

---

# Section 62 — Language Mapping Rules

## 62.1 Naming Conventions

| Language | Types | Methods | Properties | Constants |
|----------|-------|---------|------------|-----------|
| Java | PascalCase | camelCase | camelCase | SCREAMING_SNAKE |
| Kotlin | PascalCase | camelCase | camelCase | SCREAMING_SNAKE |
| Python | PascalCase | snake_case | snake_case | SCREAMING_SNAKE |
| JavaScript | PascalCase | camelCase | camelCase | SCREAMING_SNAKE |
| Rust | PascalCase | snake_case | snake_case | SCREAMING_SNAKE |
| Go | PascalCase | PascalCase | PascalCase | SCREAMING_SNAKE |
| C++ | PascalCase | camelCase | camelCase | SCREAMING_SNAKE |
| Swift | PascalCase | camelCase | camelCase | camelCase |
| C# | PascalCase | PascalCase | PascalCase | PascalCase |

## 62.2 Idiomatic Adaptations

Implementations SHOULD adapt to host language idioms:

1. **Java**: Use `Optional<T>` for nullable returns, `Stream<T>` for iterators
2. **Kotlin**: Use nullable types `T?` instead of `Optional<T>`, coroutines for async
3. **Python**: Use generators for iterators, `with` for resource management
4. **JavaScript**: Use `Promise<T>` for async, `Symbol.iterator` for iteration
5. **Rust**: Use `Result<T, BOXError>` for errors, `Iterator` trait for iteration
6. **Go**: Use `(T, error)` pattern, channels for streaming
7. **C++**: Use `std::expected<T,E>` (C++23) or `std::variant`, ranges for iteration
8. **Swift**: Use `throws`, `AsyncSequence` for async iteration
9. **C#**: Use `Task<T>` for async, `IAsyncEnumerable<T>` for async iteration

---

# Section 63 — Minimum Implementation

## 63.1 Required for Level 1 (Basic)

- `BOX.create()`
- `BOX.open()`
- `BOXContainer.close()`
- `BOXContainer.save()`
- `BOXContainer.entries()`
- `BOXContainer.find()`
- `BOXContainer.addFile()`
- `BOXContainer.addFolder()`
- `BOXContainer.remove()`
- `BOXFile.read()`
- `BOXFile.write()`
- `BOXFolder.children()`
- `BOXPath.of()`

## 63.2 Required for Level 2 (Standard)

All Level 1, plus:

- `BOXMetadata` (all methods)
- `BOXBuilder` (all methods)
- `BOXContainer.search()`
- `BOXContainer.extract()`
- `BOXContainer.rename()`
- `BOXContainer.move()`
- `BOXContainer.copy()`
- `BOXFile.openReadStream()`
- `BOXFile.openWriteStream()`
- `BOXReadStream` (all methods)
- `BOXWriteStream` (all methods)
- `BOXFolder.walk()`
- `BOXFolder.addFile()`
- `BOXFolder.addFolder()`

## 63.3 Required for Level 3 (Advanced)

All Level 2, plus:

- `BOXEncryption` (all methods)
- `BOXSignature` (all methods)
- `BOXValidator` (all methods)
- `BOXRepair` (all methods)
- `BOXChunk` (all methods)
- `BOXStatistics` (all properties)
- `BOXQuery` (all methods)
- `BOXOptions` (all properties)
- `BOXSession` (all methods)
- Event system (all events)

## 63.4 Required for Level 4 (Full)

All Level 3, plus:

- `BOXInspector` (all methods)
- `BOXIndex` (all methods)
- `BOXConfiguration` (all properties)
- Async API variants
- Memory-mapped I/O support
- Advanced streaming (backpressure, non-blocking)

---

# Part XI — Documentation

---

# Section 64 — Object Relationship Diagram

```
                            ┌──────────────┐
                            │     BOX      │
                            │  (static)    │
                            └──────┬───────┘
                                   │ creates
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
            ┌──────────┐  ┌──────────────┐  ┌──────────┐
            │BOXBuilder│  │BOXContainer  │  │BOXSession│
            └────┬─────┘  └──────┬───────┘  └────┬─────┘
                 │               │                │
                 └──────build()──┘                │
                         │                        │
                         ▼                        │
                 ┌──────────────┐                 │
                 │BOXContainer  │◄────────────────┘
                 └──────┬───────┘
                        │ contains
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │ BOXEntry │  │BOXMetadata│  │BOXIndex  │
    │ (abstract)│  └──────────┘  └──────────┘
    └────┬─────┘
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌────────┐
│BOXFile │ │BOXFolder│
└────┬───┘ └────────┘
     │ contains
     ▼
┌──────────┐
│ BOXChunk │
└──────────┘
```

## 64.1 Dependency Graph

```
BOXContainer ──depends-on──► BOXEntry, BOXMetadata, BOXIndex, BOXChunk
BOXFile ──depends-on──► BOXChunk, BOXReadStream, BOXWriteStream
BOXFolder ──depends-on──► BOXEntry
BOXBuilder ──depends-on──► BOXContainer, BOXFile, BOXFolder
BOXSession ──depends-on──► BOXContainer
BOXValidator ──depends-on──► BOXContainer
BOXRepair ──depends-on──► BOXContainer
BOXQuery ──depends-on──► BOXEntry, BOXSearchResult
BOXStream ──depends-on──► BOXFile
BOXEncryption ──depends-on──► (standalone)
BOXSignature ──depends-on──► (standalone)
BOXConfiguration ──depends-on──► (standalone)
BOXOptions ──depends-on──► BOXEncryption, BOXEntryFilter
```

---

# Section 65 — Lifecycle Diagrams

## 65.1 Container Lifecycle

```
                ┌─────────┐
                │ Created │
                └────┬────┘
                     │
          ┌──────────┼──────────┐
          ▼          ▼          ▼
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │  Build   │ │   Open   │ │  Repair  │
    │  (new)   │ │ (exist)  │ │ (broken) │
    └────┬─────┘ └────┬─────┘ └────┬─────┘
         │            │            │
         └────────────┼────────────┘
                      ▼
              ┌──────────────┐
              │    Open      │
              └──────┬───────┘
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    ▼                ▼                ▼
┌────────┐    ┌──────────┐    ┌──────────┐
│  Read  │    │  Modify  │    │  Search  │
└────┬───┘    └────┬─────┘    └────┬─────┘
     │             │               │
     │             ▼               │
     │     ┌──────────────┐        │
     │     │     Save     │        │
     │     └──────┬───────┘        │
     │            │                │
     └────────────┼────────────────┘
                  ▼
          ┌──────────────┐
          │    Close     │
          └──────────────┘
```

## 65.2 File Lifecycle

```
    ┌──────────┐
    │ Created  │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │  Ready   │
    └────┬─────┘
         │
    ┌────┼────┬────────┐
    ▼    ▼    ▼        ▼
┌──────┐┌──────┐┌──────┐┌──────┐
│ Read ││Write ││Stream││Delete│
└──┬───┘└──┬───┘└──┬───┘└──┬───┘
   │       │       │       │
   │       ▼       │       │
   │  ┌──────────┐ │       │
   │  │ Modified │ │       │
   │  └────┬─────┘ │       │
   │       │       │       │
   └───────┼───────┼───────┘
           ▼       ▼
    ┌──────────────────┐
    │   Flush/Close    │
    └──────────────────┘
```

## 65.3 Stream Lifecycle

```
    ┌──────────┐
    │ Created  │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │   Open   │
    └────┬─────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐┌────────┐
│  Read  ││ Write  │
│ (loop) ││ (loop) │
└────┬───┘└────┬───┘
     │         │
     └────┬────┘
          ▼
    ┌──────────┐
    │ Flushing │
    └────┬─────┘
         │
         ▼
    ┌──────────┐
    │  Closed  │
    └──────────┘
```

---

# Section 66 — Sequence Diagrams

## 66.1 Create and Add Files

```
User          BOX             BOXBuilder        BOXContainer
 │              │                │                  │
 │──create()──►│                │                  │
 │              │──new()───────►│                  │
 │◄─builder────│                │                  │
 │──addFile()──────────────────►│                  │
 │              │                │──buffer data──►│
 │──addFolder()──────────────────►│                │
 │              │                │──create entry──►│
 │──build()──────────────────────►│                │
 │              │                │──write all────►│
 │              │                │──finalize─────►│
 │◄─result─────────────────────────────────────────│
```

## 66.2 Open and Read

```
User          BOX             BOXContainer       BOXFile        BOXStream
 │              │                │                 │               │
 │──open()─────►│                │                 │               │
 │              │──read header──►│                 │               │
 │              │──read index───►│                 │               │
 │◄─container────────────────────│                 │               │
 │──find()───────────────────────►│                │               │
 │              │                │──lookup───────►│               │
 │◄─entry────────────────────────│                 │               │
 │──openReadStream()──────────────────────────────►│              │
 │              │                │                 │──open───────►│
 │◄─stream───────────────────────────────────────────────────────│
 │──read()───────────────────────────────────────────────────────►│
 │              │                │                 │──read chunk─►│
 │◄─data─────────────────────────────────────────────────────────│
 │──close()──────────────────────────────────────────────────────►│
```

## 66.3 Search

```
User          BOX             BOXContainer       BOXIndex       BOXQuery
 │              │                │                 │               │
 │──query()────►│                │                 │               │
 │◄─builder───────────────────────────────────────────────────────│
 │──nameContains()───────────────────────────────────────────────►│
 │──extension()──────────────────────────────────────────────────►│
 │──build()──────────────────────────────────────────────────────►│
 │              │                │                 │◄─criteria────│
 │              │                │──scan index────►│               │
 │              │                │◄─matches────────│               │
 │◄─results─────────────────────│                 │               │
```

## 66.4 Encrypt

```
User          BOX             BOXContainer       BOXEncryption
 │              │                │                 │
 │──open()─────►│                │                 │
 │◄─container────────────────────│                 │
 │──encrypt()───────────────────►│                 │
 │              │                │──deriveKey()───►│
 │              │                │                 │──generateIV─►│
 │              │                │──encryptChunks─►│              │
 │              │                │──updateHeader──►│              │
 │◄─success─────────────────────│                 │
```

---

# Section 67 — Usage Examples

## 67.1 Create Simple Container

```
Result<BOXContainer> result = BOX.create("archive.box")
    .name("My Archive")
    .addFile("/readme.txt", "Hello BOX!".toBytes())
    .build()

if (result.isSuccess) {
    BOXContainer box = result.value()
    box.close()
}
```

## 67.2 Create from Directory

```
Result<BOXContainer> result = BOX.create("project.box")
    .name("Project Backup")
    .compression(CompressionAlgorithm.ZSTD)
    .addFiles("/home/user/project")
    .metadata("author", "Developer")
    .metadata("created", "2026-06-29")
    .build()
```

## 67.3 Open and Browse

```
Result<BOXContainer> result = BOX.open("archive.box")
if (result.isFailure) {
    log(result.error.userMessage)
    return
}

BOXContainer box = result.value()

// List root
for (entry in box.entries()) {
    log("${entry.name} (${entry.size} bytes)")
}

// Navigate folders
BOXFolder docs = box.findFolder("/Documents").value()
for (file in docs.files()) {
    log(file.name)
}

box.close()
```

## 67.4 Read File

```
BOXContainer box = BOX.open("archive.box").value()
BOXFile file = box.findFile("/data/report.pdf").value()

// Read all
Bytes data = file.read().value()

// Read partial
Bytes header = file.read(0, 1024).value()

// Stream
BOXReadStream stream = file.openReadStream().value()
while (!stream.isEof) {
    Bytes block = stream.read(65536).value()
    processBlock(block)
}
stream.close()

box.close()
```

## 67.5 Modify Container

```
BOXContainer box = BOX.open("archive.box").value()

// Add file
box.addFile("/new-file.txt", contentBytes).value()

// Remove file
box.remove("/old-file.txt").value()

// Rename
box.rename("/temp.txt", "permanent.txt").value()

// Move
box.move("/file.txt", "/archive/file.txt").value()

// Save
box.save().value()

box.close()
```

## 67.6 Search

```
BOXContainer box = BOX.open("archive.box").value()

// Simple search
List<BOXSearchResult> results = box.search("report")

// Advanced query
List<BOXEntry> pdfs = box.query()
    .extension("pdf")
    .minSize(1024)
    .tag("important")
    .build()

// Find by name
Optional<BOXEntry> entry = box.index().findByName("config.json")

box.close()
```

## 67.7 Encrypt

```
BOXContainer box = BOX.open("archive.box").value()

BOXEncryption enc = BOXEncryption.fromPassword("mySecurePassword")
box.encrypt(enc).value()

box.save().value()
box.close()

// Later...
BOXEncryption enc = BOXEncryption.fromPassword("mySecurePassword")
BOXContainer box = BOX.open("archive.box").value()
box.decrypt(enc).value()
```

## 67.8 Validate and Repair

```
// Validate
BOXValidator validator = BOX.validate("archive.box").value()
if (!validator.isValid) {
    for (error in validator.errors) {
        log("ERROR: ${error.message}")
    }
    
    // Attempt repair
    BOXRepair repair = BOX.repair("archive.box").value()
    BOXCorruptionReport scan = repair.scan().value()
    
    if (scan.recoverable) {
        repair.repairTo("repaired.box").value()
    }
}
```

## 67.9 Session with Transactions

```
BOXSession session = BOX.openSession("archive.box").value()

session.beginTransaction()

session.container.addFile("/file1.txt", data1).value()
session.container.addFile("/file2.txt", data2).value()
session.container.remove("/old.txt").value()

session.commit()
session.close()
```

---

# Section 68 — Best Practices

## 68.1 Resource Management

1. Always close containers when done.
2. Use language-specific resource management (try-with, with, RAII).
3. Close streams explicitly or use scoped blocks.

## 68.2 Error Handling

1. Always check `Result` before using the value.
2. Log error details for debugging.
3. Present `userMessage` to end users, not `message`.
4. Use error codes for programmatic handling.

## 68.3 Performance

1. Use streaming for files > 10 MB.
2. Use chunk-aware reading for large files.
3. Avoid reading entire large files into memory.
4. Use eager loading only when processing all entries.
5. Configure chunk size based on access patterns.

## 68.4 Security

1. Never log encryption keys or passwords.
2. Use `BOXEncryption.fromKey()` for production systems.
3. Verify signatures before trusting signed containers.
4. Use strong passwords with PBKDF2 or Argon2.

## 68.5 Thread Safety

1. Use one container per thread, or synchronize access.
2. Share only immutable objects (`BOXEntry`, `BOXPath`) between threads.
3. Create separate stream instances per thread.

## 68.6 Metadata

1. Set author, tool, and version metadata on creation.
2. Use consistent tag naming conventions.
3. Store timestamps in ISO 8601 format.

---

# Section 69 — Migration Strategy

## 69.1 From ZIP

1. ZIP entries map 1:1 to BOX entries.
2. ZIP paths use `/` (same as BOX).
3. ZIP metadata is limited; BOX metadata is richer.
4. ZIP streaming is not supported; BOX supports streaming.

## 69.2 From TAR

1. TAR entries map to BOX entries.
2. TAR has no folder concept; BOX has native folders.
3. TAR has no metadata; BOX has rich metadata.
4. TAR has no random access; BOX has O(1) lookup.

## 69.3 From 7Z

1. 7Z entries map to BOX entries.
2. 7Z has no folder tree; BOX has native folders.
3. 7Z has no streaming; BOX supports streaming.
4. 7Z has limited metadata; BOX has rich metadata.

## 69.4 From Custom Formats

1. Map custom fields to BOX metadata tags.
2. Preserve file/folder structure.
3. Convert checksums to BOX checksum format.
4. Preserve encryption settings where possible.

---

# Part XII — Appendices

---

# Appendix A — Complete Type Reference

## A.1 All Types

| Type | Category | Description |
|------|----------|-------------|
| `BOX` | Entry point | Static API entry |
| `BOXContainer` | Core | Container representation |
| `BOXEntry` | Core | Abstract entry base |
| `BOXFile` | Core | File entry |
| `BOXFolder` | Core | Folder entry |
| `BOXPath` | Core | Path abstraction |
| `BOXMetadata` | Metadata | Key-value store |
| `BOXChunk` | Storage | Data chunk reference |
| `BOXIndex` | Storage | Entry lookup index |
| `BOXStream` | Streaming | Data stream |
| `BOXReadStream` | Streaming | Read-only stream |
| `BOXWriteStream` | Streaming | Write-only stream |
| `BOXEncryption` | Security | Encryption context |
| `BOXSignature` | Security | Digital signature |
| `BOXBuilder` | Construction | Fluent builder |
| `BOXSession` | Session | Active session |
| `BOXConfiguration` | Config | Global configuration |
| `BOXOptions` | Config | Per-operation options |
| `BOXQuery` | Search | Query builder |
| `BOXSearchResult` | Search | Search result |
| `BOXValidator` | Validation | Validation engine |
| `BOXRepair` | Repair | Repair engine |
| `BOXInspector` | Inspection | Binary inspector |
| `BOXStatistics` | Analytics | Container stats |
| `BOXEvent` | Event | Event object |
| `BOXEventHandler` | Event | Event handler interface |
| `BOXEntryFilter` | Filter | Entry filter interface |
| `BOXProgressHandler` | Callback | Progress callback |
| `BOXCancelToken` | Control | Cancellation signal |
| `BOXValidationReport` | Report | Validation report |
| `BOXValidationValidationError` | Error | Validation error |
| `BOXValidationWarning` | Error | Validation warning |
| `BOXCorruptionReport` | Report | Corruption report |
| `BOXRepairReport` | Report | Repair report |
| `BOXError` | Error | Base error |
| `BOXValidationError` | Error | Validation error |
| `BOXCorruptionError` | Error | Corruption error |
| `BOXEncryptionError` | Error | Encryption error |
| `BOXStreamingError` | Error | Streaming error |
| `BOXVersionError` | Error | Version error |
| `BOXIOError` | Error | I/O error |
| `BOXIntegrityError` | Error | Integrity error |
| `EntryType` | Enum | FILE, FOLDER, SYMLINK |
| `EncryptionAlgorithm` | Enum | NONE, AES_256_GCM, etc. |
| `SignatureAlgorithm` | Enum | ED25519, ECDSA_P256, etc. |
| `CompressionAlgorithm` | Enum | NONE, LZ4, ZSTD, etc. |
| `ChecksumAlgorithm` | Enum | NONE, CRC32, SHA256, etc. |
| `ImageFormat` | Enum | JPEG, PNG, WEBP |
| `ErrorSeverity` | Enum | INFO, WARNING, ERROR, CRITICAL |
| `ErrorCode` | Enum | All error codes |
| `Result<T>` | Generic | Success or error |
| `List<T>` | Generic | Ordered collection |
| `Map<K,V>` | Generic | Key-value mapping |
| `Optional<T>` | Generic | Nullable value |
| `Iterator<T>` | Generic | Sequential access |
| `Pair<A,B>` | Generic | Tuple |

## A.2 Complete Method Index

(All methods listed in their respective sections above.)

---

# Appendix B — Error Code Registry

## B.1 Complete Error Code List

| Code | Name | Category |
|------|------|----------|
| 1000 | ERROR_UNKNOWN | General |
| 1001 | ERROR_NOT_IMPLEMENTED | General |
| 1002 | ERROR_INVALID_ARGUMENT | General |
| 1003 | ERROR_INVALID_STATE | General |
| 1004 | ERROR_OUT_OF_MEMORY | General |
| 1005 | ERROR_CANCELLED | General |
| 2000 | ERROR_FILE_NOT_FOUND | IO |
| 2001 | ERROR_PERMISSION_DENIED | IO |
| 2002 | ERROR_DISK_FULL | IO |
| 2003 | ERROR_FILE_EXISTS | IO |
| 2004 | ERROR_PATH_TOO_LONG | IO |
| 2005 | ERROR_IO_FAILED | IO |
| 2006 | ERROR_TOO_MANY_OPEN_FILES | IO |
| 3000 | ERROR_INVALID_MAGIC | Format |
| 3001 | ERROR_UNSUPPORTED_VERSION | Format |
| 3002 | ERROR_INVALID_HEADER | Format |
| 3003 | ERROR_INVALID_SECTION | Format |
| 3004 | ERROR_INVALID_CHUNK | Format |
| 3005 | ERROR_INVALID_INDEX | Format |
| 3006 | ERROR_INVALID_CHECKSUM | Format |
| 3007 | ERROR_INVALID_SIGNATURE | Format |
| 4000 | ERROR_ENTRY_NOT_FOUND | Operation |
| 4001 | ERROR_ENTRY_EXISTS | Operation |
| 4002 | ERROR_ENTRY_IS_DIRECTORY | Operation |
| 4003 | ERROR_ENTRY_IS_FILE | Operation |
| 4004 | ERROR_CONTAINER_FULL | Operation |
| 4005 | ERROR_CONTAINER_CLOSED | Operation |
| 4006 | ERROR_READ_ONLY | Operation |
| 4007 | ERROR_STREAM_CLOSED | Operation |
| 4008 | ERROR_SEEK_FAILED | Operation |
| 5000 | ERROR_INVALID_KEY | Encryption |
| 5001 | ERROR_DECRYPTION_FAILED | Encryption |
| 5002 | ERROR_AUTHENTICATION_FAILED | Encryption |
| 5003 | ERROR_KEY_DERIVATION_FAILED | Encryption |
| 5004 | ERROR_UNSUPPORTED_ALGORITHM | Encryption |
| 6000 | ERROR_CHECKSUM_MISMATCH | Integrity |
| 6001 | ERROR_SIGNATURE_INVALID | Integrity |
| 6002 | ERROR_TAMPERING_DETECTED | Integrity |

---

# Appendix C — Event Name Registry

## C.1 Complete Event Name List

| Event Name | Category |
|------------|----------|
| container.created | Container |
| container.opened | Container |
| container.closed | Container |
| container.saved | Container |
| container.validated | Container |
| container.repaired | Container |
| container.encrypted | Container |
| container.decrypted | Container |
| container.signed | Container |
| container.verified | Container |
| entry.added | Entry |
| entry.removed | Entry |
| entry.renamed | Entry |
| entry.moved | Entry |
| entry.copied | Entry |
| entry.modified | Entry |
| entry.metadataChanged | Entry |
| entry.thumbnailSet | Entry |
| entry.encrypted | Entry |
| entry.decrypted | Entry |
| stream.opened | Stream |
| stream.closed | Stream |
| stream.progress | Stream |
| stream.completed | Stream |
| stream.error | Stream |
| search.started | Search |
| search.progress | Search |
| search.completed | Search |
| validation.started | Validation |
| validation.progress | Validation |
| validation.completed | Validation |
| repair.started | Repair |
| repair.progress | Repair |
| repair.completed | Repair |
| repair.failed | Repair |
| encryption.started | Encryption |
| encryption.progress | Encryption |
| encryption.completed | Encryption |
| decryption.started | Encryption |
| decryption.progress | Encryption |
| decryption.completed | Encryption |

---

**End of SDK Specification**

*BOX SDK Specification v1.0 — 2026-06-29*
