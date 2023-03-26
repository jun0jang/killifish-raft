package jraft.clients.common

import jraft.common.network.TransportLayer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class FakeTransportLayer : TransportLayer {
    private val buffer = ByteArrayOutputStream()

    private var inputStream: ByteArrayInputStream? = null

    override fun addInterestOps(ops: Int) {
        TODO("Not yet implemented")
    }

    override fun finishConnect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isConnected(): Boolean {
        TODO("Not yet implemented")
    }

    override fun read(dst: ByteBuffer): Int {
        return inputStream?.let {
            val bytes = it.readNBytes(dst.remaining())
            dst.put(bytes)
            return bytes.size
        } ?: -1
    }

    override fun write(srcs: List<ByteBuffer>): Long {
        var result = 0
        for (src in srcs) {
            result += src.remaining()
            buffer.write(src.array())
        }
        inputStream = ByteArrayInputStream(buffer.toByteArray())

        return result.toLong()
    }

    override fun hasPendingWrites(): Boolean {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }
}
