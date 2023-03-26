package jraft.raft

interface QuorumStateStore {
    fun readElectionState(): ElectionState

    fun writeElectionState(latest: ElectionState)
}
