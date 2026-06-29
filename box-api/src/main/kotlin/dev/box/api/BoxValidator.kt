package dev.box.api

import dev.box.core.model.*

/**
 * BOX Validator interface.
 * Validates .box file integrity.
 */
interface BoxValidator {
    fun validate(path: String): BoxResult<ValidationResult>
    fun validateHeader(path: String): BoxResult<Boolean>
    fun validateChecksums(path: String): BoxResult<Boolean>
}

/**
 * Validation result.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorCount: Int,
    val warningCount: Int,
    val errors: List<ValidationError> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList()
)

data class ValidationError(
    val code: ErrorCode,
    val message: String,
    val offset: Long = -1
)

data class ValidationWarning(
    val code: ErrorCode,
    val message: String,
    val offset: Long = -1
)
