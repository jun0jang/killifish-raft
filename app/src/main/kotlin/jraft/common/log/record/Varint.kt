package jraft.common.log.record

import java.nio.ByteBuffer

object Varint {

    fun writeVarint(value: Int, buffer: ByteBuffer) {
        writeUnsignedVarint(zigZagEncode(value), buffer)
    }

    fun writeUnsignedVarint(value: Int, buffer: ByteBuffer) {
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
            buffer.put(((actualValue and 0x7F) or 0x80).toByte())
            // 사용한 7비트를 제거
            actualValue = actualValue ushr 7
        }

        buffer.put(actualValue.toByte())
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
    fun zigZagEncode(value: Int): Int {
        return value.shl(1).xor(value.shr(31))
    }
}
