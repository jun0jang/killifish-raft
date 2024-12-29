package jraft.common.serialization

import jraft.common.protocol.ApiMessageAndVersion
import jraft.common.protocol.Readable
import jraft.common.protocol.Writable

class ApiMessageSerde : RecordSerde<ApiMessageAndVersion> {
    override fun write(data: ApiMessageAndVersion, out: Writable) {
        TODO("Not yet implemented")
    }

    override fun read(input: Readable, size: Int): ApiMessageAndVersion {
        TODO("Not yet implemented")
    }
}
