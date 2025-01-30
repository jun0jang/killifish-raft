package jraft.raft

/**
 * This class is responsible for manging the current EpochState
 */
class QuorumState(
    private val nodeId: Int,
    private val store: QuorumStateStore,
    private val voters: Set<Int>,
) {
    fun initialize(logEndOffsetAndEpoch: OffsetAndEpoch) {
        val election = ElectionState.withUnknownLeader(
            epoch = 0,
            voters = voters,
        )
    }

    fun maybeLeaderState() {
    }
}
