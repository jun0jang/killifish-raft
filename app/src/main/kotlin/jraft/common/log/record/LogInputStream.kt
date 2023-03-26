package jraft.common.log.record

interface LogInputStream {

    fun nextBatch(): RecordBatch?
}
