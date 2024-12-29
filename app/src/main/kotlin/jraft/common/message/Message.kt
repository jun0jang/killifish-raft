package jraft.common.message

import java.nio.ByteBuffer

interface Message {
    fun toByteBuffer(): ByteBuffer

    companion object {
        fun parsableByteBuffer(message: Message): ByteBuffer {
            val key =
                when (message) {
                    is FetchSnapshotRequest -> MessageKey.FETCH_SNAPSHOT_REQUEST
                    is FetchSnapshotResponse -> MessageKey.FETCH_SNAPSHOT_RESPONSE
                    else -> throw Exception("unkown message")
                }

            val messageBuffer = message.toByteBuffer().rewind()
            val buffer = ByteBuffer.allocate(4 + messageBuffer.limit())
            buffer.putInt(key.ordinal)
            buffer.put(messageBuffer)
            buffer.rewind()
            return buffer
        }

        fun parse(buffer: ByteBuffer): Message {
            return when (MessageKey.values()[buffer.int]) {
                MessageKey.FETCH_SNAPSHOT_REQUEST -> FetchSnapshotRequest.from(buffer)
                MessageKey.FETCH_SNAPSHOT_RESPONSE -> FetchSnapshotResponse.from(buffer)
            }
        }
    }
}

enum class MessageKey {
    FETCH_SNAPSHOT_REQUEST,
    FETCH_SNAPSHOT_RESPONSE,
}
