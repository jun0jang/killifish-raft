package jraft.common.network

import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel

class KafkaChannel(
    val nodeId: String,
    val transportLayer: TransportLayer,
    val receiveBuilder: ReceiveBuilder,
) {
    private var send: NetworkSend? = null

    private var receive: Receive? = null

    fun send(send: NetworkSend) {
        if (this.send != null) {
            throw IllegalStateException("Attempt to begin a send operation with prior send operation still in progress")
        }

        this.send = send
        transportLayer.addInterestOps(SelectionKey.OP_WRITE)
    }

    fun write(): Long {
        return this.send?.writeTo(transportLayer) ?: 0
    }

    fun hasSend(): Boolean {
        return send != null
    }

    fun read(): Long {
        if (receive == null) {
            receive = receiveBuilder.build()
        }

        return receive(receive!!)
    }

    fun maybeCompletedReceive(): Receive? {
        return if (receive?.completed() == true) {
            receive?.payload()?.rewind()
            val completedReceive = receive
            // 다음 receive를 위해 초기화
            receive = null
            completedReceive
        } else {
            null
        }
    }

    fun mayneCompletedSend(): NetworkSend? {
        return if (send?.completed() == true) {
            val completedSend = send
            send = null
            completedSend
        } else {
            null
        }
    }

    private fun receive(networkReceive: Receive): Long {
        return networkReceive.readFrom(transportLayer)
    }

    fun finishConnect(): Boolean {
        return transportLayer.finishConnect()
    }

    fun disconnect() {
        transportLayer.disconnect()
    }

    companion object {
        fun create(
            nodeId: String,
            selectionKey: SelectionKey,
            receiveBuilder: ReceiveBuilder,
        ): KafkaChannel {
            val transportLayer = PlaintextTransportLayer(
                key = selectionKey,
                socketChannel = selectionKey.channel() as SocketChannel,
            )

            return KafkaChannel(nodeId, transportLayer, receiveBuilder)
        }
    }
}
