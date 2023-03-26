package jraft.clients

import jraft.common.Node
import jraft.common.TopicIdPartition
import jraft.common.TopicPartition
import jraft.common.requests.FetchRequest

class FetchSessionHandler(
    val node: Node,
) {
    class FetchRequestData(
        val toSend: Map<TopicPartition, FetchRequest.PartitionData>,
        val toForget: List<TopicIdPartition>,
        val toReplace: List<TopicIdPartition>,
        val sessionPartitions: Map<TopicPartition, FetchRequest.PartitionData>,
    )
}
