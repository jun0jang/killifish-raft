package jraft.common.record

import java.nio.ByteBuffer

class MemoryRecords(
    private val byteBuffer: ByteBuffer,
) {

    private val batches: Iterable<RecordBatch> = Iterable { this.batchIterator() }

    fun batches(): Iterable<RecordBatch> {
        return batches
    }

    fun batchIterator(): RecordBatchIterator {
        return RecordBatchIterator(ByteBufferLogInputStream(byteBuffer.duplicate()))
    }

    companion object {
        val EMPTY = MemoryRecords(ByteBuffer.allocate(0))

        fun readableRecords(buffer: ByteBuffer): MemoryRecords {
            return MemoryRecords(buffer)
        }

        fun builder(
            buffer: ByteBuffer,
            magic: Byte,
            timestampType: TimestampType,
            baseOffset: Long,
            logAppendTime: Long,
            producerId: Long,
            producerEpoch: Short,
            baseSequence: Int,
            isTransactional: Boolean,
            isControlBatch: Boolean,
            partitionLeaderEpoch: Int,
        ): MemoryRecordsBuilder {
            return MemoryRecordsBuilder.new(
                buffer = buffer,
                magic = magic,
                timestampType = timestampType,
                baseOffset = baseOffset,
                logAppendTime = logAppendTime,
                producerId = producerId,
                producerEpoch = producerEpoch,
                baseSequence = baseSequence,
                isTransactional = isTransactional,
                isControlBatch = isControlBatch,
                partitionLeaderEpoch = partitionLeaderEpoch,
            )
        }
    }
}
