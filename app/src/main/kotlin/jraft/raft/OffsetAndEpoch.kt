package jraft.raft

data class OffsetAndEpoch(
    val offset: Long,
    // epoch is term in raft
    val epoch: Long,
)
