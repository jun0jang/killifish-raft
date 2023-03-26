package jraft.common.log.record

import java.nio.ByteBuffer

class MemoryRecords(
    private val byteBuffer: ByteBuffer,
) {
    fun writeTo() {}

    fun batches() {
        ByteBufferLogInputStream(byteBuffer)

        // from iterator
    }

    class Builder(
        private val byteBuffer: ByteBuffer,
    ) {
        fun append() {}
    }
}
