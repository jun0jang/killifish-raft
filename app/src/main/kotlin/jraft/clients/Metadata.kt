package jraft.clients

import jraft.common.Node

/**
 * ThreadSafe 해야함.
 * TODO write reason
 */
open class Metadata(
    private var needFullUpdate: Boolean = false,
    private var needPartialUpdate: Boolean = false,
) {

    @Synchronized
    fun timeToNextUpdate(): Long {
        return 0
    }

    fun updateRequested(): Boolean {
        return needFullUpdate || needPartialUpdate
    }

    fun newMetadataRequestBuilder() {
    }

    class LeaderAndEpoch(
        val leader: Node?,
        val epoch: Long?,
    )
}
