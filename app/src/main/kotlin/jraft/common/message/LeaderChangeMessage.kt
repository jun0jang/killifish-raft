package jraft.common.message

import jraft.common.protocol.ApiMessage
import java.util.UUID

class LeaderChangeMessage(
    val version: Int,
    val leaderId: Long,
    val voters: List<Voter>,
    val grantingVoters: List<Voter>,
) : ApiMessage {
    class Voter(
        val voterId: Long,
        val voterDirectoryId: UUID,
    )

    companion object {
        val Empty = LeaderChangeMessage(
            version = 0,
            leaderId = 0,
            voters = emptyList(),
            grantingVoters = emptyList(),
        )
    }

    override val apiKey: Short = -1
}
