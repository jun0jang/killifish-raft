package jraft.common.record

class RecordBatchIterator(
    private val logInputStream: ByteBufferLogInputStream,
) : Iterator<RecordBatch> {
    private var stopped = false

    private var nextBatch: RecordBatch? = null

    override fun hasNext(): Boolean {
        if (stopped) return false

        if (nextBatch == null) {
            nextBatch = logInputStream.nextBatch()
        }
        if (nextBatch == null) {
            stopped = true
        }

        return nextBatch != null
    }

    override fun next(): RecordBatch {
        if (!hasNext()) {
            throw NoSuchElementException()
        }

        val batch = nextBatch!!
        nextBatch = null

        return batch
    }
}
