package jraft.common.record

import jraft.common.header.Header
import jraft.common.utils.ByteBufferOutputStream
import jraft.common.utils.Utils
import java.io.DataOutputStream
import java.nio.ByteBuffer

class MemoryRecordsBuilder private constructor(
    private val bufferStream: ByteBufferOutputStream,
    private val magic: Byte,
    private val timestampType: TimestampType,
    private val baseOffset: Long,
    private val logAppendTime: Long,
    private val producerId: Long,
    private val producerEpoch: Short,
    private val baseSequence: Int,
    private val isTransactional: Boolean,
    private val isControlBatch: Boolean,
    private val partitionLeaderEpoch: Int,
    private val writeLimit: Int,
    private val deleteHorizonMs: Long,
    private val initialPosition: Int,
    private val batchHeaderSizeInBytes: Int,
    private var maxTimestamp: Long,
    private var baseTimestamp: Long?,
    private val appendStream: DataOutputStream,
) {
    companion object {
        fun new(
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
            deleteHorizonMs: Long = RecordBatch.NO_TIMESTAMP,
        ): MemoryRecordsBuilder {
            val initialPosition = buffer.position()
            val batchHeaderSizeInBytes = DefaultRecordBatch.RECORD_BATCH_OVERHEAD
            buffer.position(initialPosition + batchHeaderSizeInBytes)

            return MemoryRecordsBuilder(
                bufferStream = ByteBufferOutputStream(buffer),
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
                writeLimit = buffer.remaining(),
                deleteHorizonMs = deleteHorizonMs,
                initialPosition = initialPosition,
                batchHeaderSizeInBytes = batchHeaderSizeInBytes,
                maxTimestamp = RecordBatch.NO_TIMESTAMP,
                baseTimestamp = if (deleteHorizonMs >= 0) {
                    deleteHorizonMs
                } else {
                    null
                },
                appendStream = DataOutputStream(ByteBufferOutputStream(buffer)),
            )
        }
    }

    private var lastOffset: Long? = null

    private var numRecords: Int = 0

    private var recordSizeInByte: Int = 0

    private var builtRecords: MemoryRecords? = null

    fun build(): MemoryRecords {
        if (builtRecords != null) {
            return builtRecords!!
        }
        if (numRecords == 0) {
            return MemoryRecords.EMPTY
        }

        writeDefaultBatchHeader()
        val buffer = bufferStream.buffer().duplicate()
        buffer.flip()
        buffer.position(initialPosition)

        builtRecords = MemoryRecords.readableRecords(buffer.slice())
        return builtRecords!!
    }

    private fun writeDefaultBatchHeader(): Int {
        val buffer = bufferStream.buffer()
        val pos = buffer.position()
        buffer.position(initialPosition)
        val size = pos - initialPosition
        val writtenCompressed = size - DefaultRecordBatch.RECORD_BATCH_OVERHEAD
        val offsetDelta = lastOffset!! - baseOffset

        val maxTimestamp = if (timestampType == TimestampType.LOG_APPEND_TIME) {
            logAppendTime
        } else {
            maxTimestamp
        }

        DefaultRecordBatch.writeHeader(
            buffer = buffer,
            baseOffset = baseOffset,
            lastOffsetDelta = offsetDelta.toInt(),
            sizeInBytes = size,
            magic = magic,
            timestampType = timestampType,
            baseTimestamp = baseTimestamp!!,
            maxTimestamp = maxTimestamp,
            producerId = producerId,
            epoch = producerEpoch,
            sequence = baseSequence,
            isTransactional = isTransactional,
            isControlBatch = isControlBatch,
            isDeleteHorizonSet = deleteHorizonMs >= 0,
            partitionLeaderEpoch = partitionLeaderEpoch,
            numRecords = numRecords,
        )

        buffer.position(pos)
        return writtenCompressed
    }

    fun append(record: SimpleRecord) {
        append(record.timestamp, record.key, record.value, record.headers)
    }

    fun append(timestamp: Long, key: ByteArray, value: ByteArray) {
        append(timestamp, key, value, Record.EMPTY_HEADERS)
    }

    fun append(timestamp: Long, key: ByteArray?, value: ByteArray?, headers: List<Header>) {
        append(timestamp, Utils.wrapNullable(key), Utils.wrapNullable(value), headers)
    }

    fun append(timestamp: Long, key: ByteBuffer?, value: ByteBuffer?, headers: List<Header>) {
        appendWithOffset(nextSequentialOffset(), timestamp, key, value, headers)
    }

    fun appendWithOffset(offset: Long, timestamp: Long, key: ByteBuffer?, value: ByteBuffer?, headers: List<Header>) {
        appendWithOffset(offset, false, timestamp, key, value, headers)
    }

    private fun appendWithOffset(
        offset: Long,
        isControlRecord: Boolean,
        timestamp: Long,
        key: ByteBuffer?,
        value: ByteBuffer?,
        headers: List<Header>,
    ) {
        if (isControlRecord && this.isControlBatch.not()) {
            throw IllegalArgumentException("Control records can only be appended to control batches")
        }

        if (lastOffset != null && offset <= lastOffset!!) {
            throw IllegalArgumentException("Out of order offset: $offset (last: $lastOffset)")
        }

        if (timestamp < 0 && timestamp != RecordBatch.NO_TIMESTAMP) {
            throw IllegalArgumentException("Invalid timestamp: $timestamp")
        }

        if (baseTimestamp == null) {
            baseTimestamp = timestamp
        }

        val offsetDelta = (offset - baseOffset).toInt()
        val timestampDelta = timestamp - baseTimestamp!!
        val sizeInBytes = DefaultRecord.writeTo(appendStream, offsetDelta, timestampDelta, key, value, headers)
        recordWritten(offset, timestamp, sizeInBytes)
    }

    private fun nextSequentialOffset(): Long {
        lastOffset?.let {
            return it + 1
        }

        return baseOffset
    }

    private fun recordWritten(offset: Long, timestamp: Long, sizeInBytes: Int) {
        numRecords++
        lastOffset = offset
        recordSizeInByte += sizeInBytes

        if (timestamp > maxTimestamp) {
            maxTimestamp = timestamp
        }
    }
}
