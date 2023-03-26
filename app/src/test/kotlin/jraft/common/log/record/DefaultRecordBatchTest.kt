package jraft.common.log.record

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import jraft.common.log.compress.CompressionType
import java.nio.ByteBuffer

class DefaultRecordBatchTest : FreeSpec({

    "compression type" {
        // Given
        // Q. 실사례에서 capacity를 어떻게 주는지 확인 필요.
        val byteBuffer = ByteBuffer.allocate(1024)

        // When
        DefaultRecordBatch.writeHeader(
            buffer = byteBuffer,
            baseOffset = 0,
            length = 512,
            partitionLeaderEpoch = 0,
            magic = 1,
            crc = 1,
            compressionType = CompressionType.GZIP,
        )
        val recordBatch = DefaultRecordBatch(byteBuffer)

        // Then
        recordBatch.compressionType() shouldBe CompressionType.GZIP
    }
})
