package jraft.common.errors

class DisconnectException(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    companion object {
        val INSTANCE = DisconnectException()
    }
}
