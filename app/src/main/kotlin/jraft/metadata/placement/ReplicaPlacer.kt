package jraft.metadata.placement

interface ReplicaPlacer {
    fun place(
        placement: PlacementSpec,
        cluster: ClusterDescriber,
    ): TopicAssignment
}
