package jraft.common.network

import java.nio.ByteBuffer

class NetworkSend(
    val destinationId: String,
    val send: Send,
) : Send {
    companion object {
        fun create(
            nodeId: String,
            payload: String,
        ): NetworkSend {
            val buffer = ByteBuffer.wrap(payload.toByteArray())
            val sizeBuffer = ByteBuffer.allocate(4)
            sizeBuffer.putInt(0, buffer.remaining())

            return NetworkSend(
                nodeId,
                ByteBufferSend(listOf(sizeBuffer, buffer)),
            )
        }
    }

    override fun completed(): Boolean {
        return send.completed()
    }

    override fun writeTo(transportLayer: TransportLayer): Long {
        return send.writeTo(transportLayer)
    }

    override fun size(): Long {
        return send.size()
    }
}
