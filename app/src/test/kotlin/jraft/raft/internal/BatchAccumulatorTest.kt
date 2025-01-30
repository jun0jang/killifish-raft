package jraft.raft.internal

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import jraft.clients.consumer.internals.FakeTime
import jraft.common.memory.SimpleMemoryPool
import jraft.common.serialization.StringSerde

class BatchAccumulatorTest : FreeSpec({
    val time = FakeTime()

    fun buildAccumulator(
        leaderEpoch: Int,
        baseOffset: Long,
        maxBatchSize: Int,
    ): BatchAccumulator<String> {
        return BatchAccumulator.new(
            epoch = leaderEpoch,
            baseOffset = baseOffset,
            maxBatchSize = maxBatchSize,
            memoryPool = SimpleMemoryPool(sizeBytes = 1024, strict = true),
            time = time,
            serde = StringSerde(),
        )
    }

    "test write & drain" {
        val leaderEpoch = 1
        val baseOffset = 157L
        val maxBatchSize = 512

        val acc = buildAccumulator(
            leaderEpoch = leaderEpoch,
            baseOffset = baseOffset,
            maxBatchSize = maxBatchSize,
        )

        val records = listOf("a", "b", "c", "d", "e", "f", "g", "h", "i")

        acc.append(leaderEpoch, records.subList(0, 1), false) shouldBe baseOffset
        acc.append(leaderEpoch, records.subList(1, 3), false) shouldBe baseOffset + 2
        acc.append(leaderEpoch, records.subList(3, 6), false) shouldBe baseOffset + 5
        acc.append(leaderEpoch, records.subList(6, 8), false) shouldBe baseOffset + 7
        acc.append(leaderEpoch, records.subList(8, 9), false) shouldBe baseOffset + 8

        val batches = acc.drain()
        batches.size shouldBe 1

        val batch = batches[0]
        batch.baseOffset shouldBe baseOffset
        batch.records shouldBe records

        val recordBatches = batch.data.batches().toList()
        recordBatches.size shouldBe 1
        recordBatches[0].count() shouldBe records.size
        for ((i, savedRecord) in recordBatches[0].withIndex()) {
            savedRecord.value()!!.get().toInt().toChar().toString() shouldBe records[i]
        }
    }
})
