package jraft.common.utils

import java.io.OutputStream
import java.nio.ByteBuffer

class ByteBufferOutputStream(
    private var buffer: ByteBuffer,
) : OutputStream() {
    companion object {
        private val REALLOCATION_FACTOR = 1.1f

        fun new(initialCapacity: Int): ByteBufferOutputStream {
            return ByteBufferOutputStream(ByteBuffer.allocate(initialCapacity))
        }
    }

    private val initialPosition = buffer.position()

    private val initialCapacity = buffer.capacity()

    override fun write(b: Int) {
        ensureRemaining(1)
        buffer.put(b.toByte())
    }

    override fun write(b: ByteArray) {
        ensureRemaining(b.size)
        buffer.put(b)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        ensureRemaining(len)
        buffer.put(b, off, len)
    }

    fun buffer(): ByteBuffer = buffer

    private fun ensureRemaining(remainingBytesRequired: Int) {
        if (remainingBytesRequired > buffer.remaining()) {
            expandBuffer(remainingBytesRequired)
        }
    }

    private fun expandBuffer(remainingBytesRequired: Int) {
        val expendSize = Math.max(
            (buffer.limit() * REALLOCATION_FACTOR).toInt(),
            buffer.position() + remainingBytesRequired,
        )
        val temp = ByteBuffer.allocate(expendSize)
        val limit = buffer.limit()
        buffer.flip()
        temp.put(buffer)
        buffer.limit(limit)
        buffer.position(initialPosition)
        buffer = temp
    }
}
