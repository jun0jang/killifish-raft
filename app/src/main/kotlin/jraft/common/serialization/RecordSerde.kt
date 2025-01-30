package jraft.common.serialization

import jraft.common.protocol.Readable
import jraft.common.protocol.Writable

interface RecordSerde<T> {
    fun recordSize(data: T): Int

    fun write(data: T, out: Writable)

    fun read(input: Readable, size: Int): T
}
