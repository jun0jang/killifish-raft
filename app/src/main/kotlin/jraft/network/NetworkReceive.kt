package jraft.network

import jraft.message.Message
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class NetworkReceive {
    private val size: ByteBuffer = ByteBuffer.allocate(4)
    private var data: ByteBuffer? = null

    fun readFrom(channel: SocketChannel): Int {
        var read = 0
        if (size.hasRemaining()) {
            val bytesRead = channel.read(size)
            if (bytesRead < 0) return -1
            read += bytesRead
        }
        if (size.hasRemaining().not() && data == null) {
            size.rewind()
            data = ByteBuffer.allocate(size.int)
        }
        if (data?.hasRemaining() == true) {
            val bytesRead = channel.read(data)
            if (bytesRead < 0) return -1
            read += bytesRead
        }
        return read
    }

    fun message(): Message {
        if (completed().not()) throw RuntimeException("message is not still completed")
        return Message.parse(data!!.rewind())
    }

    fun completed(): Boolean {
        return size.hasRemaining().not() && data?.hasRemaining()?.not() == true
    }
}

