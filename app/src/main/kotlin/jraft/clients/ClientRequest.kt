package jraft.clients

import jraft.common.requests.AbstractRequest

class ClientRequest(
    val destination: String,
    val requestBuilder: AbstractRequest.Builder<*>,
    val correlationId: Int,
    val clientId: String,
    // todo what does it mean
    val expectResponse: Boolean,
    val requestTimeoutMs: Long,
    val callback: RequestCompletionHandler,
)
