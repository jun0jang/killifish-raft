package jraft.common.errors

class AuthenticationException(
    message: String,
    cause: Throwable? = null,
) : ApiException(
    message = message,
    cause = cause,
)
