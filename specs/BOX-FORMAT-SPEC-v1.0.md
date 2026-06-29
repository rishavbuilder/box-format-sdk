# BOX File Format Standard v1.0

**Official Specification**

**Document Status:** Proposed Standard  
**Version:** 1.0.0  
**Date:** 2026-06-29  
**Classification:** Open File Format Standard

---

## Copyright Notice

This specification is released under the Open File Format License (OFFL). Any developer may implement BOX Readers and Writers without royalty obligations.

---

## Abstract

BOX is a universal binary container format designed for storing, organizing, streaming, and managing files and folder hierarchies within a single self-contained file. BOX provides random access, streaming, encryption, compression, integrity verification, and corruption recovery. It is designed to succeed legacy archive formats (ZIP, TAR, RAR) by treating the container as a portable virtual filesystem rather than a flat archive.

---

## Table of Contents

- [Section 1 — Introduction](#section-1--introduction)
- [Section 2 — History](#section-2--history)
- [Section 3 — Core Philosophy](#section-3--core-philosophy)
- [Section 4 — Terminology](#section-4--terminology)
- [Section 5 — File Identification](#section-5--file-identification)
- [Section 6 — Complete Binary Layout](#section-6--complete-binary-layout)
- [Section 7 — Header Specification](#section-7--header-specification)
- [Section 8 — Metadata Specification](#section-8--metadata-specification)
- [Section 9 — Folder System](#section-9--folder-system)
- [Section 10 — File Index](#section-10--file-index)
- [Section 11 — Chunk Architecture](#section-11--chunk-architecture)
- [Section 12 — Streaming](#section-12--streaming)
- [Section 13 — Compression](#section-13--compression)
- [Section 14 — Encryption](#section-14--encryption)
- [Section 15 — Checksums](#section-15--checksums)
- [Section 16 — Error Recovery](#section-16--error-recovery)
- [Section 17 — Version Compatibility](#section-17--version-compatibility)
- [Section 18 — Performance](#section-18--performance)
- [Section 19 — Security Considerations](#section-19--security-considerations)
- [Section 20 — Reference API](#section-20--reference-api)
- [Section 21 — Future Extensions](#section-21--future-extensions)
- [Section 22 — Examples](#section-22--examples)
- [Appendix A — Numeric Identifiers](#appendix-a--numeric-identifiers)
- [Appendix B — Registered Compression Algorithms](#appendix-b--registered-compression-algorithms)
- [Appendix C — Registered Encryption Algorithms](#appendix-c--registered-encryption-algorithms)
- [Appendix D — Implementation Notes](#appendix-d--implementation-notes)

---

# Section 1 — Introduction

## 1.1 Purpose

This document specifies the BOX File Format (version 1.0), a universal binary container format for storing files, folder hierarchies, metadata, and associated resources within a single self-contained file.

## 1.2 Scope

BOX is designed as an open, platform-independent format. Any software developer may implement a BOX Reader or BOX Writer by conforming to this specification. No license, registration, or royalty is required.

## 1.3 Intended Audience

This specification is intended for:

- File format library developers
- Operating system engineers
- Application developers implementing BOX support
- Storage system architects
- Security auditors reviewing the format

## 1.4 Document Conventions

- All multi-byte integers are stored in **little-endian** byte order unless explicitly stated otherwise.
- All offsets are expressed in **bytes** from the beginning of the file.
- All bit flags are described with bit 0 being the least significant bit (LSB).
- The term "MUST" indicates a mandatory requirement. "SHOULD" indicates a recommended behavior. "MAY" indicates an optional behavior.
- Hexadecimal values are prefixed with `0x`. Binary values are prefixed with `0b`.
- Reserved fields MUST be set to zero and MUST be ignored by readers.

---

# Section 2 — History

## 2.1 Why BOX Exists

The computing industry has relied on archive formats designed decades ago under constraints that no longer apply. Each existing format carries fundamental architectural limitations that prevent modern use cases.

## 2.2 Problems with ZIP

ZIP was designed in 1989 for MS-DOS. Its limitations include:

- **65,535 file limit** imposed by the central directory structure.
- **4 GB uncompressed file size limit** due to 32-bit size fields in the local file header.
- **No native folder metadata** — directories are encoded as entries with trailing slashes.
- **No streaming support** — the central directory must be written at the end, requiring the entire content to be known before finalization.
- **No per-file encryption metadata** — encryption is an afterthought with no standardized key management.
- **Fragmented specification** — the APPNOTE document has accumulated 14+ extension documents making full conformance difficult.
- **No chunk-based storage** — files must be contiguous or use extensions that break compatibility.
- **No built-in integrity verification** — CRC-32 is optional and insufficient for large files.

## 2.3 Problems with TAR

TAR (Tape ARchive) was designed in 1979 for magnetic tape:

- **No compression** — TAR is purely a packaging format requiring external compression (gzip, bzip2, xz).
- **Sequential-only access** — extracting a single file from a 10 GB TAR requires scanning from the beginning.
- **No random access** — no index structure exists for seeking to specific files.
- **Duplicate headers** — TAR stores a copy of the header after each file for recovery, wasting space.
- **No native encryption** — encryption is entirely external.
- **No metadata beyond POSIX attributes** — no tags, comments, thumbnails, or application metadata.
- **Block alignment waste** — 512-byte block alignment wastes space for small files.

## 2.4 Problems with RAR

RAR is a proprietary format:

- **Proprietary specification** — full specification is not publicly available.
- **Patent encumbered** — RAR compression algorithms are patented.
- **Vendor lock-in** — WinRAR controls the format and licensing.
- **No open-source reference implementation** — third-party tools must reverse-engineer the format.
- **Complex recovery records** — recovery is powerful but format-specific.

## 2.5 Problems with Other Formats

- **7Z**: While open-source, the format lacks streaming support and has limited adoption.
- **CAB**: Microsoft-specific, limited to Windows ecosystem.
- **ISO 9660/UDF**: Optical media focused, not general-purpose.
- **RAR5**: Improved but still proprietary.

## 2.6 The Gap

No existing format provides:

1. Native folder tree representation
2. Chunk-based random access
3. Streaming support without central directory
4. Per-file encryption with standardized key management
5. Built-in search indexing
6. Corruption recovery at the chunk level
7. Thumbnail and preview storage
8. Digital signature support
9. Future extensibility without format versioning breaks

BOX was designed to fill this gap.

---

# Section 3 — Core Philosophy

## 3.1 Container, Not Archive

BOX is not an archive. An archive implies packaging files for later extraction. BOX is a container — a portable virtual filesystem that applications interact with directly. Files inside a BOX are not "extracted"; they are accessed, streamed, modified, and queried.

## 3.2 Random Access by Design

Every element in a BOX can be located and accessed without reading any other element. The format is structured around offset-based lookups, not sequential scanning.

## 3.3 Streaming Capable

BOX supports progressive construction and consumption. A BOX can be written incrementally and read while still being written. Video players, audio players, and document viewers can stream content without the BOX being complete.

## 3.4 Self-Describing

A BOX contains all information needed to interpret its contents. No external configuration, databases, or sidecar files are required. The container is self-describing through metadata, type annotations, and structural indices.

## 3.5 Resilient

BOX is designed for real-world conditions: interrupted writes, corrupted sectors, partial downloads, and media degradation. Recovery mechanisms operate at multiple granularities.

## 3.6 Forward-Compatible

The format is designed to accommodate features not yet imagined. Extension points are embedded throughout the binary layout. Unknown sections are safely ignorable. Future versions can add capabilities without breaking existing readers.

## 3.7 Minimal Overhead

BOX targets minimal structural overhead. The format is designed so that the overhead percentage decreases as file sizes increase. For large containers, BOX overhead approaches zero.

## 3.8 Security First

BOX integrates encryption, integrity verification, and digital signatures at the format level. Security is not bolted on as an afterthought.

---

# Section 4 — Terminology

## 4.1 Container

A BOX file. The complete binary file conforming to this specification.

## 4.2 Entry

Any item stored within the container. An entry is either a **file** or a **folder**.

## 4.3 File Entry

An entry representing a regular file with associated data chunks.

## 4.4 Folder Entry

An entry representing a directory. Folders contain no data chunks. Folders establish hierarchy.

## 4.5 Chunk

The fundamental unit of file data storage. A chunk contains a portion (or entirety) of a file's data. Chunks enable random access, streaming, and partial loading.

## 4.6 Chunk ID

A unique 64-bit unsigned integer identifying a chunk within the container.

## 4.7 Entry ID

A unique 32-bit unsigned integer identifying an entry (file or folder) within the container.

## 4.8 Offset

A 64-bit unsigned integer representing a byte position from the beginning of the container file. Offset 0 is the first byte of the file.

## 4.9 Section

A contiguous region of the container with a defined purpose (header, metadata, index, etc.). Sections appear in a defined order.

## 4.10 Frame

The binary envelope surrounding section data. A frame consists of a frame header, frame data, and a frame trailer.

## 4.11 Checksum

A cryptographic hash used for integrity verification. BOX uses SHA-256 as the default checksum algorithm.

## 4.12 Tag

A key-value pair stored in metadata for user-defined or application-defined annotation.

## 4.13 Thumbnail

A compressed image preview associated with a file entry or the container itself.

## 4.14 Digital Signature

A cryptographic signature applied to container sections for authenticity and integrity verification.

## 4.15 Encryption Envelope

The structure wrapping encrypted chunk data, containing the initialization vector, encrypted payload, and authentication tag.

## 4.16 Path Table

A lookup structure mapping hierarchical paths to Entry IDs.

## 4.17 Chunk Table

A lookup structure mapping (Entry ID, chunk index) pairs to chunk offsets and sizes.

## 4.18 Extension Slot

A reserved area in the binary layout designated for future use. Extension slots have defined sizes and positions but undefined content in version 1.0.

---

# Section 5 — File Identification

## 5.1 Magic Bytes

Every valid BOX file begins with the following 4-byte magic sequence:

```
Offset 0x00: 42 58 4F 58
```

ASCII representation: `BXOX`

This sequence uniquely identifies BOX files and distinguishes them from all other file formats.

## 5.2 File Signature

The full file signature consists of the magic bytes followed by the version identifier:

```
Offset 0x00: 42 58 4F 58   (magic: "BXOX")
Offset 0x04: 01 00         (version: 1.0, little-endian)
```

Total signature length: 6 bytes.

## 5.3 Header Identifier

The header identifier is the first 16 bytes of the container:

```
Offset 0x00: 42 58 4F 58   (magic: "BXOX")
Offset 0x04: 01 00         (format version: 1.0)
Offset 0x06: 01 00         (header version: 1.0)
Offset 0x08: [8 bytes]     (reserved, must be zero)
```

Total header identifier length: 16 bytes.

## 5.4 End Marker

The container ends with a Footer section that includes an end marker. The end marker is not a fixed byte sequence; instead, the footer structure (see Section 6) contains fields that allow readers to locate the footer from the end of the file.

## 5.5 Version Identifier

| Bytes | Offset | Value | Description |
|-------|--------|-------|-------------|
| 2 | 0x04 | `01 00` | Format major.minor version (1.0) |

The version identifier uses a 16-bit value where the high byte is the major version and the low byte is the minor version.

## 5.6 Reserved Bytes

Bytes at offsets 0x06 through 0x0F are reserved for future use. They MUST be set to zero in version 1.0.

## 5.7 File Extension

The standard file extension for BOX is `.box`.

## 5.8 MIME Type

The registered MIME type is: `application/box`

## 5.9 File Signature for Detection

To detect BOX files, a reader SHOULD check:

1. Bytes 0x00–0x03 equal `42 58 4F 58`
2. Bytes 0x04–0x05 equal `01 00` (version 1.0)
3. The file size is at least 256 bytes

---

# Section 6 — Complete Binary Layout

## 6.1 High-Level Structure

A BOX container is organized into the following top-level regions:

```
+---------------------------+
|       HEADER              |  Fixed size: 256 bytes
+---------------------------+
|       METADATA            |  Variable size
+---------------------------+
|       FOLDER TREE         |  Variable size
+---------------------------+
|       FILE INDEX          |  Variable size
+---------------------------+
|       CHUNK TABLE         |  Variable size
+---------------------------+
|       CHUNKS              |  Variable size, bulk of file
+---------------------------+
|       EXTENSION SLOTS     |  Fixed size: 4 × 256 bytes = 1024 bytes
+---------------------------+
|       FOOTER              |  Fixed size: 128 bytes
+---------------------------+
```

## 6.2 Section Ordering

Sections MUST appear in the order listed above. Readers MUST NOT assume sections can appear in arbitrary order.

## 6.3 Section Headers

Every section (except HEADER and FOOTER) is preceded by a Section Frame:

```
+-------------------------------+
|  Section Type (4 bytes)       |  e.g., "META", "FTRE", "FIDX", "CTAB"
+-------------------------------+
|  Section Size (8 bytes)       |  Size of section data in bytes
+-------------------------------+
|  Section CRC32 (4 bytes)      |  CRC-32 of section data
+-------------------------------+
|  Reserved (4 bytes)           |  Must be zero
+-------------------------------+
|  Section Data (N bytes)       |  Section-specific content
+-------------------------------+
```

Section Frame overhead: 20 bytes per section.

## 6.4 Alignment Rules

- All sections MUST begin on 64-byte boundaries relative to the start of the file.
- If a section's natural end does not land on a 64-byte boundary, padding bytes (0x00) MUST be inserted.
- Chunk data SHOULD be aligned to 4096-byte boundaries for optimal I/O performance.
- Padding bytes are not counted in the section size field.

## 6.5 Padding Rules

| Region | Alignment | Padding Byte |
|--------|-----------|--------------|
| Header | Start of file | N/A |
| Metadata | 64 bytes | 0x00 |
| Folder Tree | 64 bytes | 0x00 |
| File Index | 64 bytes | 0x00 |
| Chunk Table | 64 bytes | 0x00 |
| Chunks | 4096 bytes | 0x00 |
| Extension Slots | 64 bytes | 0x00 |
| Footer | 64 bytes | 0x00 |

## 6.6 Minimal BOX Structure

A minimal BOX containing zero files and zero folders:

```
Offset 0x0000: Header (256 bytes)
Offset 0x0100: Metadata Section Frame + empty metadata (20 bytes)
Offset 0x0114: Folder Tree Section Frame + empty tree (20 bytes)
Offset 0x0128: File Index Section Frame + empty index (20 bytes)
Offset 0x013C: Chunk Table Section Frame + empty table (20 bytes)
Offset 0x0150: Extension Slots (1024 bytes)
Offset 0x0550: Footer (128 bytes)
```

Total minimal BOX size: 1424 bytes (plus alignment padding).

## 6.7 Offset Diagram

```
0x0000 ┌──────────────────────────────┐
       │         HEADER               │  256 bytes
0x0100 ├──────────────────────────────┤
       │    Section Frame: META       │  20 bytes
       │    Metadata Data             │  N bytes
       │    Padding                   │  0–63 bytes
0x???? ├──────────────────────────────┤
       │    Section Frame: FTRE       │  20 bytes
       │    Folder Tree Data          │  N bytes
       │    Padding                   │  0–63 bytes
0x???? ├──────────────────────────────┤
       │    Section Frame: FIDX       │  20 bytes
       │    File Index Data           │  N bytes
       │    Padding                   │  0–63 bytes
0x???? ├──────────────────────────────┤
       │    Section Frame: CTAB       │  20 bytes
       │    Chunk Table Data          │  N bytes
       │    Padding                   │  0–63 bytes
0x???? ├──────────────────────────────┤
       │    CHUNK 0                   │  Variable
       │    Padding                   │  0–4095 bytes
       │    CHUNK 1                   │  Variable
       │    Padding                   │  0–4095 bytes
       │    ...                       │
       │    CHUNK N                   │  Variable
0x???? ├──────────────────────────────┤
       │    Extension Slot 0          │  256 bytes
       │    Extension Slot 1          │  256 bytes
       │    Extension Slot 2          │  256 bytes
       │    Extension Slot 3          │  256 bytes
0x???? ├──────────────────────────────┤
       │         FOOTER               │  128 bytes
0x???? └──────────────────────────────┘
```

---

# Section 7 — Header Specification

## 7.1 Overview

The header is the first 256 bytes of the container. It is always at a fixed offset (0x0000) and has a fixed size. The header is not preceded by a section frame.

## 7.2 Header Layout

```
Offset  Size  Field
------  ----  -----
0x0000  4     Magic Bytes
0x0004  2     Format Version
0x0006  2     Header Version
0x0008  8     Container ID
0x0010  8     Creation Timestamp
0x0018  8     Modification Timestamp
0x0020  4     Flags
0x0024  4     Entry Count
0x0028  4     File Entry Count
0x002C  4     Folder Entry Count
0x0030  8     Total Data Size
0x0038  4     Compression Algorithm
0x003C  4     Encryption Algorithm
0x0040  4     Checksum Algorithm
0x0044  4     Metadata Offset
0x0048  4     Metadata Size
0x004C  4     Folder Tree Offset
0x0050  4     Folder Tree Size
0x0054  4     File Index Offset
0x0058  4     File Index Size
0x005C  4     Chunk Table Offset
0x0060  4     Chunk Table Size
0x0064  4     Chunks Offset
0x0068  8     Chunks Size
0x0070  4     Extension Slots Offset
0x0074  4     Extension Slots Size
0x0078  4     Footer Offset
0x007C  4     Footer Size
0x0080  32    Header Checksum (SHA-256)
0x00A0  88    Reserved
0x00F8  8     Header End Marker
```

## 7.3 Field Descriptions

### 7.3.1 Magic Bytes (4 bytes)

| Offset | Value | Description |
|--------|-------|-------------|
| 0x00 | `42 58 4F 58` | ASCII "BXOX" |

### 7.3.2 Format Version (2 bytes)

| Offset | Value | Description |
|--------|-------|-------------|
| 0x04 | `01 00` | Major version (high byte) = 1, Minor version (low byte) = 0 |

### 7.3.3 Header Version (2 bytes)

| Offset | Value | Description |
|--------|-------|-------------|
| 0x06 | `01 00` | Header layout version 1.0 |

### 7.3.4 Container ID (8 bytes)

A unique 64-bit identifier for this container. SHOULD be generated using a cryptographically secure random number generator. Used for deduplication and caching.

### 7.3.5 Creation Timestamp (8 bytes)

A 64-bit unsigned integer representing the Unix timestamp in milliseconds (milliseconds since 1970-01-01T00:00:00Z) when the container was created.

### 7.3.6 Modification Timestamp (8 bytes)

A 64-bit unsigned integer representing the Unix timestamp in milliseconds when the container was last modified.

### 7.3.7 Flags (4 bytes)

| Bit | Name | Description |
|-----|------|-------------|
| 0 | COMPRESSION_ENABLED | Container uses compression |
| 1 | ENCRYPTION_ENABLED | Container uses encryption |
| 2 | SIGNATURE_ENABLED | Container has digital signatures |
| 3 | STREAMING_MODE | Container is in streaming mode (footer may be incomplete) |
| 4 | RECOVERY_MODE | Container includes recovery data |
| 5 | THUMBNAILS_ENABLED | Container includes thumbnails |
| 6 | SEARCH_INDEX_ENABLED | Container includes search index |
| 7–31 | RESERVED | Must be zero |

### 7.3.8 Entry Count (4 bytes)

Total number of entries (files + folders) in the container.

### 7.3.9 File Entry Count (4 bytes)

Number of file entries in the container.

### 7.3.10 Folder Entry Count (4 bytes)

Number of folder entries in the container.

### 7.3.11 Total Data Size (8 bytes)

Total uncompressed size of all file data in bytes.

### 7.3.12 Compression Algorithm (4 bytes)

Identifier for the default compression algorithm used in this container.

| Value | Algorithm |
|-------|-----------|
| 0x00000000 | None (no compression) |
| 0x00000001 | LZ4 |
| 0x00000002 | Zstandard (zstd) |
| 0x00000003 | Brotli |
| 0x00000004 | Deflate (RFC 1951) |
| 0x00000005 | LZMA |
| 0x00000006 | Zlib |
| 0x000000FF | Algorithm ID stored in chunk headers |
| 0x00000100–0xFFFFFFFF | Reserved for future algorithms |

### 7.3.13 Encryption Algorithm (4 bytes)

Identifier for the default encryption algorithm.

| Value | Algorithm |
|-------|-----------|
| 0x00000000 | None (no encryption) |
| 0x00000001 | AES-256-GCM |
| 0x00000002 | AES-256-CBC |
| 0x00000003 | ChaCha20-Poly1305 |
| 0x00000004 | XChaCha20-Poly1305 |
| 0x000000FF | Algorithm ID stored in chunk headers |
| 0x00000100–0xFFFFFFFF | Reserved for future algorithms |

### 7.3.14 Checksum Algorithm (4 bytes)

| Value | Algorithm |
|-------|-----------|
| 0x00000000 | None |
| 0x00000001 | CRC-32 |
| 0x00000002 | xxHash-64 |
| 0x00000003 | SHA-256 |
| 0x00000004 | BLAKE3 |

### 7.3.15 Metadata Offset (4 bytes)

Absolute byte offset to the start of the Metadata Section Frame.

### 7.3.16 Metadata Size (4 bytes)

Size of the Metadata Section Frame (including frame header) in bytes.

### 7.3.17 Folder Tree Offset (4 bytes)

Absolute byte offset to the start of the Folder Tree Section Frame.

### 7.3.18 Folder Tree Size (4 bytes)

Size of the Folder Tree Section Frame in bytes.

### 7.3.19 File Index Offset (4 bytes)

Absolute byte offset to the start of the File Index Section Frame.

### 7.3.20 File Index Size (4 bytes)

Size of the File Index Section Frame in bytes.

### 7.3.21 Chunk Table Offset (4 bytes)

Absolute byte offset to the start of the Chunk Table Section Frame.

### 7.3.22 Chunk Table Size (4 bytes)

Size of the Chunk Table Section Frame in bytes.

### 7.3.23 Chunks Offset (4 bytes)

Absolute byte offset to the start of the first chunk.

### 7.3.24 Chunks Size (8 bytes)

Total size of all chunks in bytes (including inter-chunk padding).

### 7.3.25 Extension Slots Offset (4 bytes)

Absolute byte offset to the start of the Extension Slots region.

### 7.3.26 Extension Slots Size (4 bytes)

Size of the Extension Slots region in bytes (always 1024 in v1.0).

### 7.3.27 Footer Offset (4 bytes)

Absolute byte offset to the start of the Footer.

### 7.3.28 Footer Size (4 bytes)

Size of the Footer in bytes (always 128 in v1.0).

### 7.3.29 Header Checksum (32 bytes)

SHA-256 hash of bytes 0x0000 through 0x007F (the first 128 bytes of the header). This allows readers to verify header integrity before parsing.

### 7.3.30 Reserved (88 bytes)

Reserved for future use. Must be zero. Readers MUST ignore this field.

### 7.3.31 Header End Marker (8 bytes)

| Value | Description |
|-------|-------------|
| `42 58 4F 58 45 4E 44 00` | ASCII "BXOEND\0" |

This marker allows readers to confirm they have correctly parsed the header.

---

# Section 8 — Metadata Specification

## 8.1 Overview

The Metadata section stores container-level and application-level metadata. It is self-describing using a tag-length-value (TLV) encoding.

## 8.2 Section Frame

```
Offset  Size  Field
------  ----  -----
0x00    4     Section Type: "META" (4D 45 54 41)
0x04    8     Section Size (excluding this frame)
0x0C    4     Section CRC-32
0x10    4     Reserved (must be zero)
0x14    N     Section Data (TLV tags)
```

## 8.3 Tag-Length-Value (TLV) Encoding

Each metadata item is encoded as:

```
+-------------------+
| Tag ID (2 bytes)  |  Little-endian
+-------------------+
| Length (4 bytes)   |  Little-endian, byte length of Value
+-------------------+
| Value (N bytes)   |  Type-dependent encoding
+-------------------+
```

TLV overhead per tag: 6 bytes.

## 8.4 Reserved Tag IDs

| Tag ID | Name | Type | Description |
|--------|------|------|-------------|
| 0x0001 | CONTAINER_NAME | UTF-8 | Human-readable container name |
| 0x0002 | CREATION_TIME | UTF-8 | ISO 8601 creation timestamp |
| 0x0003 | MODIFICATION_TIME | UTF-8 | ISO 8601 modification timestamp |
| 0x0004 | AUTHOR | UTF-8 | Container author name |
| 0x0005 | DESCRIPTION | UTF-8 | Container description |
| 0x0006 | TAGS | UTF-8 | Comma-separated tags |
| 0x0007 | COMMENTS | UTF-8 | Free-form comments |
| 0x0008 | LANGUAGE | UTF-8 | ISO 639-1 language code |
| 0x0009 | TOOL_NAME | UTF-8 | Software that created this container |
| 0x000A | TOOL_VERSION | UTF-8 | Version of the creating tool |
| 0x000B | CONTENT_TYPE | UTF-8 | MIME type of primary content |
| 0x000C | THUMBNAIL_JPEG | Binary | JPEG thumbnail (max 256×256) |
| 0x000D | THUMBNAIL_PNG | Binary | PNG thumbnail (max 256×256) |
| 0x000E | CUSTOM_START | — | Start of user-defined tags (0x000E–0x7FFF) |
| 0x8000 | EXTENSION_START | — | Start of extension-defined tags (0x8000–0xFFFF) |

## 8.5 Tag Ordering

Tags MAY appear in any order. Readers MUST NOT assume tags are sorted. Duplicate tag IDs are permitted for multi-valued tags (e.g., multiple TAGS entries).

## 8.6 UTF-8 Encoding

All string values MUST be encoded as UTF-8 (RFC 3629). Strings MUST NOT be null-terminated within the Value field; the Length field provides the boundary.

## 8.7 Thumbnail Specification

Thumbnails are stored as raw image data in the specified format. Maximum dimensions: 256×256 pixels. Maximum size: 128 KB.

## 8.8 Application Metadata

Applications MAY store custom metadata using Tag IDs in the range 0x000E–0x7FFF. Extension-defined metadata uses 0x8000–0xFFFF.

---

# Section 9 — Folder System

## 9.1 Overview

BOX natively represents folder hierarchies. Folders are first-class entries with unique IDs and parent-child relationships.

## 9.2 Section Frame

```
Offset  Size  Field
------  ----  -----
0x00    4     Section Type: "FTRE" (46 54 52 45)
0x04    8     Section Size
0x0C    4     Section CRC-32
0x10    4     Reserved (must be zero)
0x14    N     Section Data (folder entries)
```

## 9.3 Folder Entry Layout

Each folder entry is 32 bytes:

```
Offset  Size  Field
------  ----  -----
0x00    4     Entry ID (unique 32-bit identifier)
0x04    4     Parent Entry ID (0xFFFFFFFF for root)
0x08    4     Child Count (number of direct children)
0x0C    4     Total Descendant Count
0x10    4     Name Length (bytes, UTF-8)
0x14    4     Flags
0x18    4     Reserved (must be zero)
0x1C    4     CRC-32 of name bytes
```

Followed by the name string (N bytes, UTF-8, NOT null-terminated).

## 9.4 Root Folder

Every BOX contains exactly one root folder with Entry ID 0x00000000 and Parent Entry ID 0xFFFFFFFF.

## 9.5 Folder Hierarchy

Folders are stored in depth-first pre-order traversal of the folder tree. This ordering allows readers to reconstruct the hierarchy by scanning sequentially.

## 9.6 Entry ID Assignment

- Entry IDs are assigned sequentially starting from 0x00000000.
- The root folder is always 0x00000000.
- File Entry IDs follow after all Folder Entry IDs.
- Entry IDs are unique within their type (folder or file) but a folder and a file MAY share the same numeric ID if they are in different ID spaces. To avoid ambiguity, readers SHOULD use the File Index for file lookups and the Folder Tree for folder lookups.

## 9.7 Path Reconstruction

To reconstruct the full path of a folder:

1. Start with the folder's name.
2. Follow Parent Entry IDs upward until reaching the root (Parent = 0xFFFFFFFF).
3. Concatenate names with `/` separators.
4. Prepend `/` for the root path.

Example: `/Documents/Projects/BOX`

## 9.8 Maximum Folder Depth

There is no maximum folder depth. The format supports unlimited nesting.

---

# Section 10 — File Index

## 10.1 Overview

The File Index provides O(1) lookup for any file by its Entry ID. It is a flat array of file entry descriptors.

## 10.2 Section Frame

```
Offset  Size  Field
------  ----  -----
0x00    4     Section Type: "FIDX" (46 49 44 58)
0x04    8     Section Size
0x0C    4     Section CRC-32
0x10    4     Reserved (must be zero)
0x14    N     Section Data (file entries)
```

## 10.3 File Entry Layout

Each file entry is 96 bytes:

```
Offset  Size  Field
------  ----  -----
0x00    4     Entry ID
0x04    4     Parent Folder Entry ID
0x08    4     Name Length (bytes, UTF-8)
0x0C    4     Flags
0x10    8     Uncompressed Size (bytes)
0x18    8     Compressed Size (bytes, 0 if uncompressed)
0x20    4     Chunk Count (number of chunks for this file)
0x24    4     First Chunk Index (index into Chunk Table)
0x28    4     Checksum Algorithm
0x2C    32    File Checksum (SHA-256 of uncompressed data)
0x4C    4     Thumbnail Offset (0 if none)
0x50    4     Thumbnail Size (0 if none)
0x54    4     MIME Type Length (bytes)
0x58    4     Created Timestamp (seconds since epoch)
0x5C    4     Modified Timestamp (seconds since epoch)
0x60    4     CRC-32 of name bytes
0x64    4     Reserved (must be zero)
```

Followed by:
1. Name string (N bytes, UTF-8, NOT null-terminated)
2. MIME type string (M bytes, UTF-8, NOT null-terminated)

## 10.4 Entry ID Assignment

- File Entry IDs start after the last Folder Entry ID.
- If there are F folders, the first file gets Entry ID F.
- File entries are stored in Entry ID order.

## 10.5 Flags

| Bit | Name | Description |
|-----|------|-------------|
| 0 | IS_SYMLINK | Entry is a symbolic link |
| 1 | IS_HARDLINK | Entry is a hard link |
| 2 | IS_EXECUTABLE | Entry has executable permission |
| 3 | IS_HIDDEN | Entry is hidden |
| 4 | IS_ENCRYPTED | Entry data is encrypted |
| 5 | IS_COMPRESSED | Entry data is compressed |
| 6 | HAS_THUMBNAIL | Entry has an associated thumbnail |
| 7 | HAS_CUSTOM_METADATA | Entry has custom metadata TLV tags |
| 8–31 | RESERVED | Must be zero |

## 10.6 Thumbnail Storage

Thumbnails are stored inline in the File Index immediately after the MIME type string. The Thumbnail Offset field stores the byte offset from the start of the File Index Section Data to the thumbnail bytes.

## 10.7 O(1) Lookup

Given Entry ID `E` for a file:

1. Compute index: `I = E - F` (where F is the folder entry count)
2. Compute byte offset: `O = I × 96` (size of each file entry)
3. Seek to `File Index Section Data Offset + O`
4. Read 96 bytes

---

# Section 11 — Chunk Architecture

## 11.1 Overview

File data is stored in chunks. Chunks enable random access, streaming, partial loading, and corruption isolation.

## 11.2 Section Frame

```
Offset  Size  Field
------  ----  -----
0x00    4     Section Type: "CTAB" (43 54 41 42)
0x04    8     Section Size
0x0C    4     Section CRC-32
0x10    4     Reserved (must be zero)
0x14    N     Section Data (chunk descriptors)
```

## 11.3 Chunk Table Entry Layout

Each chunk table entry is 64 bytes:

```
Offset  Size  Field
------  ----  -----
0x00    8     Chunk ID (unique 64-bit identifier)
0x08    4     Entry ID (file this chunk belongs to)
0x0C    4     Chunk Index (position within file, 0-based)
0x10    8     Chunk Offset (absolute byte offset in container)
0x18    8     Chunk Payload Size (uncompressed bytes)
0x20    8     Chunk Stored Size (compressed/encrypted bytes on disk)
0x28    4     Chunk Flags
0x2C    4     Checksum Algorithm
0x30    32    Chunk Checksum (SHA-256 of uncompressed payload)
```

## 11.4 Chunk Flags

| Bit | Name | Description |
|-----|------|-------------|
| 0 | IS_COMPRESSED | Chunk payload is compressed |
| 1 | IS_ENCRYPTED | Chunk payload is encrypted |
| 2 | IS_LAST | This is the last chunk for its file |
| 3 | IS_RECOVERY | Chunk contains recovery data |
| 4 | IS_THUMBNAIL | Chunk contains thumbnail data |
| 5–31 | RESERVED | Must be zero |

## 11.5 Chunk Data Layout

Each chunk in the Chunks region has the following structure:

```
+-------------------------------+
|  Chunk Header                 |  32 bytes
+-------------------------------+
|  Chunk Payload                |  Variable size
+-------------------------------+
|  Encryption Auth Tag          |  16 bytes (if encrypted with AEAD)
+-------------------------------+
|  Padding to 4096-byte align   |  0–4095 bytes
+-------------------------------+
```

## 11.6 Chunk Header

```
Offset  Size  Field
------  ----  -----
0x00    4     Chunk Magic: "CHNK" (43 48 4E 4B)
0x04    8     Chunk ID
0x0C    4     Entry ID
0x10    4     Chunk Index
0x14    4     Chunk Flags
0x18    4     Payload Size (uncompressed)
0x1C    4     Reserved (must be zero)
```

Chunk header size: 32 bytes.

## 11.7 Chunk Payload

The payload is the raw (or compressed/encrypted) file data for this chunk. Maximum chunk payload size: 128 MB. Recommended chunk size: 1 MB–16 MB.

## 11.8 Chunk Validation

After reading a chunk, the reader SHOULD:

1. Verify the chunk magic bytes.
2. Compute the checksum of the uncompressed payload.
3. Compare against the stored checksum.
4. If mismatch, attempt recovery using the next chunk or recovery data.

## 11.9 Chunk Assignment

A file's data is divided into consecutive chunks. The `First Chunk Index` in the File Index points to the first chunk in the Chunk Table. Chunks for the same file are stored contiguously in both the Chunk Table and the Chunks region.

## 11.10 Large File Support

For files larger than 128 MB × number of chunks, multiple chunks are used. The format supports up to 2^32 chunks per container (approximately 4 billion chunks).

---

# Section 12 — Streaming

## 12.1 Overview

BOX supports progressive reading and writing. A reader can begin consuming data before the container is complete. A writer can emit chunks incrementally.

## 12.2 Streaming Mode

When the STREAMING_MODE flag is set in the header:

1. The footer MAY be incomplete or absent.
2. Readers SHOULD use the header offsets for section locations.
3. Writers SHOULD periodically flush a temporary footer for crash recovery.

## 12.3 Progressive Writing

A writer MAY:

1. Write the Header first with placeholder offsets.
2. Write Metadata, Folder Tree, File Index, and Chunk Table incrementally.
3. Write chunks as data becomes available.
4. Finalize by updating the Header offsets and writing the Footer.

## 12.4 Progressive Reading

A reader MAY:

1. Read the Header to determine section offsets.
2. Read Metadata for container information.
3. Read File Index for file listings.
4. Begin streaming chunks for requested files.
5. Continue streaming as more chunks become available (in streaming mode).

## 12.5 Video Streaming

For video playback:

1. Read the File Index to locate the video file entry.
2. Read the Chunk Table to get chunk offsets for the video file.
3. Stream chunks sequentially to the decoder.
4. Seek to specific timestamps by computing the approximate chunk offset from the timestamp.

## 12.6 Audio Streaming

Audio streaming follows the same pattern as video streaming but with smaller chunk sizes (recommended: 256 KB–1 MB for audio).

## 12.7 Partial Loading

A reader MAY load only the sections needed:

- **Directory listing**: Header + File Index only
- **Single file**: Header + File Index + specific chunks
- **Metadata only**: Header + Metadata
- **Folder structure**: Header + Folder Tree

## 12.8 No Extraction Required

At no point must the entire container be extracted to access content. All operations are performable in-place.

---

# Section 13 — Compression

## 13.1 Overview

BOX supports per-chunk compression. Compression is optional and algorithm-independent.

## 13.2 Compression Levels

| Level | Name | Description |
|-------|------|-------------|
| 0 | NONE | No compression |
| 1 | FASTEST | Minimal compression, maximum speed |
| 2 | FAST | Fast compression with reasonable ratio |
| 3 | BALANCED | Balanced speed and ratio |
| 4 | HIGH | High compression ratio |
| 5 | MAXIMUM | Maximum compression ratio |

## 13.3 Default Algorithms

| Priority | Algorithm | Rationale |
|----------|-----------|-----------|
| Primary | Zstandard | Best speed/ratio tradeoff, streaming support |
| Fallback | LZ4 | Fastest decompression, good for streaming |
| Archive | Brotli | High ratio for archival storage |

## 13.4 Per-Chunk Algorithm Selection

Each chunk MAY use a different compression algorithm. The compression algorithm is stored in the chunk flags or as a separate field in the chunk table entry.

## 13.5 Compress-When-Useful

BOX SHOULD NOT compress data that is already compressed (e.g., JPEG, MP4, PNG, ZIP). The writer SHOULD:

1. Check the file extension or MIME type.
2. Skip compression for known compressed formats.
3. Use compression for text, raw data, and uncompressed formats.

## 13.6 Future Algorithm Support

New compression algorithms are registered with the BOX specification maintainer. The algorithm ID space (0x00000100–0xFFFFFFFF) is reserved for future algorithms.

## 13.7 Algorithm Metadata

The compression algorithm used per chunk is recorded in the chunk table entry, allowing different chunks within the same file to use different algorithms.

---

# Section 14 — Encryption

## 14.1 Overview

BOX supports per-file and per-chunk encryption. Encryption is applied at the chunk level before compression (encrypt-then-compress) or after compression (compress-then-encrypt), as specified by the encryption envelope.

## 14.2 Encryption Envelope

```
+-------------------------------+
|  IV / Nonce (16 bytes)        |  Initialization vector
+-------------------------------+
|  Encrypted Payload (N bytes)  |  Ciphertext
+-------------------------------+
|  Auth Tag (16 bytes)          |  GCM/Poly1305 authentication tag
+-------------------------------+
```

## 14.3 Supported Algorithms

| Algorithm | Key Size | Nonce Size | Auth Tag Size | Notes |
|-----------|----------|------------|---------------|-------|
| AES-256-GCM | 256 bits | 96 bits | 128 bits | NIST standard |
| AES-256-CBC | 256 bits | 128 bits | N/A | With PKCS7 padding, use HMAC for auth |
| ChaCha20-Poly1305 | 256 bits | 96 bits | 128 bits | High performance on mobile |
| XChaCha20-Poly1305 | 256 bits | 192 bits | 128 bits | Extended nonce for random IVs |

## 14.4 Per-File Encryption

When a file is encrypted:

1. All chunks for the file are encrypted.
2. The IS_ENCRYPTED flag is set in the File Index entry.
3. Chunk table entries include IS_ENCRYPTED in chunk flags.
4. The encryption algorithm ID is stored in the File Index or Chunk Table.

## 14.5 Key Storage Strategy

BOX does NOT store encryption keys in the container. Keys are managed externally through:

1. **Key Derivation**: Password-based key derivation (PBKDF2, Argon2) with salt stored in the container.
2. **Key Reference**: A key ID stored in the container, with the actual key managed by an external key management system.
3. **Public Key**: For asymmetric encryption, the encrypted DEK (Data Encryption Key) is stored in the container.

## 14.6 Salt Storage

When using password-based encryption, the salt is stored in the Encryption Header:

```
Offset  Size  Field
------  ----  -----
0x00    4     Salt Length
0x04    N     Salt Data
```

The Encryption Header is stored as a special chunk at the beginning of the Chunks region for encrypted containers.

## 14.7 Digital Signatures

BOX supports digital signatures for:

1. **Container Signing**: Sign the entire container for authenticity.
2. **Per-File Signing**: Sign individual files for integrity.
3. **Section Signing**: Sign specific sections (e.g., File Index) for tamper detection.

## 14.8 Signature Envelope

```
+-------------------------------+
|  Signature Algorithm (4 bytes)|  Algorithm ID
+-------------------------------+
|  Key Reference (32 bytes)     |  Public key hash or key ID
+-------------------------------+
|  Signature Data (64 bytes)    |  ECDSA-P256 or Ed25519 signature
+-------------------------------+
|  Timestamp (8 bytes)          |  Signature creation time
+-------------------------------+
```

Signature overhead: 108 bytes per signature.

## 14.9 Integrity Verification

The encryption envelope includes AEAD authentication tags that verify:

1. The ciphertext has not been modified.
2. The nonce/IV has not been tampered with.
3. Associated data (chunk ID, entry ID) has not been altered.

## 14.10 Trust Model

BOX uses a web-of-trust model:

1. Signatures are verified against trusted public keys.
2. Key trust is established out-of-band.
3. Multiple signatures per container are supported (multi-party signing).

---

# Section 15 — Checksums

## 15.1 Overview

BOX uses checksums at three granularities: chunk, file, and container.

## 15.2 Chunk Checksum

Each chunk table entry includes a 32-byte checksum of the uncompressed chunk payload. This allows verification of individual chunks.

```
Chunk Checksum = Hash(Chunk Uncompressed Payload)
```

## 15.3 File Checksum

Each file entry in the File Index includes a 32-byte checksum of the entire uncompressed file data (concatenation of all chunks for that file).

```
File Checksum = Hash(Chunk 0 Payload || Chunk 1 Payload || ... || Chunk N Payload)
```

## 15.4 Container Checksum

The Header Checksum (SHA-256 of the first 128 bytes of the header) provides container-level integrity verification.

## 15.5 Section Checksums

Each section frame includes a CRC-32 checksum of the section data. This provides fast integrity checking for section-level corruption.

## 15.6 Checksum Algorithms

| Algorithm | Size | Speed | Use Case |
|-----------|------|-------|----------|
| CRC-32 | 4 bytes | Very fast | Section frames, quick validation |
| xxHash-64 | 8 bytes | Very fast | Chunk checksums, high performance |
| SHA-256 | 32 bytes | Moderate | File checksums, cryptographic integrity |
| BLAKE3 | 32 bytes | Fast | File checksums, parallelizable |

## 15.7 Checksum Verification Order

1. Verify Header Checksum on open.
2. Verify section CRC-32 when parsing sections.
3. Verify chunk checksums when reading chunk data.
4. Verify file checksums when integrity validation is requested.

---

# Section 16 — Error Recovery

## 16.1 Overview

BOX is designed for resilience against partial corruption, interrupted writes, and media degradation.

## 16.2 Interrupted Writes

If a BOX is interrupted during writing:

1. The reader can detect the incomplete write by:
   - Missing or invalid footer.
   - STREAMING_MODE flag still set.
   - Section sizes not matching actual data.

2. Recovery procedure:
   - Scan the Chunks region for valid chunk headers (magic "CHNK").
   - Rebuild the Chunk Table from discovered chunks.
   - Rebuild the File Index from chunk Entry IDs.
   - Rebuild the Folder Tree from File Index parent references.
   - Recalculate checksums.

## 16.3 Corrupted Chunks

If a chunk is corrupted:

1. The chunk checksum will fail verification.
2. The reader SHOULD:
   - Log the corruption.
   - Return partial data if possible (for streaming).
   - Attempt to read the next valid chunk.
   - If recovery chunks are available, use them.

## 16.4 Missing Metadata

If the Metadata section is corrupted:

1. The reader can still parse the container using the Header offsets.
2. File data remains accessible.
3. Container-level metadata is lost but not critical.

## 16.5 Broken Index

If the File Index or Chunk Table is corrupted:

1. Scan the Chunks region for valid chunk headers.
2. Rebuild the index from discovered chunks.
3. This is slower but recovers data integrity.

## 16.6 Recovery Algorithms

### 16.6.1 Forward Scan Recovery

Starting from a known good position (e.g., after a valid chunk), scan forward looking for the next valid chunk header ("CHNK"). Repeat until end of file or a configurable limit.

### 16.6.2 Backward Scan Recovery

Starting from the Footer (if valid), scan backward looking for the last valid chunk header. This recovers the most recent data.

### 16.6.3 Seed Recovery

If the original file index is available out-of-band, use it to seek to expected chunk offsets and verify chunk headers.

## 16.7 Repair Mode

A BOX Repair tool SHOULD:

1. Read the container to identify all corruption.
2. Present a corruption report to the user.
3. Attempt automatic repair for recoverable corruption.
4. Create a backup of the corrupted container before repair.
5. Write a new, repaired container.

## 16.8 Recovery Data Chunks

When the RECOVERY_MODE flag is set:

1. Additional recovery chunks are stored in the container.
2. Recovery chunks contain parity data (e.g., Reed-Solomon) for lost chunks.
3. Recovery overhead is configurable (default: 10% of data size).

---

# Section 17 — Version Compatibility

## 17.1 Backward Compatibility

A reader of version X MUST be able to read containers of version X and all earlier versions.

## 17.2 Forward Compatibility

A reader of version 1.0 SHOULD handle unknown sections gracefully:

1. Read the section type and size from the Section Frame.
2. Skip the section if the type is unknown.
3. Continue parsing subsequent sections.

## 17.3 Unknown Section Handling

```
if section_type not in known_types:
    skip(section_size + padding)
    continue
```

## 17.4 Reserved Section Handling

Sections with reserved type IDs (0x80000000–0xFFFFFFFF) MUST be skipped by readers that do not implement them.

## 17.5 Migration Strategy

When the format is updated:

1. New fields are added at the end of structures.
2. New flags are added to reserved bit positions.
3. New section types are registered.
4. Old readers ignore unknown fields, flags, and sections.

## 17.6 Version Negotiation

Readers SHOULD check the Format Version before parsing and reject containers with major versions higher than the reader's maximum supported version.

## 17.7 Header Versioning

The Header Version (separate from Format Version) allows the header layout to evolve independently. If the header layout changes, the Header Version is incremented.

---

# Section 18 — Performance

## 18.1 Random Access

BOX provides O(1) file lookup through the File Index and O(1) chunk lookup through the Chunk Table. This enables:

1. Instant directory listings.
2. Direct access to any file without scanning.
3. Seeking within files without reading preceding data.

## 18.2 Streaming Performance

BOX streaming is optimized for:

1. Sequential chunk reading for video/audio playback.
2. Minimal seeking overhead.
3. Buffer-friendly chunk boundaries.

## 18.3 Memory Usage

The format is designed for low memory overhead:

1. File Index is a flat array (no pointer chasing).
2. Chunk Table is a flat array.
3. Folder Tree is a flat array (no recursive structures).
4. Metadata is a flat TLV list.

Typical memory usage for parsing: < 1 MB for containers with up to 100,000 files.

## 18.4 Large File Handling

BOX handles large files (100 GB+) through:

1. Chunk-based storage (no single file must be contiguous).
2. 64-bit offsets for all size and offset fields.
3. Streaming support (no full-file buffering).
4. Incremental checksum computation.

## 18.5 Fast Parsing

The format is optimized for fast parsing:

1. Fixed-size headers and index entries (no variable-length parsing).
2. Little-endian byte order (native on most architectures).
3. Aligned fields for direct memory mapping.
4. Minimal branching in parsing logic.

## 18.6 Incremental Updates

BOX supports incremental updates without rewriting the entire container:

1. Append new chunks at the end of the Chunks region.
2. Update the Chunk Table.
3. Update the File Index.
4. Update the Header offsets.

For deletions, chunks are marked as free space. The format supports free space management through a Free Space Table (future extension).

## 18.7 I/O Optimization

1. Section alignment to 64-byte boundaries reduces partial reads.
2. Chunk alignment to 4096-byte boundaries matches OS page size.
3. Header at a fixed offset enables single-seek open.
4. Footer at a known position enables fast format detection.

---

# Section 19 — Security Considerations

## 19.1 Threat Model

BOX containers may be:

1. Stored on untrusted storage (cloud, USB drives, shared servers).
2. Transferred over untrusted networks.
3. Modified by malicious actors.
4. Subject to replay attacks (substituting old versions).
5. Subject to spoofing (fake containers with valid-looking headers).

## 19.2 Tampering

| Threat | Mitigation |
|--------|------------|
| Header modification | Header Checksum (SHA-256) |
| Section modification | Section CRC-32 |
| Chunk modification | Chunk Checksum (SHA-256) |
| File modification | File Checksum (SHA-256) |
| Full container tampering | Digital Signature |

## 19.3 Spoofing

1. Magic bytes prevent format misidentification.
2. Header End Marker confirms correct header parsing.
3. Section type identifiers confirm correct section identification.
4. Digital signatures verify authenticity.

## 19.4 Replay Attacks

1. Container ID is unique per container.
2. Timestamps record creation and modification times.
3. Digital signatures can include nonces to prevent replay.
4. Version numbers can track container evolution.

## 19.5 Signature Validation

Readers SHOULD:

1. Verify digital signatures before trusting signed data.
2. Check signature timestamp against container timestamps.
3. Verify the signed data has not been modified.
4. Confirm the signing key is trusted.

## 19.6 Trust Model

1. BOX does not mandate a specific PKI (Public Key Infrastructure).
2. Trust is established out-of-band.
3. Multiple signatures support multi-party trust.
4. Key reference fields store key identifiers, not keys.

## 19.7 Secure Erasure

When deleting encrypted data from a BOX:

1. Overwrite the chunk data with random bytes.
2. Update the Chunk Table to mark chunks as free.
3. Recalculate checksums.
4. The encryption key must be securely erased separately.

## 19.8 Side-Channel Resistance

1. Encrypted chunks have randomized sizes (padding to alignment boundaries).
2. Chunk IDs are not sequential within a file (scattered assignment is optional).
3. Timing attacks are mitigated by constant-time checksum comparison.

---

# Section 20 — Reference API

## 20.1 Overview

This section specifies logical APIs for BOX operations. No implementation is provided; this section defines the interface contract that implementations SHOULD follow.

## 20.2 BOX Reader

```
BoxReader:
    open(path: string) -> BoxContainer
    close(container: BoxContainer) -> void
    list_entries(container: BoxContainer) -> List<EntryInfo>
    get_entry(container: BoxContainer, id: EntryID) -> EntryInfo
    read_entry(container: BoxContainer, id: EntryID) -> bytes
    read_chunk(container: BoxContainer, chunk_id: ChunkID) -> bytes
    get_metadata(container: BoxContainer) -> Map<TagID, Value>
    get_folder_tree(container: BoxContainer) -> FolderNode
    search(container: BoxContainer, query: string) -> List<EntryInfo>
    stream_entry(container: BoxContainer, id: EntryID) -> Stream
```

## 20.3 BOX Writer

```
BoxWriter:
    create(path: string, options: BoxOptions) -> BoxContainer
    close(container: BoxContainer) -> void
    add_folder(container: BoxContainer, name: string, parent: EntryID) -> EntryID
    add_file(container: BoxContainer, name: string, parent: EntryID, data: bytes) -> EntryID
    add_file_streaming(container: BoxContainer, name: string, parent: EntryID) -> Stream
    set_metadata(container: BoxContainer, tag: TagID, value: Value) -> void
    set_thumbnail(container: BoxContainer, image: bytes, format: ImageFormat) -> void
    sign(container: BoxContainer, key: PrivateKey) -> Signature
    commit(container: BoxContainer) -> void
```

## 20.4 BOX Validator

```
BoxValidator:
    validate(path: string) -> ValidationResult
    verify_checksums(path: string) -> ChecksumResult
    verify_signatures(path: string, trusted_keys: List<PublicKey>) -> SignatureResult
    repair(path: string, output: string) -> RepairResult
```

## 20.5 BOX Inspector

```
BoxInspector:
    inspect_header(path: string) -> HeaderInfo
    inspect_sections(path: string) -> List<SectionInfo>
    inspect_chunks(path: string) -> List<ChunkInfo>
    inspect_free_space(path: string) -> FreeSpaceInfo
    dump_metadata(path: string) -> Map<TagID, Value>
    dump_folder_tree(path: string) -> FolderNode
```

## 20.6 BOX Stream

```
BoxStream:
    open(container: BoxContainer, entry_id: EntryID) -> Stream
    seek(stream: Stream, offset: bytes) -> void
    read(stream: Stream, buffer: bytes, length: bytes) -> bytes
    tell(stream: Stream) -> bytes
    close(stream: Stream) -> void
```

## 20.7 BOX Metadata

```
BoxMetadata:
    get(container: BoxContainer, tag: TagID) -> Value
    set(container: BoxContainer, tag: TagID, value: Value) -> void
    remove(container: BoxContainer, tag: TagID) -> void
    list(container: BoxContainer) -> List<TagID>
    export(container: BoxContainer, format: MetadataFormat) -> bytes
    import(container: BoxContainer, data: bytes, format: MetadataFormat) -> void
```

## 20.8 BOX Search

```
BoxSearch:
    by_name(container: BoxContainer, pattern: string) -> List<EntryInfo>
    by_mime(container: BoxContainer, mime: string) -> List<EntryInfo>
    by_size(container: BoxContainer, min: bytes, max: bytes) -> List<EntryInfo>
    by_date(container: BoxContainer, start: timestamp, end: timestamp) -> List<EntryInfo>
    by_tag(container: BoxContainer, tag: TagID, value: Value) -> List<EntryInfo>
    full_text(container: BoxContainer, query: string) -> List<EntryInfo>
```

## 20.9 BOX Encrypt

```
BoxEncrypt:
    encrypt_file(container: BoxContainer, entry_id: EntryID, key: Key) -> void
    encrypt_container(container: BoxContainer, key: Key) -> void
    decrypt_file(container: BoxContainer, entry_id: EntryID, key: Key) -> void
    decrypt_container(container: BoxContainer, key: Key) -> void
    change_password(container: BoxContainer, old_password: string, new_password: string) -> void
```

## 20.10 BOX Verify

```
BoxVerify:
    verify_container(path: string) -> VerifyResult
    verify_file(path: string, entry_id: EntryID) -> VerifyResult
    verify_chunk(path: String, chunk_id: ChunkID) -> VerifyResult
    verify_signature(path: string, signature: Signature, public_key: PublicKey) -> bool
```

## 20.11 BOX Repair

```
BoxRepair:
    scan(path: string) -> CorruptionReport
    repair(path: string, output: string, options: RepairOptions) -> RepairResult
    recover_chunks(path: string) -> RecoveryResult
    rebuild_index(path: string) -> RebuildResult
```

---

# Section 21 — Future Extensions

## 21.1 Plugin System

BOX v2.0+ MAY support plugins:

1. Plugin registry for custom section types.
2. Plugin metadata tags.
3. Plugin compression algorithms.
4. Plugin encryption algorithms.

## 21.2 AI Metadata

Future versions MAY include:

1. Object detection labels.
2. Scene classification.
3. Content embeddings for semantic search.
4. Automatic tag generation.

## 21.3 Cloud Metadata

Future versions MAY include:

1. Cloud storage references (S3, GCS, Azure Blob).
2. Sync state metadata.
3. Distributed chunk storage.
4. CDN URLs for public content.

## 21.4 Compression Upgrades

1. Neural network-based compression.
2. Domain-specific compression (video, audio, point cloud).
3. Adaptive compression per chunk.

## 21.5 Encryption Upgrades

1. Post-quantum cryptography (CRYSTALS-Kyber, CRYSTALS-Dilithium).
2. Homomorphic encryption support.
3. Multi-party computation for key management.

## 21.6 Custom Sections

Users and applications MAY define custom sections:

1. Custom section types use IDs 0x80000000–0xFFFFFFFF.
2. Custom sections MUST include a Section Frame.
3. Readers MUST skip unknown custom sections.

## 21.7 Reserved Extension Space

The format reserves 4 Extension Slots (256 bytes each) for future use. These slots have fixed positions and sizes but undefined content in v1.0.

```
Extension Slot 0: Offset 0x????, Size 256 bytes
Extension Slot 1: Offset 0x???? + 256, Size 256 bytes
Extension Slot 2: Offset 0x???? + 512, Size 256 bytes
Extension Slot 3: Offset 0x???? + 768, Size 256 bytes
```

## 21.8 Free Space Management

Future versions MAY include:

1. Free Space Table for tracking deleted chunk space.
2. Compaction support for reclaiming space.
3. Defragmentation algorithms.

## 21.9 Journaling

Future versions MAY include:

1. Write-ahead log for crash recovery.
2. Transaction support for atomic multi-file updates.
3. Undo/redo support.

---

# Section 22 — Examples

## 22.1 Minimal BOX

A BOX with one empty file "hello.txt":

```
HEADER (256 bytes):
  Magic:          42 58 4F 58
  Format Version: 01 00
  Header Version: 01 00
  Container ID:   [8 random bytes]
  Creation Time:  [8 bytes, Unix ms]
  Mod Time:       [8 bytes, Unix ms]
  Flags:          01 00 00 00 (COMPRESSION_ENABLED=0, wait, let me fix)

Actually, let me just show the logical structure:

HEADER:
  Entry Count:         1
  File Entry Count:    1
  Folder Entry Count:  0
  Total Data Size:     0 bytes
  Compression:         None (0x00000000)
  Encryption:          None (0x00000000)
  Checksum:            SHA-256 (0x00000003)

METADATA:
  Tag 0x0001 (CONTAINER_NAME): "Hello BOX"
  Tag 0x0002 (CREATION_TIME): "2026-06-29T00:00:00Z"
  Tag 0x0009 (TOOL_NAME): "BOX Writer v1.0"

FOLDER TREE:
  (empty — no folders)

FILE INDEX:
  Entry 0:
    Entry ID:        0x00000000
    Parent:          0xFFFFFFFF (root virtual)
    Name:            "hello.txt"
    Uncompressed:    0 bytes
    Chunk Count:     0
    Checksum:        [all zeros]

CHUNK TABLE:
  (empty — no chunks)

CHUNKS:
  (empty)

EXTENSION SLOTS:
  [4 × 256 bytes of zeros]

FOOTER:
  Section Offset:  [offset to FOOTER]
  Container Size:  [total file size]
  End Marker:      [footer magic]
```

## 22.2 Multi-File BOX

Structure with folders:

```
/
├── Documents/
│   ├── readme.md
│   └── notes.txt
├── Images/
│   ├── photo.jpg
│   └── diagram.png
└── data.csv

FOLDER TREE:
  Entry 0: ID=0, Parent=0xFFFFFFFF, Name="/", Children=2, Descendants=5
  Entry 1: ID=1, Parent=0, Name="Documents", Children=2, Descendants=2
  Entry 2: ID=2, Parent=0, Name="Images", Children=2, Descendants=2

FILE INDEX:
  Entry 3: ID=3, Parent=1, Name="readme.md", Size=1024 bytes, Chunks=1
  Entry 4: ID=4, Parent=1, Name="notes.txt", Size=256 bytes, Chunks=1
  Entry 5: ID=5, Parent=2, Name="photo.jpg", Size=2048000 bytes, Chunks=2
  Entry 6: ID=6, Parent=2, Name="diagram.png", Size=512000 bytes, Chunks=1
  Entry 7: ID=7, Parent=0xFFFFFFFF, Name="data.csv", Size=4096 bytes, Chunks=1

CHUNK TABLE:
  Chunk 0: ID=0, Entry=3, Index=0, Offset=0x10000, Payload=1024, Compressed=1024
  Chunk 1: ID=1, Entry=4, Index=0, Offset=0x10400, Payload=256, Compressed=256
  Chunk 2: ID=2, Entry=5, Index=0, Offset=0x10800, Payload=1024000, Compressed=1024000
  Chunk 3: ID=3, Entry=5, Index=1, Offset=0x200000, Payload=1024000, Compressed=1024000
  Chunk 4: ID=4, Entry=6, Index=0, Offset=0x300000, Payload=512000, Compressed=512000
  Chunk 5: ID=5, Entry=7, Index=0, Offset=0x380000, Payload=4096, Compressed=4096
```

## 22.3 Encrypted BOX

```
HEADER:
  Flags: 0x00000002 (ENCRYPTION_ENABLED)
  Encryption Algorithm: 0x00000001 (AES-256-GCM)

ENCRYPTION HEADER (stored as first chunk):
  Salt: [32 bytes random]
  Key Reference: [encrypted DEK]

FILE INDEX:
  Entry 0: IS_ENCRYPTED=1, Chunk 0
  Entry 1: IS_ENCRYPTED=1, Chunk 1

CHUNK TABLE:
  Chunk 0: IS_ENCRYPTED=1, Offset=0x10000, Stored=1040, Payload=1024
  Chunk 1: IS_ENCRYPTED=1, Offset=0x10800, Stored=204872, Payload=204800

CHUNK DATA:
  Chunk 0:
    IV: [16 bytes]
    Ciphertext: [1024 bytes encrypted]
    Auth Tag: [16 bytes GCM tag]
    Padding: [to 4096-byte boundary]
  Chunk 1:
    IV: [16 bytes]
    Ciphertext: [204800 bytes encrypted]
    Auth Tag: [16 bytes GCM tag]
    Padding: [to 4096-byte boundary]
```

## 22.4 Chunked Large File

A 5 GB video file split into 16 MB chunks:

```
FILE INDEX:
  Entry 0: Name="video.mp4", Size=5368709120, Chunks=320, First Chunk=0

CHUNK TABLE:
  Chunk 0: Entry=0, Index=0, Offset=0x10000, Payload=16777216, Compressed=16777216
  Chunk 1: Entry=0, Index=1, Offset=0x1100000, Payload=16777216, Compressed=16777216
  ...
  Chunk 319: Entry=0, Index=319, Offset=0x???000, Payload=0, Compressed=0

Total chunks: 320 × 16 MB = 5120 MB = 5 GB
```

## 22.5 Metadata Example

```
METADATA SECTION:
  Tag 0x0001: "Project Archive"
  Tag 0x0002: "2026-06-29T10:30:00Z"
  Tag 0x0003: "2026-06-29T15:45:00Z"
  Tag 0x0004: "John Doe"
  Tag 0x0005: "Complete project archive with source code and assets"
  Tag 0x0006: "project,archive,v1.0,release"
  Tag 0x0007: "Created for the v1.0 release milestone"
  Tag 0x0008: "en"
  Tag 0x0009: "BOX Writer"
  Tag 0x000A: "1.0.0"
  Tag 0x000B: "application/box"
  Tag 0x000C: [256×256 JPEG thumbnail, 12 KB]
```

## 22.6 Folder Path Reconstruction

Given this folder tree:

```
Entry 0: ID=0, Parent=0xFFFFFFFF, Name="root"
Entry 1: ID=1, Parent=0, Name="home"
Entry 2: ID=2, Parent=1, Name="user"
Entry 3: ID=3, Parent=2, Name="documents"
Entry 4: ID=4, Parent=2, Name="projects"
Entry 5: ID=5, Parent=4, Name="box"
```

Path reconstruction for Entry 5:

1. Entry 5: name="box", parent=4
2. Entry 4: name="projects", parent=2
3. Entry 2: name="user", parent=1
4. Entry 1: name="home", parent=0
5. Entry 0: name="root", parent=0xFFFFFFFF (root)

Path: `/root/home/user/projects/box`

---

# Appendix A — Numeric Identifiers

## A.1 Section Type IDs

| ID | ASCII | Description |
|----|-------|-------------|
| 0x4D455441 | META | Metadata |
| 0x46545245 | FTRE | Folder Tree |
| 0x46494458 | FIDX | File Index |
| 0x43544142 | CTAB | Chunk Table |

## A.2 Entry ID Ranges

| Range | Usage |
|-------|-------|
| 0x00000000 | Root folder |
| 0x00000001–0x7FFFFFFF | Folder entries |
| 0x80000000–0xFFFFFFFF | File entries |

## A.3 Tag ID Ranges

| Range | Usage |
|-------|-------|
| 0x0001–0x000D | Reserved (standard tags) |
| 0x000E–0x7FFF | User-defined tags |
| 0x8000–0xFFFF | Extension-defined tags |

---

# Appendix B — Registered Compression Algorithms

| ID | Algorithm | Reference |
|----|-----------|-----------|
| 0x00000000 | None | — |
| 0x00000001 | LZ4 | lz4.org |
| 0x00000002 | Zstandard | facebook.github.io/zstd |
| 0x00000003 | Brotli | brotli.org |
| 0x00000004 | Deflate | RFC 1951 |
| 0x00000005 | LZMA | — |
| 0x00000006 | Zlib | RFC 1950 |

---

# Appendix C — Registered Encryption Algorithms

| ID | Algorithm | Reference |
|----|-----------|-----------|
| 0x00000000 | None | — |
| 0x00000001 | AES-256-GCM | NIST SP 800-38D |
| 0x00000002 | AES-256-CBC | NIST SP 800-38A |
| 0x00000003 | ChaCha20-Poly1305 | RFC 8439 |
| 0x00000004 | XChaCha20-Poly1305 | — |

---

# Appendix D — Implementation Notes

## D.1 Minimum Reader Requirements

A conforming BOX Reader MUST:

1. Read and validate the Header.
2. Parse the File Index for directory listings.
3. Read file data via the Chunk Table and Chunks.
4. Skip unknown sections gracefully.
5. Verify checksums when present.

## D.2 Minimum Writer Requirements

A conforming BOX Writer MUST:

1. Write a valid Header with correct offsets.
2. Write Section Frames with type, size, and CRC-32.
3. Assign Entry IDs sequentially.
4. Store chunks at aligned offsets.
5. Write a valid Footer.

## D.3 Endianness

All multi-byte integers are little-endian. This is non-negotiable.

## D.4 Character Encoding

All strings are UTF-8 (RFC 3629). No null terminators are used.

## D.5 Conformance Levels

| Level | Requirements |
|-------|-------------|
| Basic | Read/write files without compression or encryption |
| Standard | + Compression, folder support |
| Advanced | + Encryption, signatures, thumbnails |
| Full | + Streaming, recovery, search |

---

**End of Specification**

*BOX File Format Standard v1.0 — 2026-06-29*
