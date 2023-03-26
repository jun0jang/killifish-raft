package jraft.common.log.record

import java.lang.Record

interface RecordBatch : Iterable<Record> {
    fun checkSum(): Long

    fun nextOffset(): Long
}
