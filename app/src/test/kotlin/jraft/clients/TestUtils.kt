package jraft.clients

object TestUtils {
    fun waitForCondition(
        timeoutMs: Long,
        message: String,
        condition: () -> Boolean,
    ) {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (condition()) {
                return
            }
            Thread.sleep(10)
        }
        throw AssertionError(message)
    }
}
