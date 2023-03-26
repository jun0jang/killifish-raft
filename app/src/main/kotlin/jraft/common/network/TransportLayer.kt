package jraft.common.network

import java.nio.ByteBuffer

interface TransportLayer {
    fun addInterestOps(ops: Int)

    fun finishConnect(): Boolean

    fun isConnected(): Boolean

    fun read(dst: ByteBuffer): Int

    fun write(srcs: List<ByteBuffer>): Long

    fun hasPendingWrites(): Boolean

    fun disconnect()
}
