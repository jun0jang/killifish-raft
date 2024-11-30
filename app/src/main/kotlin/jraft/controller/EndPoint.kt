package jraft.controller

import java.net.InetSocketAddress

data class EndPoint(
    val host: String,
    val port: Int,
) {
    fun socketAddress(): InetSocketAddress {
        if (host.isBlank()) {
            return InetSocketAddress(port)
        }
        return InetSocketAddress(host, port)
    }
}
