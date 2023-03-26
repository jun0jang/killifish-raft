package jraft.clients.common

import io.kotest.core.spec.style.FreeSpec
import jraft.common.network.HttpReceive
import java.nio.ByteBuffer

class HttpReceiveTest : FreeSpec({
    val transportLayer = FakeTransportLayer()

    "parse Http Response" {
        // Given
        val sut = HttpReceive()

        transportLayer.write(
            listOf(
                "HTTP/1.1 200 OK\r\n".toByteArray(),
                "Content-Type: text/html; charset=UTF-8\r\n".toByteArray(),
                "Transfer-Encoding: chunked\r\n".toByteArray(),
                "\r\n".toByteArray(),
                "Hello world\r\n".toByteArray(),
                "0\r\n".toByteArray(),
                "\r\n".toByteArray(),
                // next response
                "HTTP/1.1 200 OK\r\n".toByteArray(),
                "Content-Type: text/html; charset=UTF-8\r\n".toByteArray(),
                "Transfer-Encoding: chunked\r\n".toByteArray(),
                "\r\n".toByteArray(),
                "Next Hello world\r\n".toByteArray(),
                "0\r\n".toByteArray(),
                "\r\n".toByteArray(),
            ).map {
                ByteBuffer.wrap(it)
            },
        )

        // When
        sut.readFrom(transportLayer)

        // Then
        val payload = sut.payload()
        println(String(payload.array()))
    }
})
