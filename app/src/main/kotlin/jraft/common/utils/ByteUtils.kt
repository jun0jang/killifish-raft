package jraft.common.utils

import java.io.DataOutput
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

object ByteUtils {
    // UTF-8
    fun utf8(buffer: ByteBuffer, length: Int): String {
        return utf8(buffer, 0, length)
    }

    private fun utf8(buffer: ByteBuffer, offset: Int, length: Int): String {
        return String(
            buffer.array(),
            buffer.arrayOffset() + buffer.position() + offset,
            length,
            StandardCharsets.UTF_8,
        )
    }

    fun utf8(s: String): ByteArray {
        return s.toByteArray(StandardCharsets.UTF_8)
    }

    /**
     * Get the length for UTF8-encoding a string without encoding it first
     * - TODO: 공부 및 최적화 필요
     */
    fun utf8Length(s: CharSequence): Int {
        return Charsets.UTF_8.encode(s.toString()).limit()
    }

    fun toArray(buffer: ByteBuffer): ByteArray {
        val dest = ByteArray(buffer.remaining())
        System.arraycopy(buffer.array(), buffer.arrayOffset() + buffer.position(), dest, 0, buffer.remaining())
        return dest
    }

    // VarInt

    fun writeVarint(value: Int, buffer: DataOutput) {
        writeUnsignedVarint(zigZagEncode(value), buffer)
    }

    fun readVarint(buffer: ByteBuffer): Int {
        val value = readUnsignedVarint(buffer)
        return zigZagDecode(value)
    }

    fun writeUnsignedVarint(value: Int, buffer: DataOutput) {
        var actualValue = value

        /**
         * 0x7F = 127 = 0000 0000 0000 0000 0000 0000 0111 1111
         * 0x7F.inv() = 1111 1111 1111 1111 1111 1111 1000 0000
         * (value and 0x7F.inv()) != 0
         *  - value가 7 비트 이내로 표현 되지 않음을 의미
         */
        while (actualValue and 0x7F.inv() != 0) {
            /**
             * 0x7F = 127 = 0111 1111
             * 0x80 = 128 = 1000 0000
             * actualValue & 0x7F or 0x80
             * - 7비트를 가져옴
             * - 8번째 비트를 1로 세팅 (Continuation bit)
             */
            buffer.writeByte(((actualValue and 0x7F) or 0x80))
            // 사용한 7비트를 제거
            actualValue = actualValue ushr 7
        }

        buffer.writeByte(actualValue)
    }

    fun readUnsignedVarint(buffer: ByteBuffer): Int {
        var value = 0
        var shift = 0
        var b: Int
        do {
            b = buffer.get().toInt()
            /**
             * 0x7F = 127 = 0111 1111
             * (b and 0x7F) -> 7비트를 가져옴
             * (b and 0x7F shl shift) -> payload를 nth에 맞게 shift
             * value or ((b and 0x7F) shl shift) -> 기존 value에 payload를 추가
             */
            value = value or ((b and 0x7F) shl shift)
            shift += 7
            if (shift > 35) {
                throw IllegalArgumentException("Varint is too long")
            }
        }
        // 0x80 = 1000 0000
        // 8번째 비트가 0이면 종료
        while (b and 0x80 != 0)

        return value
    }

    fun sizeOfVarint(value: Int): Int {
        return sizeOfUnsignedVarint(zigZagEncode(value))
    }

    fun sizeOfUnsignedVarint(value: Int): Int {
        // Protocol buffers varint encoding is variable length, with a minimum of 1 byte
        // (for zero). The values themselves are not important. What's important here is
        // any leading zero bits are dropped from output. We can use this leading zero
        // count w/ fast intrinsic to calc the output length directly.
        //
        // 0일 경우
        //   (38 - leadingZeros) / 7 = 0
        //   leadingZeros / 32 = 1
        // 1일 경우
        //   (38 - leadingZeros) / 7 = 1
        //   leadingZeros / 32 = 0
        // 11111111_11111111_11111111_11111111일 경우
        //   (38 - 0) / 7 = 5
        //   0 / 32 = 0
        // leadingZeros 0, 1, 2, 3 까지는 return 5

        val leadingZeros = Integer.numberOfLeadingZeros(value)
        return (38 - leadingZeros) / 7 + leadingZeros / 32
    }

    // (n << 1) ^ (n >> 31) is ZigZag encoding
    private fun zigZagEncode(value: Int): Int {
        return value.shl(1).xor(value.shr(31))
    }

    private fun zigZagDecode(value: Int): Int {
        return value.ushr(1) xor -(value and 1)
    }

    // VarLong

    fun writeVarLong(value: Long, buffer: DataOutput) {
        writeUnsignedVarLong(zigZagEncode(value), buffer)
    }

    fun readVarLong(buffer: ByteBuffer): Long {
        val value = readUnsignedVarLong(buffer)
        return zigZagDecode(value)
    }

    fun writeUnsignedVarLong(value: Long, buffer: DataOutput) {
        var actualValue = value

        while (actualValue and 0x7F.inv() != 0L) {
            buffer.writeByte(((actualValue and 0x7F) or 0x80).toInt())
            actualValue = actualValue ushr 7
        }

        buffer.writeByte(actualValue.toByte().toInt())
    }

    fun readUnsignedVarLong(buffer: ByteBuffer): Long {
        var value = 0L
        var shift = 0
        var b: Long
        do {
            b = buffer.get().toLong()
            value = value or ((b and 0x7F) shl shift)
            shift += 7
            if (shift > 63) {
                throw IllegalArgumentException("Varlong is too long")
            }
        } while (b and 0x80 != 0L)

        return value
    }

    fun sizeOfVarLong(value: Long): Int {
        return sizeOfUnsignedVarLong(zigZagEncode(value))
    }

    fun sizeOfUnsignedVarLong(value: Long): Int {
        val leadingZeros = java.lang.Long.numberOfLeadingZeros(value)
        return (70 - leadingZeros) / 7 + leadingZeros / 64
    }

    private fun zigZagEncode(value: Long): Long {
        return value.shl(1).xor(value.shr(63))
    }

    private fun zigZagDecode(value: Long): Long {
        return value.ushr(1) xor -(value and 1)
    }
}
