package jraft.common.utils

import kotlin.time.Duration

interface Time {
    companion object {
        val SYSTEM = SystemTime()
    }

    fun milliseconds(): Long

    fun sleep(ms: Long)

    fun timer(timeout: Duration): Timer {
        return Timer(time = this, timeoutMs = timeout.inWholeMicroseconds)
    }
}
