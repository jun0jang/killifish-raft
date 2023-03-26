package jraft.clients.consumer.internals

import jraft.clients.consumer.SubscriptionState
import jraft.common.logger
import jraft.common.protocol.Errors

class FetchCollector<K, V>(
    val subscription: SubscriptionState,
    val fetchConfig: FetchConfig,
) {
    private val log = logger<FetchCollector<K, V>>()

    /**
     *
     */
    fun collectFetch(fetchBuffer: FetchBuffer): Fetch<K, V> {
        val fetch = Fetch.empty<K, V>()
        var recordsRemaining = fetchConfig.maxPollRecords

        while (true) {
            val nextInLineFetch = fetchBuffer.nextInLineFetch()

            // null 혹은 이미 소비된 fetch일 경우 다음으로 소비될 fetch를 가져온다.
            if (nextInLineFetch == null || nextInLineFetch.isConsumed) {
                val completedFetch = fetchBuffer.peek() ?: break

                if (!completedFetch.isInitialize) {
                    fetchBuffer.setNextInLineFetch(initialize(completedFetch))
                } else {
                    fetchBuffer.setNextInLineFetch(completedFetch)
                }
                // pause된 partition일 경우 다음 fetch는 null 이다.
            } else if (subscription.isPaused(nextInLineFetch.partition)) {
                fetchBuffer.setNextInLineFetch(null)
                // fetch를 수집한다.
            } else {
                val nextFetch = fetchRecords(nextInLineFetch, recordsRemaining)
                fetch.add(nextFetch)

                // TODO: number of records read
                recordsRemaining -= 1
            }

            fetchBuffer.poll()
        }

        return fetch
    }

    private fun fetchRecords(nextInLineFetch: CompletedFetch, maxRecords: Int): Fetch<K, V> {
        val tp = nextInLineFetch.partition

        if (subscription.isAssigned(tp).not()) {
            return Fetch.empty()
        }
        if (subscription.isFetchable(tp).not()) {
            return Fetch.empty()
        }

        val position = subscription.position(tp)
            ?: throw IllegalStateException("Partition $tp is not assigned")

        if (nextInLineFetch.nextFetchOffset != position.offset) {
            log.debug(
                "Ignoring fethced records for {} at offset {} since the current position is {}",
                tp,
                nextInLineFetch.nextFetchOffset,
                position,
            )
            return Fetch.empty()
        }

        nextInLineFetch

        log.trace("Draining fetched records for partition {}", tp)
        nextInLineFetch.drain()

        return Fetch.empty()
    }

    private fun initialize(completedFetch: CompletedFetch): CompletedFetch? {
        val tp = completedFetch.partition
        val error = Errors.forCode(completedFetch.partitionData.errorCode())
        var recordMetrics = true

        try {
            if (subscription.hasValidPosition(tp).not()) {
                return null
            } else if (error == Errors.NONE) {
                val ret = handleInitializeSuccess(completedFetch)
                recordMetrics = ret == null
                return ret
            } else {
                handleInitializeError(completedFetch, error)
                return null
            }
        } finally {
            if (recordMetrics) {
                completedFetch.recordAggregatedMetrics(0, 0)
            }

            if (error != Errors.NONE) {
                // error가 있으면 partition을 끝으로 이동
                // 왜 why.
                subscription.movePartitionToEnd(tp)
            }
        }
    }

    private fun handleInitializeSuccess(completedFetch: CompletedFetch): CompletedFetch? {
        val tp = completedFetch.partition
        val fetchOffset = completedFetch.nextFetchOffset

        val position = subscription.position(tp)
        if (position == null || position.offset != fetchOffset) {
            log.debug(
                "Discarding stale fetch response for partition $tp since its offset $fetchOffset does not match " +
                    "the expected offset $position",
            )
            return null
        }

        val partition = completedFetch.partitionData

        return null
    }

    private fun handleInitializeError(completedFetch: CompletedFetch, error: Errors) {
        val tp = completedFetch.partition

        if (error == Errors.UNKNOWN_TOPIC_OR_PARTITION) {
        } else if (error == Errors.OFFSET_OUT_OF_RANGE) {
            val replicaId = subscription.clearPreferredReadReplica(tp)

            if (replicaId != null) {
                subscription.position(tp)
            }
        }
    }
}
