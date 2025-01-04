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
            timestamp: Long = RecordBatch.NO_TIMESTAMP,
            headers: List<Header> = emptyList(),
        ): SimpleRecord {
            return SimpleRecord(
                key = key,
                value = value,
                timestamp = timestamp,
                headers = headers,
            )
        }

        fun new(
            key: ByteArray?,
            value: ByteArray?,
            timestamp: Long = RecordBatch.NO_TIMESTAMP,
            headers: List<Header> = emptyList(),
        ): SimpleRecord {
            return SimpleRecord(
                key = key?.let { ByteBuffer.wrap(it) },
                value = value?.let { ByteBuffer.wrap(it) },
                timestamp = timestamp,
                headers = headers,
            )
        }
    }
}
