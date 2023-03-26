package jraft.common.errors

class UnknownServerException(
    message: String,
) : ApiException(
    message = message,
)
