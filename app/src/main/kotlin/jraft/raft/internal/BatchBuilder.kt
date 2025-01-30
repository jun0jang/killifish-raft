package jraft.raft.internal

import jraft.common.protocol.DataOutputStreamWritable
import jraft.common.record.DefaultRecord
import jraft.common.record.DefaultRecordBatch
import jraft.common.record.MemoryRecords
import jraft.common.record.Record
import jraft.common.record.RecordBatch
import jraft.common.record.TimestampType
import jraft.common.serialization.RecordSerde
import jraft.common.utils.ByteBufferOutputStream
import jraft.common.utils.ByteUtils
import java.io.DataOutputStream
import java.nio.ByteBuffer

class BatchBuilder<T> private constructor(
    private val buffer: ByteBuffer,
    private val initialPosition: Int,
    private val serde: RecordSerde<T>,
    private val baseOffset: Long,
    private var nextOffset: Long,
    private var batchOutput: ByteBufferOutputStream,
    private val recordOutput: DataOutputStreamWritable,
    private val appendTime: Long,
    private val leaderEpoch: Int,
    private val maxBytes: Int,
) {
    companion object {
        fun <T> new(
            buffer: ByteBuffer,
            serde: RecordSerde<T>,
            baseOffset: Long,
            appendTime: Long,
            leaderEpoch: Int,
            maxBytes: Int,
        ): BatchBuilder<T> {
            // Header는 압축 되지 않음
            val batchOutput = ByteBufferOutputStream(buffer)
            val initialPosition = buffer.position()
            batchOutput.position(initialPosition + DefaultRecordBatch.RECORD_BATCH_OVERHEAD)
            // 만약 압축 알고리즘을 돌린다면 record 부분만 압축한다. 따라서 output이 나눠짐.
            // batch 부분에 어떤 compression type을 사용하는지 표시 되기 때문에 batch 부분은 압축하지 않음.
            val recordOutput = DataOutputStreamWritable(DataOutputStream(batchOutput))

            return BatchBuilder(
                buffer = buffer,
                initialPosition = initialPosition,
                serde = serde,
                baseOffset = baseOffset,
                nextOffset = baseOffset,
                batchOutput = batchOutput,
                recordOutput = recordOutput,
                appendTime = appendTime,
                leaderEpoch = leaderEpoch,
                maxBytes = maxBytes,
            )
        }
    }

    private val records: MutableList<T> = mutableListOf()

    private var unflushedBytes = 0

    fun baseOffset(): Long {
        return baseOffset
    }

    fun records(): List<T> {
        return records
    }

    fun build(): MemoryRecords {
        recordOutput.close()
        writeHeader()
        val buffer = batchOutput.buffer().duplicate()
        buffer.flip()
        buffer.position(initialPosition)
        return MemoryRecords.readableRecords(buffer)
    }

    fun isEmpty(): Boolean {
        return records.isEmpty()
    }

    fun bytesNeeded(records: List<T>): Int? {
        val bytesNeeded = bytesNeededForRecords(records)

        val approxUnusedSizeInBytes = maxBytes - approximateSizeInBytes()
        if (approxUnusedSizeInBytes >= bytesNeeded) {
            return null
        }
        if (unflushedBytes > 0) {
            recordOutput.flush()
            unflushedBytes = 0
            val unusedSizeInBytes = maxBytes - flushedSizeInBytes()
            if (unusedSizeInBytes >= bytesNeeded) {
                return null
            }
        }

        return DefaultRecordBatch.RECORD_BATCH_OVERHEAD + bytesNeeded
    }

    fun appendRecord(record: T): Long {
        val offset = nextOffset++

        val recordSizeInBytes = writeRecord(offset.toInt(), record)
        unflushedBytes += recordSizeInBytes
        records.add(record)

        return offset
    }

    private fun writeHeader() {
        val buffer = batchOutput.buffer()
        val lastPosition = buffer.position()

        buffer.position(initialPosition)
        val size = lastPosition - initialPosition
        val lastOffsetDelta: Int = (nextOffset - 1 - baseOffset).toInt()

        DefaultRecordBatch.writeHeader(
            buffer = buffer,
            baseOffset = baseOffset,
            lastOffsetDelta = lastOffsetDelta,
            sizeInBytes = size,
            magic = RecordBatch.MAGIC_VALUE_V2,
            TimestampType.CREATE_TIME,
            appendTime,
            appendTime,
            RecordBatch.NO_PRODUCER_ID,
            RecordBatch.NO_PRODUCER_EPOCH,
            RecordBatch.NO_SEQUENCE,
            false,
            false,
            false,
            leaderEpoch,
            (nextOffset - baseOffset).toInt(),
        )

        buffer.position(lastPosition)
    }

    private fun writeRecord(offset: Int, record: T): Int {
        val offsetDelta = offset - baseOffset
        val timestampDelta = 0L

        val payloadSize = serde.recordSize(record)
        val sizeInBytes = DefaultRecord.sizeOfBodyInBytes(
            offsetDelta = offsetDelta.toInt(),
            timestampDelta = timestampDelta,
            keySize = -1,
            valueSize = payloadSize,
            headers = Record.EMPTY_HEADERS,
        )
        recordOutput.writeVarint(sizeInBytes)
        // Write attributes (currently unused)
        recordOutput.writeByte(0.toByte())

        // Write timestamp and offset
        recordOutput.writeVarlong(timestampDelta)
        recordOutput.writeVarint(offsetDelta.toInt())

        // Write key, which is always null for controller messages
        recordOutput.writeVarint(-1)

        // Write value
        recordOutput.writeVarint(payloadSize)
        serde.write(record, recordOutput)

        // Write headers (currently unused)
        recordOutput.writeVarint(0)
        return ByteUtils.sizeOfVarint(sizeInBytes) + sizeInBytes
    }

    /**
     * estimated size in bytes of the appended records if no compression is in use.
     */
    private fun approximateSizeInBytes(): Int {
        return flushedSizeInBytes() + unflushedBytes
    }

    private fun flushedSizeInBytes(): Int {
        return batchOutput.position() - initialPosition
    }

    private fun bytesNeededForRecords(records: List<T>): Int {
        var expectedNextOffset = nextOffset
        var bytesNeeded = 0

        for (record in records) {
            val recordSizeInBytes = DefaultRecord.sizeOfBodyInBytes(
                offsetDelta = (expectedNextOffset - baseOffset).toInt(),
                0,
                -1,
                serde.recordSize(record),
                Record.EMPTY_HEADERS,
            )

            bytesNeeded += ByteUtils.sizeOfVarint(recordSizeInBytes)
            bytesNeeded += recordSizeInBytes

            expectedNextOffset += 1
        }

        return bytesNeeded
    }
}
