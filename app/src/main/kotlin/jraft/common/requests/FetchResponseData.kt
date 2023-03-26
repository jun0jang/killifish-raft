package jraft.common.requests

import java.util.UUID

class FetchResponseData(
    val throttleTimeMs: Int,
    val errorCode: Short,
    val sessionId: Int,
    val responses: List<FetchableTopicResponse>,
    val nodeEndpoints: NodeEndpointCollection,
) {

    class PartitionData {

        fun errorCode(): Short {
            return 0
        }
    }

    data class FetchableTopicResponse(
        val topic: String,
        val topicId: UUID,
        val partitionData: List<PartitionData>,
    )

    data class NodeEndpointCollection(
        val expectedNumElements: Int,
    )
}
