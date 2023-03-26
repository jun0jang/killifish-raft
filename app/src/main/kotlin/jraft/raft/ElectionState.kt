package jraft.raft

data class ElectionState(
    val epoch: Int,
    val leaderId: Int?,
    val votedId: Int?,
    val voters: Set<Int>,
) {
    companion object {
        fun withUnknownLeader(
            epoch: Int,
            voters: Set<Int>,
        ): ElectionState {
            return ElectionState(epoch, null, null, voters)
        }

        fun withElectedLeader(
            epoch: Int,
            leaderId: Int,
            voters: Set<Int>,
        ): ElectionState {
            return ElectionState(epoch, leaderId, null, voters)
        }
    }
}
