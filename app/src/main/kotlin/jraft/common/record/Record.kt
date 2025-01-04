package jraft.common.record

import jraft.common.header.Header
import java.nio.ByteBuffer

interface Record {
    companion object {
        val EMPTY_HEADERS = emptyList<Header>()
    }

    fun offset(): Long

    fun sequence(): Int

    // get the size in bytes of the record
    fun sizeInBytes(): Int

    fun timestamp(): Long

    fun keySize(): Int

    fun hasKey(): Boolean

    fun key(): ByteBuffer?

    fun valueSize(): Int

    fun hasValue(): Boolean

    fun value(): ByteBuffer?

    fun isCompressed(): Boolean

    fun hasTimestampType(type: TimestampType): Boolean

    fun headers(): List<Header>
}
