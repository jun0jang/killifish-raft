package jraft.common.record

import java.nio.ByteBuffer

class RecordIterator(
    private val buffer: ByteBuffer,
    private val logAppendTime: Long?,
    private val baseOffset: Long,
    private val baseTimestamp: Long,
    private val baseSequence: Int,
    private val numRecords: Int,
) : Iterator<Record> {
    private var readRecords: Int = 0

    override fun hasNext(): Boolean {
        return readRecords < numRecords
    }

    override fun next(): Record {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        readRecords++

        val record = DefaultRecord.readFrom(
            buffer = buffer,
            baseOffset = baseOffset,
            baseTimestamp = baseTimestamp,
            baseSequence = baseSequence,
            logAppendTime = logAppendTime,
        )
        if (readRecords == numRecords) {
            require(buffer.remaining() == 0) { "Found more records than expected" }
        }

        return record
    }
}
