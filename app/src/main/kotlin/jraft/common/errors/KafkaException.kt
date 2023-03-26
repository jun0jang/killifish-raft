package jraft.common.errors

open class KafkaException(
    override val message: String? = null,
    override val cause: Throwable? = null,
) : RuntimeException(
    message,
    cause,
)
