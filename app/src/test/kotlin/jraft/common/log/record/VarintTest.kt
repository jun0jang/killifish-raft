package jraft.common.log.record

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import jraft.common.utils.ByteBufferOutputStream
import jraft.common.utils.ByteUtils
import org.junit.jupiter.api.Assertions.assertArrayEquals
import java.io.DataOutputStream

class VarintTest : FreeSpec({

    "write and read varint" {
        val values = 0..Int.MAX_VALUE step 13
        for (value in values) {
            // Given
            val out = ByteBufferOutputStream.new(5)
            ByteUtils.writeVarint(value, DataOutputStream(out))

            val buffer = out.buffer()
            buffer.flip()

            // When
            val readValue = ByteUtils.readVarint(buffer)

            // Then
            readValue shouldBe value
        }
    }

    "writeUnsignedVarint" {
        // Given
        val pairs = listOf(
            1 to byteArrayOf(1, 0, 0, 0, 0),
            // 128 == 0b1000_0000
            128 to byteArrayOf(-128, 1, 0, 0, 0),
            // 256 == 0b1_0000_0000
            256 to byteArrayOf(-128, 2, 0, 0, 0),
            // 2147483647 == 0b0111_1111_1111_1111_1111_1111_1111_1111
            2147483647 to byteArrayOf(-1, -1, -1, -1, 7),
        )

        for ((value, expected) in pairs) {
            // Given
            val out = ByteBufferOutputStream.new(5)

            // When
            ByteUtils.writeUnsignedVarint(value, DataOutputStream(out))

            val buffer = out.buffer()

            // Then
            assertArrayEquals(
                expected,
                buffer.array(),
                "Implementations do not match for integer=$value",
            )
        }
    }

    "readUnsignedVarint" {
        val values = 0..Int.MAX_VALUE step 13
        for (value in values) {
            // Given
            val output = ByteBufferOutputStream.new(5)
            ByteUtils.writeUnsignedVarint(value, DataOutputStream(output))

            val buffer = output.buffer()
            buffer.flip()

            // When
            val readValue = ByteUtils.readUnsignedVarint(buffer)

            // Then
            readValue shouldBe value
        }
    }

    "sizeOfVariant" {
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
            val size = ByteUtils.sizeOfVarint(value)

            size shouldBe expected
        }
    }
})
