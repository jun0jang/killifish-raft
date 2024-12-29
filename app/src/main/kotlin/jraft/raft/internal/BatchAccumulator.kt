package jraft.raft.internal

import jraft.common.memory.MemoryPool
import jraft.common.message.LeaderChangeMessage
import jraft.common.record.BatchBuilder
import jraft.common.record.MemoryRecords
import jraft.common.serialization.RecordSerde
import jraft.common.utils.Time
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max

class BatchAccumulator<T>(
    private val serde: RecordSerde<T>,
    private val memoryPool: MemoryPool,
    private val lingerMs: Long,
    private val time: Time,
    private var nextOffset: Long,
) {
    interface MemoryRecordsCreator {
        fun create(
            baseOffset: Long,
            epoch: Int,
            byteBuffer: ByteBuffer,
        ): MemoryRecords
    }

    private val lingerTimer = SimpleTimer()

    private val appendLock = ReentrantLock()

    private var currentBatch: BatchBuilder? = null

    fun append(epoch: Int, records: List<T>) {
        appendLock.withLock {
        }
    }

    fun appendLeaderChangeMessage(
        leaderChangeMessage: LeaderChangeMessage,
        currentMs: Long,
    ) {
    }

    fun needsDrain(currentMs: Long) {
    }

    private fun mayBeCompleteDrain() {
    }

    private fun mayBeResetLinger() {
        if (lingerTimer.isRunning().not()) {
            lingerTimer.reset(time.milliseconds() + lingerMs)
        }
    }

    private class SimpleTimer {
        private val deadlineMs: AtomicLong = AtomicLong(Long.MAX_VALUE)

        fun isRunning(): Boolean {
            return deadlineMs.get() != Long.MAX_VALUE
        }

        fun reset(timeoutMs: Long) {
            deadlineMs.set(timeoutMs)
        }

        fun remainingMs(currentMs: Long): Long {
            return max(0, deadlineMs.get() - currentMs)
        }
    }
}
