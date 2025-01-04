package jraft.common.record

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import jraft.common.header.internals.RecordHeader
import jraft.common.utils.ByteBufferOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

class DefaultRecordTest : FreeSpec({
    "testBasicSerde" {
        val headers = listOf(
            RecordHeader("foo", "value".toByteArray()),
            RecordHeader("bar", null),
            RecordHeader("\"A\\u00ea\\u00f1\\u00fcC\"", "value".toByteArray()),
        )

        val records = listOf(
            SimpleRecord.new(
                key = ByteBuffer.wrap("hi".toByteArray()),
                value = ByteBuffer.wrap("there".toByteArray()),
            ),
            SimpleRecord.new(
                key = null,
                value = ByteBuffer.wrap("there".toByteArray()),
            ),
            SimpleRecord.new(
                key = ByteBuffer.wrap("hi".toByteArray()),
                value = null,
            ),
            SimpleRecord.new(
                key = null as ByteBuffer?,
                value = null,
            ),
            SimpleRecord.new(
                key = ByteBuffer.wrap("hi".toByteArray()),
                value = ByteBuffer.wrap("there".toByteArray()),
                timestamp = 15L,
                headers = headers,
            ),
        )

        for (record in records) {
            val baseSequence = 723
            val baseOffset = 37L
            val offsetDelta = 10
            val baseTimestamp = System.currentTimeMillis()
            val timestampDelta = 323L

            val out = ByteBufferOutputStream.new(1024)
            DefaultRecord.writeTo(
                out = DataOutputStream(out),
                offsetDelta = offsetDelta,
                timestampDelta = timestampDelta,
                key = record.key,
                value = record.value,
                headers = record.headers,
            )
            val buffer = out.buffer()
            buffer.flip()

            val logRecord = DefaultRecord.readFrom(buffer = buffer, baseOffset = baseOffset, baseTimestamp = baseTimestamp, baseSequence = baseSequence, logAppendTime = null)

            logRecord.offset shouldBe baseOffset + offsetDelta
            logRecord.sequence shouldBe baseSequence + offsetDelta
            logRecord.timestamp shouldBe baseTimestamp + timestampDelta
            record.key shouldBe logRecord.key
            record.value shouldBe logRecord.value
            record.headers shouldBe logRecord.headers
        }
    }
})
