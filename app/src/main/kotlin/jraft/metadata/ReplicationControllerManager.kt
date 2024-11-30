package jraft.metadata

import jraft.common.protocol.Errors
import jraft.message.CreateTopicsRequestData
import jraft.message.CreateTopicsResponseData
import jraft.metadata.placement.KRaftClusterDescriber
import jraft.metadata.placement.PlacementSpec
import java.util.UUID

class ReplicationControllerManager(
    // 현재 존재하는 모든 토픽의 이름과 UUID를 가지고 있는 맵
    private val topicsByName: Map<String, UUID>,
    private val clusterControl: ClusterControllerManager,
) {
    private val clusterDescriber = KRaftClusterDescriber(clusterControl)

    fun createTopics(request: CreateTopicsRequestData) {
        // 토픽 이름이 이미 존재하는지 확인
        request.topics.forEach { topic ->
            if (topicsByName.containsKey(topic.name)) {
                throw IllegalArgumentException("Topic with name ${topic.name} already exists")
            }
        }

        val success = mutableMapOf<String, CreateTopicsResponseData.CreatableTopicResult>()

        for (topic in request.topics) {
            createTopic(topic = topic, success = success)
        }
    }

    private fun createTopic(
        topic: CreateTopicsRequestData.CreatableTopic,
        success: MutableMap<String, CreateTopicsResponseData.CreatableTopicResult>,
    ) {
        val newParts = mutableMapOf<Int, PartitionRegistration>()

        val topicAssignment = clusterControl.replicaPlacer.place(
            PlacementSpec(
                startPartition = 0,
                numPartitions = topic.numPartitions,
                numReplicas = topic.replicationFactor,
            ),
            clusterDescriber,
        )

        for ((index, partitionAssignment) in topicAssignment.assignments().withIndex()) {
            val isr = partitionAssignment.replicas.filter {
                clusterControl.isActive(it)
            }

            newParts[index] = PartitionRegistration(
                replicas = partitionAssignment.replicas,
                isr = isr,
                leader = isr.first(),
                leaderEpoch = 0,
                partitionEpoch = 0,
            )
        }

        val topicId = UUID.randomUUID()

        CreateTopicsResponseData.CreatableTopicResult(
            name = topic.name,
            topicId = topicId,
            errorCode = Errors.NONE.code,
            errorMessage = null,
            numPartitions = newParts.size,
            replicationFactor = newParts.values.iterator().next().replicas.size.toShort(),
        )
    }
}
