package jraft.common

import java.util.concurrent.atomic.AtomicReference

class Utils() {
    private val log = logger<Utils>()

    fun closeQuietly(closeable: AutoCloseable, name: String) {
        try {
            closeable.close()
        } catch (e: Exception) {
            log.warn("Failed to close $name", e)
        }
    }

    fun closeQuietly(closeable: AutoCloseable, name: String, firstException: AtomicReference<Throwable>) {
        try {
            closeable.close()
        } catch (e: Exception) {
            firstException.compareAndSet(null, e)
            log.warn("Failed to close $name", e)
        }
    }
}
