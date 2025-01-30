package jraft.raft

import jraft.common.protocol.ApiMessage
import java.util.function.Supplier

/**
 *
 */
class RaftClient<T>(
    nodeId: Int,
    store: QuorumStateStore,
) {
    private val quorum = QuorumState(
        nodeId = nodeId,
        store = store,
        voters = emptySet(),
    )

    fun prepareAppend(epoch: Int, records: List<T>) {
        quorum
    }

    fun poll() {
    }

    private fun maybeSendRequest(
        destinationId: Int,
        requestSupplier: Supplier<ApiMessage>,
    ) {
    }
}
