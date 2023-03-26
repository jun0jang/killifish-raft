package jraft.clients

import jraft.common.Node
import jraft.common.errors.DisconnectException
import jraft.common.requests.AbstractRequest

class NetworkClient(
    private val metadataUpdater: MetadataUpdater,
    private val clientId: String,
    private var state: State,
) : KafkaClient {
    private var correlation = 0

    enum class State {
        ACTIVE,
        CLOSING,
        CLOSED,
    }

    override fun leastLoadedNode(currentTimeMs: Long): Node? {
        return null
    }

    override fun newClientRequest(
        nodeId: String,
        requestBuilder: AbstractRequest.Builder<*>,
        createdTimeMs: Long,
        expectResponse: Boolean,
        requestTimeoutMs: Long,
        callback: RequestCompletionHandler,
    ): ClientRequest {
        return ClientRequest(
            destination = nodeId,
            requestBuilder = requestBuilder,
            correlationId = nextCorrelationId(),
            clientId = clientId,
            expectResponse = expectResponse,
            requestTimeoutMs = requestTimeoutMs,
            callback = callback,
        )
    }

    override fun ready(node: Node, currentTimeMs: Long): Boolean {
        if (node.isEmpty()) {
            throw IllegalArgumentException("Cannot connect to empty node $node")
        }

        return true
    }

    // visible for testing
    fun nextCorrelationId(): Int {
        return correlation++
    }

    override fun send(
        clientRequest: ClientRequest,
        currentTimeMs: Long,
    ) {
        doSend(clientRequest, false, currentTimeMs)
    }

    private fun doSend(
        clientRequest: ClientRequest,
        isInternalRequest: Boolean,
        currentTimeMs: Long,
    ) {
        ensureActive()

        val nodeId = clientRequest.destination
        if (isInternalRequest.not()) {
            if (canSendRequest(nodeId, currentTimeMs).not()) {
                throw IllegalStateException("Attempt to send a request to node $nodeId which is not ready.")
            }
        }

        val builder = clientRequest
    }

    private fun canSendRequest(nodeId: String, currentTimeMs: Long): Boolean {
        return true
    }

    override fun poll(timeoutMs: Long, currentTimeMs: Long) {
    }

    fun active(): Boolean {
        return state == State.ACTIVE
    }

    private fun ensureActive() {
        if (active().not()) {
            throw DisconnectException("Network client is no longer active, state is $state")
        }
    }

    class DefaultMetadataUpdater(
        private val metadata: Metadata,
        private val inProgress: InProgressData? = null,
    ) : MetadataUpdater {

        override fun isUpdateDue(): Boolean {
            return metadata.timeToNextUpdate() == 0L
        }

        class InProgressData(
            val requestVersion: Int,
            val isPartialUpdate: Boolean,
        )
    }
}
