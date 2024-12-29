package jraft.common.message

import java.util.UUID

class CreateTopicsResponseData {
    data class CreatableTopicResult(
        val name: String,
        val topicId: UUID,
        val errorCode: Short?,
        val errorMessage: String?,
        val numPartitions: Int,
        val replicationFactor: Short,
    )
}
