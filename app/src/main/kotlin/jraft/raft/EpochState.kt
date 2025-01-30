package jraft.raft

sealed interface EpochState {
    fun epoch(): Int
}
