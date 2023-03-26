package jraft.raft

import jraft.common.protocol.ApiMessage
import java.util.function.Supplier

/**
 *
 */
class RaftClient(
    nodeId: Int,
    store: QuorumStateStore,
) {
    private val quorum = QuorumState(
        nodeId = nodeId,
        store = store,
        voters = emptySet(),
    )

    fun poll() {
    }

    private fun maybeSendRequest(
        destinationId: Int,
        requestSupplier: Supplier<ApiMessage>,
    ) {
    }
}
