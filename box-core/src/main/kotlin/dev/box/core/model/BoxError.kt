package dev.box.core.model

/**
 * Error severity levels per BOX SDK spec.
 */
enum class ErrorSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Error codes for BOX operations.
 */
enum class ErrorCode(
    val code: Int,
    val userMessage: String,
    val severity: ErrorSeverity = ErrorSeverity.ERROR,
    val recoverable: Boolean = false
) {
    // General (1000-1005)
    UNKNOWN(1000, "An unknown error occurred", ErrorSeverity.ERROR, false),
    NOT_IMPLEMENTED(1001, "This feature is not implemented", ErrorSeverity.WARNING, false),
    INVALID_ARGUMENT(1002, "Invalid argument provided", ErrorSeverity.ERROR, false),
    INVALID_STATE(1003, "Operation not valid in current state", ErrorSeverity.ERROR, false),
    OUT_OF_MEMORY(1004, "Insufficient memory", ErrorSeverity.CRITICAL, false),
    CANCELLED(1005, "Operation was cancelled", ErrorSeverity.INFO, true),

    // File/IO (2000-2006)
    FILE_NOT_FOUND(2000, "File not found", ErrorSeverity.ERROR, false),
    PERMISSION_DENIED(2001, "Permission denied", ErrorSeverity.ERROR, false),
    DISK_FULL(2002, "Disk is full", ErrorSeverity.CRITICAL, false),
    FILE_EXISTS(2003, "File already exists", ErrorSeverity.ERROR, false),
    PATH_TOO_LONG(2004, "File path too long", ErrorSeverity.ERROR, false),
    IO_FAILED(2005, "I/O operation failed", ErrorSeverity.ERROR, true),
    TOO_MANY_OPEN_FILES(2006, "Too many open files", ErrorSeverity.ERROR, true),

    // Format (3000-3007)
    INVALID_MAGIC(3000, "Not a valid BOX file", ErrorSeverity.ERROR, false),
    UNSUPPORTED_VERSION(3001, "Unsupported format version", ErrorSeverity.ERROR, false),
    INVALID_HEADER(3002, "Corrupted header", ErrorSeverity.ERROR, false),
    INVALID_SECTION(3003, "Corrupted section", ErrorSeverity.ERROR, false),
    INVALID_CHUNK(3004, "Corrupted chunk", ErrorSeverity.ERROR, false),
    INVALID_INDEX(3005, "Corrupted index", ErrorSeverity.ERROR, false),
    INVALID_CHECKSUM(3006, "Checksum mismatch", ErrorSeverity.ERROR, false),
    INVALID_SIGNATURE(3007, "Invalid signature", ErrorSeverity.ERROR, false),

    // Operation (4000-4008)
    ENTRY_NOT_FOUND(4000, "Entry not found", ErrorSeverity.ERROR, false),
    ENTRY_EXISTS(4001, "Entry already exists", ErrorSeverity.ERROR, false),
    ENTRY_IS_DIRECTORY(4002, "Expected file, got folder", ErrorSeverity.ERROR, false),
    ENTRY_IS_FILE(4003, "Expected folder, got file", ErrorSeverity.ERROR, false),
    CONTAINER_FULL(4004, "Container cannot accept more entries", ErrorSeverity.ERROR, false),
    CONTAINER_CLOSED(4005, "Container is closed", ErrorSeverity.ERROR, false),
    READ_ONLY(4006, "Container is read-only", ErrorSeverity.ERROR, false),
    STREAM_CLOSED(4007, "Stream is closed", ErrorSeverity.ERROR, false),
    SEEK_FAILED(4008, "Seek operation failed", ErrorSeverity.ERROR, true);

    companion object {
        fun fromCode(code: Int): ErrorCode? = entries.find { it.code == code }
    }
}

/**
 * BOX error with code, message, severity, recoverability, and optional cause.
 */
data class BoxError(
    val code: ErrorCode,
    override val message: String,
    override val cause: Throwable? = null,
    val source: String = "",
    val context: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) : Exception(message, cause) {

    val userMessage: String get() = code.userMessage
    val severity: ErrorSeverity get() = code.severity
    val recoverable: Boolean get() = code.recoverable

    override fun toString(): String =
        "BoxError(${code.name}, code=${code.code}, severity=$severity, recoverable=$recoverable, message=$message)"
}
