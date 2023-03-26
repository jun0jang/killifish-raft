package jraft.common

import java.util.concurrent.atomic.AtomicBoolean

class IdempotentCloser(
    private val isClosed: AtomicBoolean = AtomicBoolean(false),
) : AutoCloseable {

    override fun close() {
        close(null, null)
    }

    fun close(onInitialClose: Runnable?, onSubsequentClose: Runnable?) {
        if (isClosed.compareAndSet(false, true)) {
            onInitialClose?.run()
        } else {
            onSubsequentClose?.run()
        }
    }
}
