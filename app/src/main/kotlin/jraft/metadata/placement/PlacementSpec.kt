package jraft.metadata.placement

/**
 * Specifies a replica placement that we want to make.
 */
class PlacementSpec(
    val startPartition: Int,
    val numPartitions: Int,
    val numReplicas: Short,
)
