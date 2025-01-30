package jraft.raft.internal

import jraft.common.memory.MemoryPool
import jraft.common.record.MemoryRecords
import jraft.common.serialization.RecordSerde
import jraft.common.utils.Time
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 축압기란 것 자체가 Batch를 쌓아두고 나중에 drain 할 수 있어야 함.
 */
class BatchAccumulator<T> private constructor(
    private val epoch: Int,
    private var nextOffset: Long,
    private val maxBatchSize: Int,
    private val memoryPool: MemoryPool,
    private val time: Time,
    private val serde: RecordSerde<T>,
) {
    private enum class DrainStatus {
        STARTED, FINISHED, NONE
    }

    companion object {
        fun <T> new(
            epoch: Int,
            baseOffset: Long,
            maxBatchSize: Int,
            memoryPool: MemoryPool,
            time: Time,
            serde: RecordSerde<T>,
        ): BatchAccumulator<T> {
            return BatchAccumulator(
                epoch = epoch,
                nextOffset = baseOffset,
                maxBatchSize = maxBatchSize,
                memoryPool = memoryPool,
                time = time,
                serde = serde,
            )
        }
    }

    private val appendLock = ReentrantLock()

    private var currentBatch: BatchBuilder<T>? = null

    @Volatile
    private var drainStatus: DrainStatus = DrainStatus.NONE

    private val completed = ConcurrentLinkedQueue<CompletedBatch<T>>()

    // 해당 값까지 drain이 허용 됨.
    private val drainOffset = AtomicLong(Long.MAX_VALUE)

    fun append(
        epoch: Int,
        records: List<T>,
        delayDrain: Boolean,
    ): Long {
        if (epoch != this.epoch) {
            throw IllegalArgumentException("Invalid epoch $epoch, expected ${this.epoch}")
        }

        return appendLock.withLock {
            // if nextOffset: 0, records.size: 10, lastOffset: 9
            val lastOffset = nextOffset + records.size - 1
            maybeCompleteDrain()

            if (delayDrain) {
                // prevent drain until allowDrain() is called
                drainOffset.compareAndSet(Long.MAX_VALUE, nextOffset)
            }

            val batch = maybeAllocateBatch(records)

            for (record in records) {
                batch.appendRecord(record)
            }

            nextOffset = lastOffset + 1
            lastOffset
        }
    }

    fun drain(): List<CompletedBatch<T>> {
        return drain(drainOffset.get())
    }

    private fun drain(drainOffset: Long): List<CompletedBatch<T>> {
        if (drainStatus == DrainStatus.NONE) {
            drainStatus = DrainStatus.STARTED
        }

        appendLock.withLock {
            maybeCompleteDrain()
        }

        if (drainStatus == DrainStatus.FINISHED) {
            drainStatus = DrainStatus.NONE
            return drainCompleted(drainOffset)
        } else {
            return emptyList()
        }
    }

    private fun drainCompleted(drainOffset: Long): List<CompletedBatch<T>> {
        val res = mutableListOf<CompletedBatch<T>>()

        while (true) {
            val batch = completed.peek()
            if (batch == null || batch.drainable(drainOffset).not()) {
                break
            }

            completed.poll()
            res.add(batch)
        }

        return res
    }

    fun allowDrain() {
        drainOffset.set(Long.MAX_VALUE)
    }

    private fun maybeCompleteDrain() {
        if (drainStatus == DrainStatus.STARTED) {
            currentBatch?.takeIf { it.isEmpty().not() }?.let {
                completeCurrentBatch()
            }

            drainStatus = DrainStatus.FINISHED
        }
    }

    private fun maybeAllocateBatch(records: List<T>): BatchBuilder<T> {
        if (currentBatch == null) {
            startNewBatch()
        }

        if (currentBatch != null) {
            // 추가적으로 필요한 bytes
            val byteNeeds = currentBatch!!.bytesNeeded(records)
            if (byteNeeds != null) {
                completeCurrentBatch()
                startNewBatch()
            }
        }

        return currentBatch!!
    }

    private fun startNewBatch() {
        val buffer = memoryPool.tryAllocate(maxBatchSize)
        if (buffer != null) {
            currentBatch = BatchBuilder.new(
                buffer = buffer,
                serde = serde,
                baseOffset = nextOffset,
                appendTime = time.milliseconds(),
                leaderEpoch = epoch,
                maxBytes = maxBatchSize,
            )
        }
    }

    private fun completeCurrentBatch() {
        val memoryRecords = currentBatch!!.build()

        completed.add(
            CompletedBatch(
                baseOffset = currentBatch!!.baseOffset(),
                numRecords = currentBatch!!.records().size,
                records = currentBatch!!.records(),
                data = memoryRecords,
                memoryPool = memoryPool,
            ),
        )

        currentBatch = null
    }

    data class CompletedBatch<T>(
        val baseOffset: Long,
        val numRecords: Int,
        val records: List<T>,
        val data: MemoryRecords,
        val memoryPool: MemoryPool,
    ) {
        fun drainable(drainOffset: Long): Boolean {
            return baseOffset + numRecords - 1 < drainOffset
        }
    }
}
