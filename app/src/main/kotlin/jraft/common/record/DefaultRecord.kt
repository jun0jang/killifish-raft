package jraft.common.record

import jraft.common.header.Header
import jraft.common.header.internals.RecordHeader
import jraft.common.utils.ByteUtils
import jraft.common.utils.Utils
import java.io.DataOutputStream
import java.nio.ByteBuffer

class DefaultRecord(
    val sizeInBytes: Int,
    val attributes: Byte,
    val offset: Long,
    val timestamp: Long,
    val sequence: Int,
    val key: ByteBuffer?,
    val value: ByteBuffer?,
    val headers: List<Header>,
) : Record {
    override fun offset(): Long {
        return offset
    }

    override fun sequence(): Int {
        return sequence
    }

    override fun sizeInBytes(): Int {
        return sizeInBytes
    }

    override fun timestamp(): Long {
        return timestamp
    }

    override fun keySize(): Int {
        return key?.remaining() ?: -1
    }

    override fun hasKey(): Boolean {
        return key != null
    }

    override fun key(): ByteBuffer? {
        return key?.duplicate()
    }

    override fun valueSize(): Int {
        return value?.remaining() ?: -1
    }

    override fun hasValue(): Boolean {
        return value != null
    }

    override fun value(): ByteBuffer? {
        return value?.duplicate()
    }

    override fun isCompressed(): Boolean {
        return false
    }

    override fun hasTimestampType(type: TimestampType): Boolean {
        return false
    }

    override fun headers(): List<Header> {
        return headers
    }

    companion object {
        private val NULL_VARINT_SIZE_BYTES = ByteUtils.sizeOfVarint(-1)

        fun writeTo(
            out: DataOutputStream,
            offsetDelta: Int,
            timestampDelta: Long,
            key: ByteBuffer?,
            value: ByteBuffer?,
            headers: List<Header>,
        ): Int {
            val sizeOfBodyInBytes = sizeOfBodyInBytes(offsetDelta, timestampDelta, key, value, headers)
            ByteUtils.writeVarint(sizeOfBodyInBytes, out)

            val attributes: Byte = 0 // there are no used record attributes at the moment
            out.writeByte(attributes.toInt())

            ByteUtils.writeVarLong(timestampDelta, out)
            ByteUtils.writeVarint(offsetDelta, out)

            if (key == null) {
                ByteUtils.writeVarint(-1, out)
            } else {
                val keySize = key.remaining()
                ByteUtils.writeVarint(keySize, out)
                Utils.writeTo(out, key, keySize)
            }

            if (value == null) {
                ByteUtils.writeVarint(-1, out)
            } else {
                val valueSize = value.remaining()
                ByteUtils.writeVarint(valueSize, out)
                Utils.writeTo(out, value, valueSize)
            }

            ByteUtils.writeVarint(headers.size, out)

            for (header in headers) {
                val headerKey = header.key()

                val utf8Bytes = ByteUtils.utf8(headerKey)
                ByteUtils.writeVarint(utf8Bytes.size, out)
                out.write(utf8Bytes)

                val headerValue = header.value()
                if (headerValue == null) {
                    ByteUtils.writeVarint(-1, out)
                } else {
                    ByteUtils.writeVarint(headerValue.size, out)
                    out.write(headerValue)
                }
            }

            return ByteUtils.sizeOfVarint(sizeOfBodyInBytes) + sizeOfBodyInBytes
        }

        fun readFrom(
            buffer: ByteBuffer,
            baseOffset: Long,
            baseTimestamp: Long,
            baseSequence: Int,
            logAppendTime: Long?,
        ): DefaultRecord {
            val sizeOfBodyInBytes = ByteUtils.readVarint(buffer)

            return readFrom(
                buffer = buffer,
                baseOffset = baseOffset,
                baseTimestamp = baseTimestamp,
                baseSequence = baseSequence,
                logAppendTime = logAppendTime,
                sizeOfBodyInBytes = sizeOfBodyInBytes,
            )
        }

        private fun readFrom(
            buffer: ByteBuffer,
            baseOffset: Long,
            baseTimestamp: Long,
            baseSequence: Int,
            logAppendTime: Long?,
            sizeOfBodyInBytes: Int,
        ): DefaultRecord {
            if (buffer.remaining() < sizeOfBodyInBytes) {
                throw IllegalArgumentException("Insufficient data to read a record")
            }

            val recordStart = buffer.position()
            val attributes = buffer.get()

            val timestampDelta = ByteUtils.readVarLong(buffer)
            var timestamp = baseTimestamp + timestampDelta
            if (logAppendTime != null) {
                timestamp = logAppendTime
            }

            val offsetDelta = ByteUtils.readVarint(buffer)
            val offset = baseOffset + offsetDelta
            val sequence = if (baseSequence >= 0) {
                baseSequence + offsetDelta
            } else {
                -1
            }

            val keySize = ByteUtils.readVarint(buffer)
            val key = Utils.readBytes(buffer, keySize)

            val valueSize = ByteUtils.readVarint(buffer)
            val value = Utils.readBytes(buffer, valueSize)

            val numHeaders = ByteUtils.readVarint(buffer)

            val headers = mutableListOf<Header>()
            for (i in 0 until numHeaders) {
                val headerKeySize = ByteUtils.readVarint(buffer)
                val headerKeyBuffer = Utils.readBytes(buffer, headerKeySize)!!

                val headerValueSize = ByteUtils.readVarint(buffer)
                val headerValueBuffer = Utils.readBytes(buffer, headerValueSize)

                val header = RecordHeader(
                    key = ByteUtils.utf8(headerKeyBuffer, headerKeySize),
                    value = headerValueBuffer?.let { ByteUtils.toArray(it) },
                )
                headers.add(header)
            }

            if (buffer.position() - recordStart != sizeOfBodyInBytes) {
                throw IllegalArgumentException("Record size in header ($sizeOfBodyInBytes) does not match actual record size (${buffer.position() - recordStart})")
            }

            val totalSizeInBytes = ByteUtils.sizeOfVarint(sizeOfBodyInBytes) + sizeOfBodyInBytes
            return DefaultRecord(
                sizeInBytes = totalSizeInBytes,
                attributes = attributes,
                offset = offset,
                timestamp = timestamp,
                sequence = sequence,
                key = key,
                value = value,
                headers = headers,
            )
        }

        private fun sizeOfBodyInBytes(
            offsetDelta: Int,
            timestampDelta: Long,
            key: ByteBuffer?,
            value: ByteBuffer?,
            headers: List<Header>,
        ): Int {
            val keySize = key?.remaining() ?: -1
            val valueSize = value?.remaining() ?: -1
            return sizeOfBodyInBytes(offsetDelta, timestampDelta, keySize, valueSize, headers)
        }

        private fun sizeOfBodyInBytes(
            offsetDelta: Int,
            timestampDelta: Long,
            keySize: Int,
            valueSize: Int,
            headers: List<Header>,
        ): Int {
            // always one byte for attributes
            var size = 1
            size += ByteUtils.sizeOfVarint(offsetDelta)
            size += ByteUtils.sizeOfVarLong(timestampDelta)
            size += sizeOf(keySize, valueSize, headers)
            return size
        }

        private fun sizeOf(
            keySize: Int,
            valueSize: Int,
            headers: List<Header>,
        ): Int {
            var size = 0
            size += if (keySize < 0) {
                NULL_VARINT_SIZE_BYTES
            } else {
                ByteUtils.sizeOfVarint(keySize) + keySize
            }

            size += if (valueSize < 0) {
                NULL_VARINT_SIZE_BYTES
            } else {
                ByteUtils.sizeOfVarint(valueSize) + valueSize
            }

            size += ByteUtils.sizeOfVarint(headers.size)

            for (header in headers) {
                val headerKey = header.key()
                val headerKeySize = ByteUtils.utf8Length(headerKey)
                size += ByteUtils.sizeOfVarint(headerKeySize) + headerKeySize

                val headerValue = header.value()
                size += if (headerValue == null) {
                    NULL_VARINT_SIZE_BYTES
                } else {
                    ByteUtils.sizeOfVarint(headerValue.size) + headerValue.size
                }
            }

            return size
        }
    }
}
