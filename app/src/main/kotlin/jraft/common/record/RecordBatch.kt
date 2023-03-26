package jraft.common.record

import java.lang.Record

interface RecordBatch : Iterable<Record> {
    fun checkSum(): Long

    fun nextOffset(): Long
}
