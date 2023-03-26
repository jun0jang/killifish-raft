package jraft.common.network

import java.nio.ByteBuffer

class NetworkReceive : Receive {
    var buffer: ByteBuffer? = null

    private val size = ByteBuffer.allocate(4)

    private var requestedBufferSize: Int = -1

    override fun completed(): Boolean {
        // size가 다 채워져 있고, buffer도 다 채워져 있으면 완료
        return size.hasRemaining().not() && buffer?.hasRemaining()?.not() == true
    }

    override fun readFrom(transportLayer: TransportLayer): Long {
        var read = 0L

        // size가 아직 안채워져 있다는 것은 아직 size를 읽지 않았다는 것
        if (size.hasRemaining()) {
            read += transportLayer.read(size)

            if (size.hasRemaining().not()) {
                size.rewind()
                val receiveSize = size.getInt()
                requestedBufferSize = receiveSize
            }
        }

        if (buffer == null) {
            buffer = ByteBuffer.allocate(requestedBufferSize)
        }

        val bytesRead = transportLayer.read(buffer!!)
        read += bytesRead

        return read
    }

    override fun payload(): ByteBuffer {
        return buffer!!
    }
}
