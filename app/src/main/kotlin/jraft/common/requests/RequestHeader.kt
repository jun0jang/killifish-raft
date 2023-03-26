package jraft.common.requests

import jraft.common.protocol.ApiKeys

data class RequestHeader(
    val requestApiKey: ApiKeys,
    val clientId: String,
    val correlationId: Int,
)
