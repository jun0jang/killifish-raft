package jraft.common.network

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.nio.channels.UnresolvedAddressException

class Selector(
    private val receiveBuilder: ReceiveBuilder,
) {
    private val nioSelector = java.nio.channels.Selector.open()

    private val channels = mutableMapOf<String, KafkaChannel>()

    // key: nodeId, value: NetworkReceive
    val completedReceives = mutableMapOf<String, Receive>()

    val completedSends = mutableListOf<NetworkSend>()

    fun poll(timeout: Long) {
        val numReadyKeys = nioSelector.select(timeout)

        if (numReadyKeys > 0) {
            val readyKeys = nioSelector.selectedKeys()

            pollSelectionKeys(readyKeys)

            // Clear all selected keys so that they are excluded from the ready count for the next select
            readyKeys.clear()
        }
    }

    private fun pollSelectionKeys(selectionKeys: Set<SelectionKey>) {
        for (key in selectionKeys) {
            val channel = key.attachment() as KafkaChannel

            // OP_OCONNECT가 Ready 상태인 경우
            if (key.isConnectable) {
                channel.finishConnect()
            }

            if (key.isReadable) {
                attemptRead(channel)
            }

            if (channel.hasSend() && key.isWritable) {
                attemptWrite(channel)
            }

            if (key.isValid.not()) {
                channel.disconnect()
            }
        }
    }

    private fun attemptRead(channel: KafkaChannel) {
        val bytesReceive = channel.read()
        if (bytesReceive > 0) {
            val receive = channel.maybeCompletedReceive()
            if (receive != null) {
                completedReceives[channel.nodeId] = receive
            }
        }
    }

    private fun attemptWrite(channel: KafkaChannel) {
        channel.write()
        val send = channel.mayneCompletedSend()
        if (send?.completed() == true) {
            completedSends.add(send)
        }
    }

    fun connect(
        nodeId: String,
        address: InetSocketAddress,
        sendBufferSize: Int,
        receiveBufferSize: Int,
    ) {
        ensureNotRegistered(nodeId)

        val socketChannel = SocketChannel.open()
        val key: SelectionKey?

        try {
            configureSocketChannel(socketChannel, sendBufferSize, receiveBufferSize)
            val isConnected = doConnect(socketChannel, address)
            key = registerChannel(nodeId, socketChannel)

            // check: poll 에서 대응하면 안되는 이유가 있는지 체크
            if (isConnected) {
                // 이미 연결이 완료 되었기 떄문에 select 괸심 flag가 없음
                key.interestOps(0)
            }
        } catch (e: IOException) {
            socketChannel.close()
            throw e
        }
    }

    private fun ensureNotRegistered(nodeId: String) {
        if (channels.containsKey(nodeId)) {
            throw IllegalStateException("There is already a connection for $nodeId")
        }
    }

    private fun registerChannel(
        nodeId: String,
        socketChannel: SocketChannel,
    ): SelectionKey {
        val key = socketChannel.register(nioSelector, SelectionKey.OP_CONNECT)

        try {
            val channel = KafkaChannel.create(nodeId, key, receiveBuilder)
            key.attach(channel)
            channels[nodeId] = channel
        } catch (e: Exception) {
            try {
                socketChannel.close()
            } finally {
                key.cancel()
            }
            throw IOException("Error while creating KafkaChannel $socketChannel", e)
        }

        return key
    }

    private fun doConnect(channel: SocketChannel, address: InetSocketAddress): Boolean {
        try {
            return channel.connect(address)
        } catch (e: UnresolvedAddressException) {
            throw IOException("Can't resolve address: $address")
        }
    }

    private fun configureSocketChannel(socketChannel: SocketChannel, sendBufferSize: Int, receiveBufferSize: Int) {
        socketChannel.configureBlocking(false)
        val socket = socketChannel.socket()
        socket.keepAlive = true

        if (sendBufferSize > 0) {
            socket.sendBufferSize = sendBufferSize
        }
        if (receiveBufferSize > 0) {
            socket.receiveBufferSize = receiveBufferSize
        }

        socket.tcpNoDelay = true
    }

    fun send(send: NetworkSend) {
        val nodeId = send.destinationId

        val channel = openOrClosingChannelOrFail(nodeId)
        try {
            channel.send(send)
        } catch (e: Exception) {
            throw e
        }
    }

    private fun openOrClosingChannelOrFail(nodeId: String): KafkaChannel {
        return channels[nodeId]
            ?: throw IllegalStateException("Attempt to send to node $nodeId which is not registered.")
    }
}
