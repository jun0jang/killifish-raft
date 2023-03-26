package jraft.common.errors

class UnsupportedVersionException(
    message: String,
    cause: Throwable? = null,
) : ApiException(
    message = message,
    cause = cause,
)
