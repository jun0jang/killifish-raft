package jraft.common.record

import java.nio.ByteBuffer

interface RecordBatch : Iterable<Record> {
    companion object {
        const val MAGIC_VALUE_V0: Byte = 0
        const val MAGIC_VALUE_V1: Byte = 1
        const val MAGIC_VALUE_V2: Byte = 2

        const val CURRENT_MAGIC_VALUE: Byte = MAGIC_VALUE_V2

        const val NO_TIMESTAMP = -1L

        const val NO_PRODUCER_ID: Long = -1

        const val NO_PRODUCER_EPOCH: Short = -1

        const val NO_SEQUENCE: Int = -1
    }

    fun isValid(): Boolean

    fun baseOffset(): Long

    fun lastOffset(): Long

    fun nextOffset(): Long

    fun magic(): Long

    fun producerId(): Long

    fun producerEpoch(): Long

    fun hasProducerId(): Boolean

    fun baseSequence(): Int

    fun lastSequence(): Int

    fun sizeInBytes(): Int

    fun count(): Int?

    fun writeTo(buffer: ByteBuffer)
}
