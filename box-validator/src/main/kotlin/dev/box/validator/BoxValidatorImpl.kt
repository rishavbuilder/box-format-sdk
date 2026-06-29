package dev.box.validator

import dev.box.api.BoxValidator
import dev.box.api.ValidationError
import dev.box.api.ValidationResult
import dev.box.api.ValidationWarning
import dev.box.core.model.BoxResult
import dev.box.core.model.ErrorCode
import dev.box.parser.BoxParser
import java.io.File
import java.io.RandomAccessFile

/**
 * Default BOX Validator implementation.
 */
class BoxValidatorImpl : BoxValidator {

    override fun validate(path: String): BoxResult<ValidationResult> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "File not found: $path")
            }

            val raf = RandomAccessFile(file, "r")
            val parser = BoxParser(raf)

            val errors = mutableListOf<ValidationError>()
            val warnings = mutableListOf<ValidationWarning>()

            // Validate magic bytes
            val headerResult = parser.readHeader()
            if (headerResult.isFailure()) {
                raf.close()
                return BoxResult.failure(headerResult.error())
            }

            val header = headerResult.value()

            // Check magic bytes
            if (!header.headerEndMarker.contentEquals(dev.box.core.constants.BoxConstants.HEADER_END_MARKER)) {
                warnings.add(ValidationWarning(
                    code = ErrorCode.INVALID_HEADER,
                    message = "Header end marker mismatch",
                    offset = 0xF8
                ))
            }

            // Validate header checksum
            val checksumResult = parser.validateHeaderChecksum(header)
            if (checksumResult.isFailure()) {
                errors.add(ValidationError(
                    code = ErrorCode.INVALID_CHECKSUM,
                    message = "Header checksum mismatch",
                    offset = 0x80
                ))
            }

            // Validate footer
            val footerResult = parser.readFooter(header.footerOffset)
            if (footerResult.isFailure()) {
                errors.add(ValidationError(
                    code = ErrorCode.INVALID_HEADER,
                    message = "Failed to read footer: ${footerResult.error().message}",
                    offset = header.footerOffset.toLong()
                ))
            } else {
                val footer = footerResult.value()
                if (footer.containerSize != file.length()) {
                    warnings.add(ValidationWarning(
                        code = ErrorCode.INVALID_HEADER,
                        message = "Container size mismatch: expected ${footer.containerSize}, actual ${file.length()}",
                        offset = header.footerOffset.toLong()
                    ))
                }
            }

            // Validate section offsets
            if (header.metadataOffset > 0 && header.metadataSize > 0) {
                val metaFrameResult = parser.readSectionFrame(header.metadataOffset)
                if (metaFrameResult.isFailure()) {
                    errors.add(ValidationError(
                        code = ErrorCode.INVALID_SECTION,
                        message = "Failed to read metadata section frame",
                        offset = header.metadataOffset.toLong()
                    ))
                }
            }

            if (header.folderTreeOffset > 0 && header.folderTreeSize > 0) {
                val frameResult = parser.readSectionFrame(header.folderTreeOffset)
                if (frameResult.isFailure()) {
                    errors.add(ValidationError(
                        code = ErrorCode.INVALID_SECTION,
                        message = "Failed to read folder tree section frame",
                        offset = header.folderTreeOffset.toLong()
                    ))
                }
            }

            if (header.fileIndexOffset > 0 && header.fileIndexSize > 0) {
                val frameResult = parser.readSectionFrame(header.fileIndexOffset)
                if (frameResult.isFailure()) {
                    errors.add(ValidationError(
                        code = ErrorCode.INVALID_SECTION,
                        message = "Failed to read file index section frame",
                        offset = header.fileIndexOffset.toLong()
                    ))
                }
            }

            if (header.chunkTableOffset > 0 && header.chunkTableSize > 0) {
                val frameResult = parser.readSectionFrame(header.chunkTableOffset)
                if (frameResult.isFailure()) {
                    errors.add(ValidationError(
                        code = ErrorCode.INVALID_SECTION,
                        message = "Failed to read chunk table section frame",
                        offset = header.chunkTableOffset.toLong()
                    ))
                }
            }

            // Validate entry counts
            val expectedEntryCount = header.folderEntryCount + header.fileEntryCount
            if (header.entryCount != expectedEntryCount) {
                warnings.add(ValidationWarning(
                    code = ErrorCode.INVALID_INDEX,
                    message = "Entry count mismatch: header says ${header.entryCount}, expected $expectedEntryCount",
                    offset = 0x24
                ))
            }

            raf.close()

            BoxResult.success(ValidationResult(
                isValid = errors.isEmpty(),
                errorCount = errors.size,
                warningCount = warnings.size,
                errors = errors,
                warnings = warnings
            ))
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Validation failed: ${e.message}", e)
        }
    }

    override fun validateHeader(path: String): BoxResult<Boolean> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "File not found: $path")
            }

            val raf = RandomAccessFile(file, "r")
            val parser = BoxParser(raf)

            val headerResult = parser.readHeader()
            raf.close()

            if (headerResult.isFailure()) {
                return BoxResult.failure(headerResult.error())
            }

            val header = headerResult.value()
            val checksumResult = parser.validateHeaderChecksum(header)

            if (checksumResult.isFailure()) {
                return BoxResult.failure(checksumResult.error())
            }

            BoxResult.success(true)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Header validation failed: ${e.message}", e)
        }
    }

    override fun validateChecksums(path: String): BoxResult<Boolean> {
        return try {
            val file = File(path)
            if (!file.exists()) {
                return BoxResult.failure(ErrorCode.FILE_NOT_FOUND, "File not found: $path")
            }

            val raf = RandomAccessFile(file, "r")
            val parser = BoxParser(raf)

            val headerResult = parser.readHeader()
            if (headerResult.isFailure()) {
                raf.close()
                return BoxResult.failure(headerResult.error())
            }

            val header = headerResult.value()

            // Validate header checksum
            val headerChecksumResult = parser.validateHeaderChecksum(header)
            if (headerChecksumResult.isFailure()) {
                raf.close()
                return BoxResult.failure(headerChecksumResult.error())
            }

            // Validate section CRC-32s
            if (header.metadataSize > 0) {
                val frame = parser.readSectionFrame(header.metadataOffset)
                if (frame.isSuccess()) {
                    val crcResult = parser.validateSectionCrc32(
                        header.metadataOffset,
                        frame.value().sectionSize,
                        frame.value().sectionCrc32
                    )
                    if (crcResult.isFailure()) {
                        raf.close()
                        return BoxResult.failure(crcResult.error())
                    }
                }
            }

            raf.close()
            BoxResult.success(true)
        } catch (e: Exception) {
            BoxResult.failure(ErrorCode.IO_FAILED, "Checksum validation failed: ${e.message}", e)
        }
    }
}
