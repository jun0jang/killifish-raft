package jraft.raft

class LeaderState<T>(
    private val epoch: Int,
) : EpochState {
    override fun epoch(): Int {
        return epoch
    }
}
