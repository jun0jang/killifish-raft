package jraft.common.utils

class SystemTime : Time {
    override fun milliseconds(): Long {
        return System.currentTimeMillis()
    }

    override fun sleep(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
