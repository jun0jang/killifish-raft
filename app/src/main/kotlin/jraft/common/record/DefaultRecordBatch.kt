package jraft.common.record

import jraft.common.log.compress.CompressionType
import java.nio.ByteBuffer
import kotlin.experimental.or

class DefaultRecordBatch(
    private val buffer: ByteBuffer,
) : RecordBatch {

    override fun isValid(): Boolean {
        return sizeInBytes() >= RECORD_BATCH_OVERHEAD
    }

    override fun baseOffset(): Long {
        return buffer.getLong(BASE_OFFSET_OFFSET)
    }

    override fun lastOffset(): Long {
        return baseOffset() + buffer.getInt(LAST_OFFSET_DELTA_OFFSET)
    }

    override fun magic(): Long {
        return buffer.get(MAGIC_OFFSET).toLong()
    }

    override fun producerId(): Long {
        return buffer.getLong(PRODUCER_ID_OFFSET)
    }

    override fun producerEpoch(): Long {
        return buffer.getShort(PRODUCER_EPOCH_OFFSET).toLong()
    }

    override fun hasProducerId(): Boolean {
        return producerId() > RecordBatch.NO_PRODUCER_ID
    }

    override fun baseSequence(): Int {
        return buffer.getInt(BASE_SEQUENCE_OFFSET)
    }

    override fun sizeInBytes(): Int {
        return Records.LOG_OVERHEAD + buffer.getInt(LENGTH_OFFSET)
    }

    override fun lastSequence(): Int {
        val baseSequence = baseSequence()
        if (baseSequence == RecordBatch.NO_SEQUENCE) {
            return RecordBatch.NO_SEQUENCE
        }

        return baseSequence() + buffer.getInt(LAST_OFFSET_DELTA_OFFSET)
    }

    override fun count(): Int {
        return buffer.getInt(RECORDS_COUNT_OFFSET)
    }

    override fun nextOffset(): Long {
        return lastOffset() + 1
    }

    override fun writeTo(buffer: ByteBuffer) {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<Record> {
        val buffer = this.buffer.duplicate()
        buffer.position(RECORDS_OFFSET)

        return RecordIterator(
            buffer = buffer,
            logAppendTime = null,
            baseOffset = baseOffset(),
            baseTimestamp = buffer.getLong(BASE_TIMESTAMP_OFFSET),
            baseSequence = baseSequence(),
            numRecords = count(),
        )
    }

    companion object {
        const val BASE_OFFSET_OFFSET: Int = 0
        const val BASE_OFFSET_LENGTH: Int = 8
        const val LENGTH_OFFSET: Int = BASE_OFFSET_OFFSET + BASE_OFFSET_LENGTH
        const val LENGTH_LENGTH: Int = 4
        const val PARTITION_LEADER_EPOCH_OFFSET: Int = LENGTH_OFFSET + LENGTH_LENGTH
        const val PARTITION_LEADER_EPOCH_LENGTH: Int = 4
        const val MAGIC_OFFSET: Int = PARTITION_LEADER_EPOCH_OFFSET + PARTITION_LEADER_EPOCH_LENGTH
        const val MAGIC_LENGTH: Int = 1
        const val CRC_OFFSET: Int = MAGIC_OFFSET + MAGIC_LENGTH
        const val CRC_LENGTH: Int = 4
        const val ATTRIBUTES_OFFSET: Int = CRC_OFFSET + CRC_LENGTH
        const val ATTRIBUTE_LENGTH: Int = 2
        const val LAST_OFFSET_DELTA_OFFSET: Int = ATTRIBUTES_OFFSET + ATTRIBUTE_LENGTH
        const val LAST_OFFSET_DELTA_LENGTH: Int = 4
        const val BASE_TIMESTAMP_OFFSET: Int = LAST_OFFSET_DELTA_OFFSET + LAST_OFFSET_DELTA_LENGTH
        const val BASE_TIMESTAMP_LENGTH: Int = 8
        const val MAX_TIMESTAMP_OFFSET: Int = BASE_TIMESTAMP_OFFSET + BASE_TIMESTAMP_LENGTH
        const val MAX_TIMESTAMP_LENGTH: Int = 8
        const val PRODUCER_ID_OFFSET: Int = MAX_TIMESTAMP_OFFSET + MAX_TIMESTAMP_LENGTH
        const val PRODUCER_ID_LENGTH: Int = 8
        const val PRODUCER_EPOCH_OFFSET: Int = PRODUCER_ID_OFFSET + PRODUCER_ID_LENGTH
        const val PRODUCER_EPOCH_LENGTH: Int = 2
        const val BASE_SEQUENCE_OFFSET: Int = PRODUCER_EPOCH_OFFSET + PRODUCER_EPOCH_LENGTH
        const val BASE_SEQUENCE_LENGTH: Int = 4
        const val RECORDS_COUNT_OFFSET: Int = BASE_SEQUENCE_OFFSET + BASE_SEQUENCE_LENGTH
        const val RECORDS_COUNT_LENGTH: Int = 4
        const val RECORDS_OFFSET: Int = RECORDS_COUNT_OFFSET + RECORDS_COUNT_LENGTH
        const val RECORD_BATCH_OVERHEAD: Int = RECORDS_OFFSET

        private const val COMPRESSION_CODEC_MASK: Byte = 0x07
        private const val TRANSACTIONAL_FLAG_MASK: Byte = 0x10
        private const val CONTROL_FLAG_MASK: Int = 0x20
        private const val DELETE_HORIZON_FLAG_MASK: Byte = 0x40
        private const val TIMESTAMP_TYPE_MASK: Byte = 0x08

        fun writeHeader(
            buffer: ByteBuffer,
            baseOffset: Long,
            lastOffsetDelta: Int,
            sizeInBytes: Int,
            magic: Byte,
            timestampType: TimestampType,
            baseTimestamp: Long,
            maxTimestamp: Long,
            producerId: Long,
            epoch: Short,
            sequence: Int,
            isTransactional: Boolean,
            isControlBatch: Boolean,
            isDeleteHorizonSet: Boolean,
            partitionLeaderEpoch: Int,
            numRecords: Int,
        ) {
            if (magic < RecordBatch.CURRENT_MAGIC_VALUE) {
                throw IllegalArgumentException("Invalid magic value $magic")
            }
            val attributes = computeAttributes(
                CompressionType.NONE,
                timestampType,
                isTransactional,
                isControlBatch,
                isDeleteHorizonSet,
            )

            val position = buffer.position()
            buffer.putLong(position + BASE_OFFSET_OFFSET, baseOffset)
            buffer.putInt(position + LENGTH_OFFSET, sizeInBytes - Records.LOG_OVERHEAD)
            buffer.putInt(position + PARTITION_LEADER_EPOCH_OFFSET, partitionLeaderEpoch)
            buffer.put(position + MAGIC_OFFSET, magic)
            buffer.putShort(position + ATTRIBUTES_OFFSET, attributes.toShort())
            buffer.putLong(position + BASE_TIMESTAMP_OFFSET, baseTimestamp)
            buffer.putLong(position + MAX_TIMESTAMP_OFFSET, maxTimestamp)
            buffer.putInt(position + LAST_OFFSET_DELTA_OFFSET, lastOffsetDelta)
            buffer.putLong(position + PRODUCER_ID_OFFSET, producerId)
            buffer.putShort(position + PRODUCER_EPOCH_OFFSET, epoch)
            buffer.putInt(position + BASE_SEQUENCE_OFFSET, sequence)
            buffer.putInt(position + RECORDS_COUNT_OFFSET, numRecords)
            buffer.putInt(position + CRC_OFFSET, 0) // TODO
            buffer.position(position + RECORD_BATCH_OVERHEAD)
        }

        private fun computeAttributes(
            type: CompressionType,
            timestampType: TimestampType,
            isTransactional: Boolean,
            isControl: Boolean,
            isDeleteHorizonSet: Boolean,
        ): Byte {
            var attributes = if (isTransactional) TRANSACTIONAL_FLAG_MASK else 0

            if (isControl) {
                attributes = attributes.or(CONTROL_FLAG_MASK.toByte())
            }
            if (type.id > 0) {
                attributes = attributes.or(type.id.toByte())
            }
            if (timestampType == TimestampType.LOG_APPEND_TIME) {
                attributes = attributes.or(TIMESTAMP_TYPE_MASK)
            }
            if (isDeleteHorizonSet) {
                attributes = attributes.or(DELETE_HORIZON_FLAG_MASK)
            }
            return attributes
        }
    }

    private fun attributes(): Byte {
        // note we're not using the second byte of attributes
        // short.toByte() == (byte1 byte2) -> byte2
        return buffer.getShort(ATTRIBUTES_OFFSET).toByte()
    }
}
