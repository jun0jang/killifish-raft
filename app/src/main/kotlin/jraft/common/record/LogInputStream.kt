package jraft.common.record

interface LogInputStream {

    fun nextBatch(): RecordBatch?
}
