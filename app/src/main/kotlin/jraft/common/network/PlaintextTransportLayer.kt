package jraft.common.network

import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

class PlaintextTransportLayer(
    val key: SelectionKey,
    val socketChannel: SocketChannel,
) : TransportLayer {

    override fun addInterestOps(ops: Int) {
        key.interestOps(key.interestOps() or ops)
    }

    override fun finishConnect(): Boolean {
        val connected = socketChannel.finishConnect()
        if (connected) {
            // OP_CONNECT flag 제거 & OP_READ flag 추가
            key.interestOps(key.interestOps() and SelectionKey.OP_CONNECT.inv())
            key.interestOps(key.interestOps() or SelectionKey.OP_READ)
        }

        return connected
    }

    override fun isConnected(): Boolean {
        return socketChannel.isConnected
    }

    override fun read(dst: ByteBuffer): Int {
        return socketChannel.read(dst)
    }

    override fun write(srcs: List<ByteBuffer>): Long {
        return socketChannel.write(srcs.toTypedArray())
    }

    override fun hasPendingWrites(): Boolean {
        return false
    }

    override fun disconnect() {
        key.cancel()
        socketChannel.close()
    }
}
