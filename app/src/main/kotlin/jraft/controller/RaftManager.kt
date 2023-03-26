package jraft.controller

import jraft.raft.OffsetAndEpoch
import jraft.raft.QuorumState

class RaftManager(
    private val quorumState: QuorumState,
) : Runnable {
    fun start() {
        quorumState.initialize(OffsetAndEpoch(0, 0))
    }

    /**
     * Raft 상태 조정
     * kafka's RaftIoThread & client.poll
     */
    override fun run() {
        // raft 상태 조정
//        if (quorumState.isFollower) {
//            // fetch snapshot
//        } else if (quorumState.isCandidate) {
//            // send vote request
//        } else if (quorumState.isLeader) {
//            // append entries
//        }

        // response enqueue to message queue
        // handle response
    }
}
