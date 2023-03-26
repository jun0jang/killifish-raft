package jraft.common.log.record

object ByteUtils {

    // -1: 11111111_11111111_11111111_11111111
    // 11111111_11111111_11111111_11111111 shl 1 -> 11111111_11111111_11111111_11111110
    // 11111111_11111111_11111111_11111111 shr 31 -> 11111111_11111111_11111111_11111111
    //  - shr은 음수의 경우 빈자리가 1로 채워지기에 현상 유지된다.
    // xor -> 00000000_00000000_00000000_00000001

    // 양수의 경우
    // - shl 1을 하면 2배가 된다.
    // - shr 31을 하면 0이된다.
    //  - 해당 결과에 xor을 해도 영향이 없다.
    // -1의 경우 ( 11111111_11111111_11111111_11111111)
    //  - shl 1 -> 11111111_11111111_11111111_11111110
    //  - shr 31 -> 11111111_11111111_11111111_11111111
    //    - shr은 음수의 경우 빈자리가 1로 채워지기에 현상 유지된다.
    //  - xor -> 00000000_00000000_00000000_00000001
    // 2147483648의 경우 (10000000_00000000_00000000_00000000)
    // - shl 1 -> 00000000_00000000_00000000_00000000
    // - shr 31 -> 11111111_11111111_11111111_11111111
    // - xor -> 11111111_11111111_11111111_11111111
    fun sizeOfVariant(value: Int): Int {
        // 2배수
        // varint 특성상 128 (10000000)이면 1바이트가 더 필요하다.
        return sizeOfUnsignedVarint(value.shl(1).xor(value.shr(31)))
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
}
