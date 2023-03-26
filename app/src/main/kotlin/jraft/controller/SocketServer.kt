package jraft.controller

import jraft.common.network.NetworkReceive
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.ConcurrentLinkedDeque

/**
 *
 * KafkaRaftServer -> BrokerServer      -> SharedServer
 *                 -> ControllerServer  -> SharedServer -> RaftManager
 *
 * kafka
 * - enableProcessingRequest
 *  - Acceptor.openServerSocket
 *    - Acceptor has thread
 * RaftManager
 * - RaftIoThread
 *    -> RaftClient.poll
 */
class SocketServer(
    private val port: Int,
) : Runnable {
    private val thread = Thread(this)
    private val nioSelector = Selector.open()
    private val processor = Processor()

    fun start() {
        thread.start()
    }

    override fun run() {
        val serverChannel = openServerChannel()
        serverChannel.register(nioSelector, SelectionKey.OP_ACCEPT)
        processor.start()

        while (true) {
            val numReady = nioSelector.select(1000)
            if (numReady > 0) {
                val keys = nioSelector.selectedKeys()
                val iter = keys.iterator()
                while (iter.hasNext()) {
                    val key = iter.next()
                    iter.remove()

                    val socketChannel = (key.channel() as ServerSocketChannel).accept()
                    processor.accept(socketChannel)
                }
            }
        }
    }

    private fun openServerChannel(): ServerSocketChannel {
        val serverChannel = ServerSocketChannel.open()
        serverChannel.configureBlocking(false)
        val socketAddress = InetSocketAddress(this.port)
        serverChannel.bind(socketAddress)

        return serverChannel
    }
}

class Processor : Runnable {
    private val thread = Thread(this)
    private val nioSelector = Selector.open()
    private val newConnections = ConcurrentLinkedDeque<SocketChannel>()
    private val newCompletedReceives = ConcurrentLinkedDeque<NetworkReceive>()

    override fun run() {
        while (true) {
            configureNewConnections()
            readFromConnections()
            processCompletedReceive()
        }
    }

    /**
     * called in another thread
     */
    fun accept(socketChannel: SocketChannel) {
        newConnections.add(socketChannel)
    }

    private fun configureNewConnections() {
        while (newConnections.isNotEmpty()) {
            val newConnection = newConnections.poll()
            newConnection.configureBlocking(false)
            val key = newConnection.register(nioSelector, SelectionKey.OP_READ)
            key.attach(NetworkReceive())
        }
    }

    private fun readFromConnections() {
        val newReady = nioSelector.select(1000)
        if (newReady > 0) {
            val keys = nioSelector.selectedKeys()
            val iter = keys.iterator()
            while (iter.hasNext()) {
                val key = iter.next()
                iter.remove()
                if (key.isValid.not()) continue
                if (key.isReadable.not()) throw Exception("not readable")

                val channel = key.channel() as SocketChannel
                val receive = key.attachment() as NetworkReceive

//                val read = receive.readFrom(channel)
//                if (read <= 0) {
//                    channel.close()
//                }
//                if (receive.completed()) {
//                    newCompletedReceives.add(receive)
//                }
            }
        }
    }

    private fun processCompletedReceive() {
        while (newCompletedReceives.isNotEmpty()) {
            val receive = newCompletedReceives.poll()
            // how to response?
        }
    }

    fun start() {
        thread.start()
    }
}
