package jraft.common

/**
 * S: State
 *
 * round-robin
 */
class PartitionState<S> {
    private val map = LinkedHashMap<TopicPartition, S>()

    private var size: Int = 0

    fun moveToEnd(tp: TopicPartition) {
        val state = map.remove(tp)
        if (state != null) {
            map[tp] = state
        }
    }

    fun update(
        tp: TopicPartition,
        state: S,
    ) {
        map[tp] = state
        size = map.size
    }

    fun stateValue(tp: TopicPartition): S? {
        return map[tp]
    }
}
