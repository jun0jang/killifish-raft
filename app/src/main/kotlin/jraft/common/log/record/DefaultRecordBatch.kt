package jraft.common.log.record

import jraft.common.log.compress.CompressionType
import java.nio.ByteBuffer
import kotlin.experimental.and

class DefaultRecordBatch(
    private val buffer: ByteBuffer,
) {
    fun compressionType(): CompressionType {
        val id = attributes() and COMPRESSION_CODEC_MASK
        return CompressionType.of(id.toInt())
    }

    companion object {
        const val BASE_OFFSET_OFFSET: Int = 0
        const val BASE_OFFSET_LENGTH: Int = 8
        const val LENGTH_OFFSET: Int = BASE_OFFSET_OFFSET + BASE_OFFSET_LENGTH
        const val LENGTH_LENGTH: Int = 4
        const val PARTITION_LEADER_EPOCH_OFFSET: Int = LENGTH_OFFSET + LENGTH_LENGTH
        const val PARTITION_LEADER_EPOCH_LENGTH: Int = 4
        const val MAGIC_OFFSET: Int = PARTITION_LEADER_EPOCH_OFFSET + PARTITION_LEADER_EPOCH_LENGTH
        const val MAGIC_LENGTH: Int = 1
        const val CRC_OFFSET: Int = MAGIC_OFFSET + MAGIC_LENGTH
        const val CRC_LENGTH: Int = 4
        const val ATTRIBUTES_OFFSET: Int = CRC_OFFSET + CRC_LENGTH
        const val ATTRIBUTE_LENGTH: Int = 2

        const val COMPRESSION_CODEC_MASK: Byte = 0x07 // 0000 0111

        fun writeHeader(
            buffer: ByteBuffer,
            baseOffset: Long,
            length: Int,
            partitionLeaderEpoch: Int,
            magic: Byte,
            crc: Int,
            compressionType: CompressionType,
        ) {
            buffer.putLong(BASE_OFFSET_OFFSET, baseOffset)
            buffer.putInt(LENGTH_OFFSET, length)
            buffer.putInt(PARTITION_LEADER_EPOCH_OFFSET, partitionLeaderEpoch)
            buffer.put(MAGIC_OFFSET, magic)
            buffer.putInt(CRC_OFFSET, crc)
            buffer.putShort(
                ATTRIBUTES_OFFSET,
                computeAttributes(compressionType).toShort(),
            )
        }

        private fun computeAttributes(compressionType: CompressionType): Byte {
            return compressionType.id.toByte() and COMPRESSION_CODEC_MASK
        }
    }

    private fun attributes(): Byte {
        // note we're not using the second byte of attributes
        // short.toByte() == (byte1 byte2) -> byte2
        return buffer.getShort(ATTRIBUTES_OFFSET).toByte()
    }
}
