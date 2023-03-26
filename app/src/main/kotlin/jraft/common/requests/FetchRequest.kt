package jraft.common.requests

import jraft.common.protocol.ApiKeys
import jraft.common.protocol.ApiMessage
import java.util.UUID

class FetchRequest(
    apiKeys: ApiKeys,
    version: Short,
    val data: ApiMessage,
) : AbstractRequest(
    apiKeys = apiKeys,
    version = version,
) {
    override fun data(): ApiMessage {
        return data
    }

    class PartitionData(
        val topicId: UUID,
        val fetchOffset: Long,
        val logStartOffset: Long,
        val maxBytes: Int,
        val currentLeaderEpoch: Int?,
        val lastFetchedEpoch: Int?,
    )

    class Builder(
        apiKeys: ApiKeys,
        version: Short,
        latestAvailableVersion: Short,
        var replicaId: Int,
        var maxWait: Int,
    ) : AbstractRequest.Builder<FetchRequest>(
        apiKeys = apiKeys,
        version = version,
        latestAvailableVersion = latestAvailableVersion,
    ) {
        override fun build(version: Short): FetchRequest {
            TODO()
//            return FetchRequest(this, version, data = FetchReques)
        }
    }
}
