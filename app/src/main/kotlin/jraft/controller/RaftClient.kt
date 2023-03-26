package jraft.controller

import jraft.message.FetchSnapshotRequest
import jraft.message.Message
import jraft.network.NetworkSend
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

class RaftClient {
    fun fetchSnapshot() {
        val socket = SocketChannel.open()
        socket.connect(InetSocketAddress("localhost", 9094))

        val message = FetchSnapshotRequest(endOffset = 10)
        val networkSend = NetworkSend.from(message)
        networkSend.writeTo(socket)

        val byteBuffer = ByteBuffer.allocate(1024)
        socket.read(byteBuffer)

        println(Message.parse(byteBuffer))
        println(byteBuffer)
    }
}

fun main() {
    val client = RaftClient()
    client.fetchSnapshot()
}