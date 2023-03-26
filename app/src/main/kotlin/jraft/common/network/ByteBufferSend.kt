package jraft.common.network

import java.nio.ByteBuffer

class ByteBufferSend(
    private val buffers: List<ByteBuffer>,
) : Send {
    private var remaining = buffers.sumOf { it.remaining().toLong() }

    private val size = remaining

    private var pending = false

    override fun size(): Long {
        return this.size
    }

    override fun writeTo(transportLayer: TransportLayer): Long {
        val written = transportLayer.write(buffers)

        remaining -= written
        pending = transportLayer.hasPendingWrites()

        return written
    }

    override fun completed(): Boolean {
        return remaining <= 0 && !pending
    }
}
