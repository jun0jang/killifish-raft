package jraft.common.protocol

import java.nio.ByteBuffer

interface Readable {
    fun readByte(): Byte

    fun readShort(): Short

    fun readInt(): Int

    fun readLong(): Long

    fun readDouble(): Double

    fun readArray(length: Int): ByteArray

    fun readUnsignedVarInt(): Int

    fun readByteBuffer(length: Int): ByteBuffer

    fun readVarInt(): Int

    fun readVarLong(): Long

    fun remaining(): Int
}
