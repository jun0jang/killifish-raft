package jraft.metadata.placement

class TopicAssignment(
    private val assignments: List<PartitionAssignment>,
) {
    fun assignments(): List<PartitionAssignment> {
        return assignments
    }
}
