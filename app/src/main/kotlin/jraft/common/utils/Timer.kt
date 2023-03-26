package jraft.common.utils

class Timer(
    val time: Time,
    var timeoutMs: Long,
) {
    private var currentTimeMs = time.milliseconds()
    private var startMs = currentTimeMs
    private var deadlineMs = currentTimeMs + timeoutMs

    fun update() {
        update(time.milliseconds())
    }

    fun update(currentTimeMs: Long) {
        this.currentTimeMs = maxOf(currentTimeMs, this.currentTimeMs)
    }

    fun remainingMs(): Long {
        return deadlineMs - currentTimeMs
    }

    fun isExpired(): Boolean {
        return currentTimeMs >= deadlineMs
    }

    fun notExpired(): Boolean {
        return !isExpired()
    }

    fun reset(timeoutMs: Long) {
        this.timeoutMs = timeoutMs
        this.startMs = currentTimeMs

        if (currentTimeMs > Long.MAX_VALUE - timeoutMs) {
            this.deadlineMs = Long.MAX_VALUE
        } else {
            this.deadlineMs = currentTimeMs + timeoutMs
        }
    }
}
