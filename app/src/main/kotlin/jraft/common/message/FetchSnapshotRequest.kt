package jraft.common.message

import java.nio.ByteBuffer

class FetchSnapshotRequest(
    val endOffset: Long,
) : Message {
    override fun toByteBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(8)
        buffer.putLong(endOffset)
        buffer.rewind()
        return buffer
    }

    companion object {
        fun from(buffer: ByteBuffer): FetchSnapshotRequest {
            val endOffset = buffer.long
            return FetchSnapshotRequest(endOffset)
        }
    }
}
