package jraft.common.record

import jraft.common.header.Header
import java.nio.ByteBuffer

class SimpleRecord(
    val key: ByteBuffer?,
    val value: ByteBuffer?,
    val timestamp: Long,
    val headers: List<Header>,
) {
    companion object {
        fun new(
            key: ByteBuffer?,
            value: ByteBuffer?,
        ): SimpleRecord {
            return SimpleRecord(
                key = key,
                value = value,
                timestamp = RecordBatch.NO_TIMESTAMP,
                headers = emptyList(),
            )
        }
    }
}
