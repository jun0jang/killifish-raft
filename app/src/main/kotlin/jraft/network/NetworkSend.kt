package jraft.network

import jraft.message.Message
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class NetworkSend(
    buffer: ByteBuffer,
) {
    private val buffer = ByteBuffer.allocate(4 + buffer.limit())
        .putInt(buffer.limit())
        .put(buffer)
        .rewind()

    fun writeTo(channel: SocketChannel): Int {
        return channel.write(buffer)
    }

    companion object {
        fun from(message: Message): NetworkSend {
            val byteBuffer = Message.parsableByteBuffer(message)
            return NetworkSend(byteBuffer)
        }
    }
}