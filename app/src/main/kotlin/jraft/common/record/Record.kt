package jraft.common.record

interface Record {

    fun offset(): Long

    fun sequence(): Long

    // get the size in bytes of the record
    fun sizeInBytes(): Int
}
