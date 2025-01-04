package jraft.common.utils

import java.io.DataOutput
import java.nio.ByteBuffer

object Utils {
    fun readBytes(srcBuffer: ByteBuffer, bytesToRead: Int): ByteBuffer? {
        if (bytesToRead < 0) return null

        val dstBuffer = srcBuffer.slice()
        dstBuffer.limit(bytesToRead)
        srcBuffer.position(srcBuffer.position() + bytesToRead)

        return dstBuffer
    }

    // Write the contents of a buffer to an output stream.
    fun writeTo(out: DataOutput, buffer: ByteBuffer, length: Int) {
        if (buffer.hasArray()) {
            out.write(buffer.array(), buffer.position() + buffer.arrayOffset(), length)
        } else {
            val pos = buffer.position()
            for (i in pos until pos + length) {
                out.writeByte(buffer.get(i).toInt())
            }
        }
    }

    fun wrapNullable(array: ByteArray?): ByteBuffer? {
        return if (array == null) {
            null
        } else {
            ByteBuffer.wrap(array)
        }
    }
}
