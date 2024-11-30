package jraft.metadata

class PartitionRegistration(
    val replicas: List<Int>,
    val isr: List<Int>,
    val leader: Int,
    val leaderEpoch: Int,
    val partitionEpoch: Int,
)
