package jraft.clients.consumer

import jraft.clients.ClientResponse
import jraft.clients.FetchSessionHandler
import jraft.clients.consumer.internals.Fetch
import jraft.clients.consumer.internals.FetchBuffer
import jraft.clients.consumer.internals.FetchCollector
import jraft.clients.consumer.internals.WakeupTrigger
import jraft.clients.consumer.internals.evetns.ApplicationEventHandler
import jraft.clients.consumer.internals.evetns.PollEvent
import jraft.clients.consumer.internals.metrics.KafkaConsumerMetrics
import jraft.common.Node
import jraft.common.TopicPartition
import jraft.common.utils.Time
import jraft.common.utils.Timer
import kotlin.time.Duration

class Consumer<K, V>(
    val time: Time,
    val subscription: SubscriptionState,
    val fetchCollector: FetchCollector<K, V>,
    val applicationEventHandler: ApplicationEventHandler,
    val kafkaConsumerMetrics: KafkaConsumerMetrics,
    val wakeupTrigger: WakeupTrigger = WakeupTrigger(),
) {
    companion object {
        fun create() {
        }
    }

    private val fetchBuffer: FetchBuffer = FetchBuffer()

    fun subscribe(topics: Set<String>, listener: ConsumerRebalanceListener) {
        subscription.subscribe(topics, listener)
    }

    fun poll(timeout: Duration) {
        acquireAndEnsureOpen()

        val timer = time.timer(timeout)
        try {
            kafkaConsumerMetrics.recordPollStart(time.milliseconds())

            while (timer.notExpired()) {
                wakeupTrigger.wakeup()

                applicationEventHandler.add(PollEvent(pollTimeMs = time.milliseconds()))

                val fetch = pollForFetches(timer)
            }
        } finally {
            kafkaConsumerMetrics.recordPollEnd(time.milliseconds())
        }
    }

    private fun pollForFetches(timer: Timer) {
        val pollTimeOut = Math.min(
            applicationEventHandler.maximumTimeToWait(),
            timer.remainingMs(),
        )

        val fetch = collectFetch()
    }

    private fun collectFetch(): Fetch<K, V> {
        val fetch = fetchCollector.collectFetch(fetchBuffer)

        applicationEventHandler.wakeupNetworkThread()

        return fetch
    }

    fun commitAsync(
        offsets: Map<TopicPartition, OffsetAndMetadata> = subscription.allConsumed(),
        callback: OffsetCommitCallback? = null,
    ) {
    }

    private fun updateAssignmentMetadataIfNeeded() {
    }

    /**
     * closed 상태인지 확인한다.
     * multithread 환경을 방지한다.
     */
    private fun acquireAndEnsureOpen() {
    }
}

class Fetcher(
    val subscription: SubscriptionState,
    val fetchBuffer: FetchBuffer,
) {
    fun senFetch() {
        val fetchRequests = prepareFetchRequest()
    }

    private fun prepareFetchRequest(): Map<Node, FetchSessionHandler.FetchRequestData> {
        return emptyMap()
    }

    private fun sendFetchesInternal(
        fetchRequests: Map<Node, FetchSessionHandler.FetchRequestData>,
        successHandler: ResponseHandler<ClientResponse>,
        errorHandler: ResponseHandler<Throwable>,
    ) {
        for (entry in fetchRequests.entries) {
            val target = entry.key
            val data = entry.value
        }
    }

    interface ResponseHandler<T> {
        fun handleResponse(
            target: Node,
            data: FetchSessionHandler.FetchRequestData,
            response: T,
        ) {
        }
    }
}
