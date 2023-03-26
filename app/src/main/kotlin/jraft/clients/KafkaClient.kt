package jraft.clients

import jraft.common.Node
import jraft.common.requests.AbstractRequest

interface KafkaClient {
    fun poll(timeoutMs: Long, now: Long)

    fun send(clientRequest: ClientRequest, currentTimeMs: Long)

    fun ready(node: Node, currentTimeMs: Long): Boolean

    fun newClientRequest(
        nodeId: String,
        requestBuilder: AbstractRequest.Builder<*>,
        createdTimeMs: Long,
        expectResponse: Boolean,
        requestTimeoutMs: Long,
        callback: RequestCompletionHandler,
    ): ClientRequest

    fun leastLoadedNode(currentTimeMs: Long): Node?
}
