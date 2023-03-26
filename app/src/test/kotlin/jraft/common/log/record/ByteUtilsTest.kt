package jraft.common.log.record

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class ByteUtilsTest : FreeSpec({

    "testSizeOfVariant" {
        // Given
        val pairs = listOf(
            0b11111111_11111111_11111111_11111111 to 1, // -1's size is 1
            0b00000000_00000000_00000000_00000011 to 1, // 3's size is 1 bytes
            0b00000000_00000000_00000000_10000000 to 2, // 128's size is 2 bytes
            0b10000000_00000000_00000000_00000000 to 5, // -2147483648's size is 5 bytes
            0b01000000_00000000_00000000_00000000 to 5, // 1073741824's size is 5 bytes
            0b00010000_00000000_00000000_00000000 to 5, // 268435456's size is 5 bytes
            0b00000100_00000000_00000000_00000000 to 4, // 268435456's size is 5 bytes
        ).map { (value, expected) -> value.toInt() to expected }

        for ((value, expected) in pairs) {
            val size = ByteUtils.sizeOfVariant(value)

            size shouldBe expected
        }
    }
})
