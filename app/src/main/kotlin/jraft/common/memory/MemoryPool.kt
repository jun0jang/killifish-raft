package jraft.common.memory

import java.nio.ByteBuffer

interface MemoryPool {
    fun tryAllocate(sizeBytes: Int): ByteBuffer?

    fun release(buffer: ByteBuffer)
}
