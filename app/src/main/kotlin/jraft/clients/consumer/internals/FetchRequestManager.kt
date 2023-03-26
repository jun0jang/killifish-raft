package jraft.clients.consumer.internals

import jraft.clients.consumer.SubscriptionState
import jraft.common.TopicPartition

class FetchRequestManager(
    private val fetchBuffer: FetchBuffer,
    private val subscription: SubscriptionState,
) : RequestManager {

    override fun poll(currentTimeMs: Long): NetworkClientDelegate.PollResult {
        throw NotImplementedError()
    }

    fun prepareRequest() {
        // 패치할 tp 계산.

//        subscription.isFetchable()

        subscription
    }

    /**
     * FetchBuffer에 존재하지 않으면서, assignment된 Parition 계산
     */
    private fun fetchablePartitions(): List<TopicPartition> {
        fetchBuffer

        return emptyList()
    }
}
