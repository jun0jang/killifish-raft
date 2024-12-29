package jraft.common.header.internals

import jraft.common.header.Header

data class RecordHeader(
    private val key: String,
    private val value: ByteArray?,
) : Header {
    override fun key(): String = key

    override fun value(): ByteArray? = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecordHeader

        if (key != other.key) return false
        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + (value?.contentHashCode() ?: 0)
        return result
    }
}
