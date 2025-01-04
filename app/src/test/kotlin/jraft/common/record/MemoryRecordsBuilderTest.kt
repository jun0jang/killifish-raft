package jraft.common.record

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.nio.ByteBuffer

class MemoryRecordsBuilderTest : FreeSpec({

    val logAppendTime = System.currentTimeMillis()

    "testIterator" {
        val producerId = 1L
        val epoch = 28
        val firstOffset = 10L
        val firstSequence = 777
        val partitionLeaderEpoch = 998

        val buffer = ByteBuffer.allocate(1024)

        val builder = MemoryRecordsBuilder.new(
            buffer = buffer,
            magic = RecordBatch.CURRENT_MAGIC_VALUE,
            timestampType = TimestampType.CREATE_TIME,
            baseOffset = firstOffset,
            logAppendTime = logAppendTime,
            producerId = producerId,
            producerEpoch = epoch.toShort(),
            baseSequence = firstSequence,
            isTransactional = false,
            isControlBatch = false,
            partitionLeaderEpoch = partitionLeaderEpoch,
        )

        val records = listOf(
            SimpleRecord.new("a".toByteArray(), "1".toByteArray(), 1L),
            SimpleRecord.new("b".toByteArray(), "2".toByteArray(), 2L),
            SimpleRecord.new("c".toByteArray(), "3".toByteArray(), 3L),
            SimpleRecord.new("d".toByteArray(), "4".toByteArray(), 4L),
            SimpleRecord.new("e".toByteArray(), null, 5L),
            SimpleRecord.new(null as ByteArray?, null, 6L),
        )

        for (record in records) {
            builder.append(record)
        }

        val memoryRecords = builder.build()

        var total = 0
        for (batch in memoryRecords.batches()) {
            batch.producerId() shouldBe producerId
            batch.baseSequence() shouldBe firstSequence

            for (record in batch) {
                record.offset() shouldBe firstOffset + total
                record.sequence() shouldBe firstSequence + total
                record.key() shouldBe records[total].key
                record.value() shouldBe records[total].value

                total++
            }
        }
    }
})
