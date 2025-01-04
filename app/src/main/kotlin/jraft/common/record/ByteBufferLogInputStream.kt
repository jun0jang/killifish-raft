package jraft.common.record

import java.nio.ByteBuffer

class ByteBufferLogInputStream(
    private val byteBuffer: ByteBuffer,
) : LogInputStream {
    override fun nextBatch(): RecordBatch? {
        val remaining = byteBuffer.remaining()
        if (remaining < Records.LOG_OVERHEAD) {
            return null
        }

        val batchSize = Records.LOG_OVERHEAD + byteBuffer.getInt(byteBuffer.position() + Records.SIZE_OFFSET)
        if (remaining < batchSize) {
            return null
        }

        val batchSlice = byteBuffer.slice()
        batchSlice.limit(batchSize)
        byteBuffer.position(byteBuffer.position() + batchSize)

        return DefaultRecordBatch(batchSlice)
    }
}
