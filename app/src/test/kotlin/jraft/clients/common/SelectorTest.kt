package jraft.clients.common

import io.kotest.core.spec.style.FreeSpec
import jraft.clients.TestUtils
import jraft.common.network.ByteBufferSend
import jraft.common.network.NetworkSend
import jraft.common.network.ReceiveBuilder
import jraft.common.network.Selector
import java.net.InetSocketAddress
import java.nio.ByteBuffer

// TODO: LocalHost echo 서버 띄워서 직접 연결성 테스트하기.
class SelectorTest : FreeSpec({
    val selector = Selector(
        receiveBuilder = ReceiveBuilder(http = true),
    )

    "connect to google" - {
        val nodeId = "google"

        selector.connect(
            nodeId = "google",
            address = InetSocketAddress("www.google.com", 80),
            sendBufferSize = 1024,
            receiveBufferSize = 1024,
        )

        "send http" {
            val networkSend = NetworkSend(
                destinationId = nodeId,
                send = ByteBufferSend(
                    buffers = listOf(
                        ByteBuffer.wrap("GET / HTTP/1.1\r\n".toByteArray()),
                        ByteBuffer.wrap("Host: www.google.com\r\n".toByteArray()),
                        ByteBuffer.wrap("Accept: */*\r\n\r\n".toByteArray()),
                    ),
                ),
            )
            selector.send(networkSend)

            // send
            TestUtils.waitForCondition(
                timeoutMs = 5000,
                message = "Send failed",
            ) {
                selector.poll(timeout = 1000)
                selector.completedSends.isNotEmpty()
            }

            // receive
            TestUtils.waitForCondition(
                timeoutMs = 5000,
                message = "Receive failed",
            ) {
                selector.poll(timeout = 1000)
                selector.completedReceives.isNotEmpty()
            }
            println(selector.completedReceives)
        }
    }
})
