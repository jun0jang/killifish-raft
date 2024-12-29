package jraft.common.message

import java.nio.ByteBuffer

class FetchSnapshotResponse(
    private val endOffset: Long,
) : Message {
    override fun toByteBuffer(): ByteBuffer {
        val buffer = ByteBuffer.allocate(8)
        buffer.putLong(endOffset)
        return buffer
    }

    companion object {
        fun from(buffer: ByteBuffer): FetchSnapshotResponse {
            val endOffset = buffer.long
            return FetchSnapshotResponse(endOffset)
        }
    }
}
