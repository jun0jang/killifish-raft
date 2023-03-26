package jraft.common.errors

class FencedInstanceIdException(
    message: String,
    cause: Throwable? = null,
) : ApiException(
    message = message,
    cause = cause,
)
