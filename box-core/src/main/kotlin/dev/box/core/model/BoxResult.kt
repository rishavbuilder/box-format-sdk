package dev.box.core.model

/**
 * Result type for BOX operations.
 * Either a success value or an error.
 */
sealed class BoxResult<out T> {
    abstract fun isSuccess(): Boolean
    abstract fun isFailure(): Boolean

    fun value(): T = when (this) {
        is Success -> value
        is Failure -> throw IllegalStateException("Called value() on a failure result: ${error.message}", error)
    }

    fun error(): BoxError = when (this) {
        is Success -> throw IllegalStateException("Called error() on a success result")
        is Failure -> error
    }

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    fun <R> map(transform: (T) -> R): BoxResult<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> Failure(error)
    }

    fun <R> flatMap(transform: (T) -> BoxResult<R>): BoxResult<R> = when (this) {
        is Success -> transform(value)
        is Failure -> Failure(error)
    }

    fun recover(default: @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default
    }

    companion object {
        fun <T> success(value: T): BoxResult<T> = Success(value)
        fun <T> failure(error: BoxError): BoxResult<T> = Failure(error)
        fun <T> failure(code: ErrorCode, message: String): BoxResult<T> = Failure(BoxError(code, message))
        fun <T> failure(code: ErrorCode, message: String, cause: Throwable?): BoxResult<T> = Failure(BoxError(code, message, cause))
    }

    class Success<T>(val value: T) : BoxResult<T>() {
        override fun isSuccess() = true
        override fun isFailure() = false
        override fun toString() = "Success($value)"
    }

    class Failure<T>(val error: BoxError) : BoxResult<T>() {
        override fun isSuccess() = false
        override fun isFailure() = true
        override fun toString() = "Failure($error)"
    }
}
