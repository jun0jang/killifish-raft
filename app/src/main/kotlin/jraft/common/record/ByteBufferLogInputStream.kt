package jraft.common.record

import java.nio.ByteBuffer

class ByteBufferLogInputStream(
    private val byteBuffer: ByteBuffer,
) : LogInputStream {

    override fun nextBatch(): RecordBatch? {
        TODO("Not yet implemented")
    }
}
