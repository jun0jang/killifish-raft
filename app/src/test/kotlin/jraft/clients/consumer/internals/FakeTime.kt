package jraft.clients.consumer.internals

import jraft.common.utils.Time
import java.util.concurrent.atomic.AtomicLong

/**
 * cc kafka's MockTime
 */
class FakeTime(
    timeMs: Long = System.currentTimeMillis(),
    private val autoTickMs: Long = 0,
    private val listeners: MutableList<Listener> = mutableListOf(),
) : Time {
    private var timeMs = AtomicLong(timeMs)

    interface Listener {
        fun onTimeUpdated()
    }

    override fun milliseconds(): Long {
        maybeSleep(autoTickMs)
        return timeMs.get()
    }

    override fun sleep(ms: Long) {
        timeMs.addAndGet(ms)
        tick()
    }

    private fun maybeSleep(ms: Long) {
        if (ms > 0) {
            sleep(ms)
        }
    }

    private fun tick() {
        listeners.forEach { it.onTimeUpdated() }
    }
}
