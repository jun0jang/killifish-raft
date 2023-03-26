package jraft.message

import java.nio.ByteBuffer

class VoteRequest(
    term: Long,
    candidateId: Int,
    // log index, log term
) : Message {
    override fun toByteBuffer(): ByteBuffer {
        TODO("Not yet implemented")
    }
}
