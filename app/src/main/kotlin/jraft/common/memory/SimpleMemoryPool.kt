package jraft.common.memory

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

class SimpleMemoryPool(
    sizeBytes: Long,
    private val strict: Boolean,
) : MemoryPool {
    private val availableMemory = AtomicLong(sizeBytes)

    override fun tryAllocate(sizeBytes: Int): ByteBuffer? {
        val needs = if (strict) sizeBytes else 1

        var success = false
        while (true) {
            val available = availableMemory.get()
            if (available < needs) {
                break
            }

            success = availableMemory.compareAndSet(available, available - needs)
            if (success) break
        }
        if (success.not()) return null

        return ByteBuffer.allocate(sizeBytes)
    }

    override fun release(buffer: ByteBuffer) {
        availableMemory.addAndGet(buffer.capacity().toLong())
    }
}
