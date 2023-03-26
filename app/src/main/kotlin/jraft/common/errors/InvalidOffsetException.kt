package jraft.common.errors

open class InvalidOffsetException(
    message: String? = null,
    cause: Throwable? = null,
) : ApiException(
    message = message,
    cause = cause,
)
