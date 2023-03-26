package jraft.clients.consumer.internals

import jraft.clients.ClientResponse
import jraft.clients.KafkaClient
import jraft.clients.Metadata
import jraft.clients.RequestCompletionHandler
import jraft.clients.consumer.ConsumerConfig
import jraft.common.Node
import jraft.common.errors.DisconnectException
import jraft.common.logger
import jraft.common.metrics.Metrics
import jraft.common.requests.AbstractRequest
import jraft.common.utils.Time
import jraft.common.utils.Timer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.milliseconds

class NetworkClientDelegate(
    val time: Time,
    private val client: KafkaClient,
    private val requestTimeoutMs: Int,
    private val unsentRequests: Queue<UnsentRequest>,
    private val retryBackoffMs: Long,
) {
    private val log = logger<NetworkClientDelegate>()

    fun poll(
        timeoutMs: Long,
        currentTimeMs: Long,
    ) {
        trySend(currentTimeMs)

        var pollTimeoutMs = timeoutMs
        if (unsentRequests.isNotEmpty()) {
            pollTimeoutMs = minOf(retryBackoffMs, pollTimeoutMs)
        }
        client.poll(pollTimeoutMs, currentTimeMs)

        checkDisconnect(currentTimeMs)
    }

    private fun trySend(currentTimeMs: Long) {
        val iter = unsentRequests.iterator()

        while (iter.hasNext()) {
            val unsent = iter.next()

            val isExpired = unsent.timer?.isExpired() ?: false
            if (isExpired) {
                // queue 자체에서 지워버림.
                iter.remove()
                unsent.handler.onFailure(
                    currentTimeMs,
                    TimeoutException("Faield to send request after ${unsent.timer?.timeoutMs}ms"),
                )
                continue
            }

            val successSend = doSend(unsent, currentTimeMs)
            if (successSend) {
                iter.remove()
            }
        }
    }

    private fun doSend(r: UnsentRequest, currentTimeMs: Long): Boolean {
        val node = r.node ?: client.leastLoadedNode(currentTimeMs)
        if (node == null || nodeUnavailable(node)) {
            return false
        }
        if (client.ready(node, currentTimeMs).not()) {
            return false
        }

        // UnsentRequest to ClientRequest
        val request = client.newClientRequest(
            nodeId = node.id.toString(),
            requestBuilder = r.requestBuilder,
            createdTimeMs = currentTimeMs,
            expectResponse = true,
            requestTimeoutMs = r.timer?.remainingMs() ?: Long.MAX_VALUE,
            callback = r.handler,
        )
        client.send(request, currentTimeMs)

        return true
    }

    private fun nodeUnavailable(node: Node): Boolean {
        return false
    }

    private fun checkDisconnect(currentTimeMs: Long) {
        val iter = unsentRequests.iterator()
    }

    fun addAll(pollResult: PollResult): Long {
        for (request in pollResult.unsentRequests) {
            request.timer = time.timer(requestTimeoutMs.milliseconds)
            unsentRequests.add(request)
        }
        return pollResult.timeUntilNextPollMs
    }

    class PollResult(
        val timeUntilNextPollMs: Long,
        val unsentRequests: List<UnsentRequest>,
    )

    class UnsentRequest(
        val requestBuilder: AbstractRequest.Builder<*>,
        val handler: FutureCompletionHandler = FutureCompletionHandler(),
        val node: Node?,
        var timer: Timer? = null,
    )

    class FutureCompletionHandler(
        private val future: CompletableFuture<ClientResponse> = CompletableFuture(),
    ) : RequestCompletionHandler {
        private var responseCompletionTimeMs: Long? = null

        override fun onComplete(response: ClientResponse) {
            val completionTimeMs = response.receivedTimeMs
            if (response.authenticationException != null) {
                onFailure(completionTimeMs, response.authenticationException)
            } else if (response.wasDisconnected()) {
                onFailure(completionTimeMs, DisconnectException.INSTANCE)
            } else if (response.versionMismatch != null) {
                onFailure(completionTimeMs, response.versionMismatch)
            } else {
                responseCompletionTimeMs = completionTimeMs
                future.complete(response)
            }
        }

        fun onFailure(
            currentTimeMs: Long,
            e: Throwable,
        ) {
            responseCompletionTimeMs = currentTimeMs
            future.completeExceptionally(e)
        }
    }

    companion object {
        fun supplier(
            time: Time,
            metaData: Metadata,
            config: ConsumerConfig,
            metrics: Metrics,

        ) {
        }
    }
}
