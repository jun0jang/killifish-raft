package jraft.common.record

import jraft.common.header.Header
import java.nio.ByteBuffer

interface Record {
    fun offset(): Long

    fun sequence(): Long

    // get the size in bytes of the record
    fun sizeInBytes(): Int

    fun timestamp(): Long

    fun ensueValid(): Boolean

    fun keySize(): Int

    fun hasKey(): Boolean

    fun key(): ByteBuffer

    fun valueSize(): Int

    fun hasValue(): Boolean

    fun hasMagic(magic: Byte): Boolean

    fun isCompressed(): Boolean

    fun hasTimestampType(type: TimestampType): Boolean

    fun headers(): List<Header>
}
