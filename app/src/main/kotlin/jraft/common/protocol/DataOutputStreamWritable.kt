package jraft.common.protocol

import jraft.common.utils.ByteUtils
import jraft.common.utils.Utils
import java.io.Closeable
import java.io.DataOutputStream
import java.nio.ByteBuffer

class DataOutputStreamWritable(
    private val out: DataOutputStream,
) : Writable, Closeable {
    override fun writeByte(value: Byte) {
        out.writeByte(value.toInt())
    }

    override fun writeShort(value: Short) {
        out.writeShort(value.toInt())
    }

    override fun writeInt(value: Int) {
        out.writeInt(value)
    }

    override fun writeLong(value: Long) {
        out.writeLong(value)
    }

    override fun writeDouble(value: Double) {
        out.writeDouble(value)
    }

    override fun writeByteArray(value: ByteArray) {
        out.write(value)
    }

    override fun writeUnsignedVarInt(value: Int) {
        ByteUtils.writeUnsignedVarint(value, out)
    }

    override fun writeByteBuffer(value: ByteBuffer) {
        Utils.writeTo(out, value, value.remaining())
    }

    override fun writeVarint(value: Int) {
        ByteUtils.writeVarint(value, out)
    }

    override fun writeVarlong(value: Long) {
        ByteUtils.writeVarLong(value, out)
    }

    override fun flush() {
        out.flush()
    }

    // close trigger flush
    override fun close() {
        out.close()
    }
}
