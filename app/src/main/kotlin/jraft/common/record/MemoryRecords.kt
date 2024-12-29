package jraft.common.record

import java.nio.ByteBuffer

class MemoryRecords(
    private val byteBuffer: ByteBuffer,
) {
    fun batches(): Iterable<RecordBatch> {
        return emptyList()
    }
}
