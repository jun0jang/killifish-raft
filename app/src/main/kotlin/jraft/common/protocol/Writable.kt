package jraft.common.protocol

import java.nio.ByteBuffer

interface Writable {
    fun writeBye(value: Byte)

    fun writeShort(value: Short)

    fun writeInt(value: Int)

    fun writeLong(value: Long)

    fun writeDouble(value: Double)

    fun writeByteArray(value: ByteArray)

    fun writeUnsignedVarInt(value: Int)

    fun writeByteBuffer(value: ByteBuffer)

    fun writeVarInt(value: Int)

    fun writeVarLong(value: Long)
}
