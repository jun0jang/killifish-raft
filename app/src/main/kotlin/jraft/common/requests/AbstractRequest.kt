package jraft.common.requests

import jraft.common.protocol.ApiKeys

abstract class AbstractRequest(
    val apiKeys: ApiKeys,
    val version: Short,
) : AbstractRequestResponse {
    abstract class Builder<T : AbstractRequest>(
        var apiKeys: ApiKeys,
        var version: Short,
        var latestAvailableVersion: Short,
    ) {
        fun build(): T {
            return build(latestAvailableVersion)
        }

        abstract fun build(version: Short): T
    }
}
