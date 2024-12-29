package jraft.common.serialization

import jraft.common.protocol.Readable
import jraft.common.protocol.Writable

class StringSerde : RecordSerde<String> {
    override fun write(data: String, out: Writable) {
        out.writeByteArray(data.toByteArray(Charsets.UTF_8))
    }

    override fun read(input: Readable, size: Int): String {
        return input.readArray(size).toString(Charsets.UTF_8)
    }
}
