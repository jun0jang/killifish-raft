package jraft.common.message

class CreateTopicsRequestData(
    val topics: List<CreatableTopic>,
) {
    class CreatableTopic(
        val name: String,
        val numPartitions: Int,
        val replicationFactor: Short,
        val assignments: List<CreatableReplicaAssignment>,
    )

    class CreatableReplicaAssignment(
        val partitionIndex: Int,
        val brokerIds: List<Int>,
    )
}
