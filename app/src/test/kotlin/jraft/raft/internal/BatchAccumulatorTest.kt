package jraft.raft.internal

import io.kotest.core.spec.style.FreeSpec
import jraft.clients.consumer.internals.FakeTime
import jraft.common.memory.SimpleMemoryPool
import jraft.common.message.LeaderChangeMessage
import jraft.common.serialization.StringSerde

class BatchAccumulatorTest : FreeSpec({
    val time = FakeTime()

    fun buildAccumulator(
        leaderEpoch: Int,
        baseOffset: Long,
        lingerMs: Long,
        maxBatchSize: Int,
    ): BatchAccumulator<String> {
        return BatchAccumulator(
            serde = StringSerde(),
            memoryPool = SimpleMemoryPool(sizeBytes = 1024, strict = true),
            lingerMs = lingerMs,
            time = time,
            nextOffset = 0,
        )
    }

    "testLeaderChangeMessageWritten" {
        val acc = buildAccumulator(
            leaderEpoch = 1,
            baseOffset = 0,
            lingerMs = 50,
            maxBatchSize = 512,
        )

        acc.appendLeaderChangeMessage(LeaderChangeMessage.Empty, time.milliseconds())
    }
})
