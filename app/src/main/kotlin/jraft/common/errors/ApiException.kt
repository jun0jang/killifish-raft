package jraft.common.errors

open class ApiException(
    message: String? = null,
    cause: Throwable? = null,
) : KafkaException(message, cause)
