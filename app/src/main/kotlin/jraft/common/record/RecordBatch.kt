package jraft.common.record

import java.nio.ByteBuffer

interface RecordBatch {
    companion object {
        const val NO_TIMESTAMP = -1L
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
