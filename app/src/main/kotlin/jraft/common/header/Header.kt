package jraft.common.header

interface Header {
    fun key(): String
    fun value(): ByteArray?
}
