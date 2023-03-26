package jraft.clients.consumer

import jraft.clients.Metadata
import jraft.common.PartitionState
import jraft.common.TopicPartition
import java.util.Random

class SubscriptionState(
    val defaultResetStrategy: OffsetResetStrategy,
    val assignment: PartitionState<TopicPartitionState> = PartitionState(),
    // the list of topics the user has requested
    var subscription: Set<String> = setOf(),
    var rebalanceListener: ConsumerRebalanceListener? = null,
) {
    fun subscribe(
        topics: Set<String>,
        listener: ConsumerRebalanceListener,
    ) {
        subscription = topics
        rebalanceListener = listener
    }

    fun allConsumed(): Map<TopicPartition, OffsetAndMetadata> {
        return mapOf(
            TopicPartition("topic", Random().nextInt()) to OffsetAndMetadata(),
        )
    }

    fun position(tp: TopicPartition): FetchPosition? {
        return assignment.stateValue(tp)?.position
    }

    fun isPaused(tp: TopicPartition): Boolean {
        return false
    }

    fun isAssigned(tp: TopicPartition): Boolean {
        return false
    }

    fun isFetchable(tp: TopicPartition): Boolean {
        val assigned = assignment.stateValue(tp)
        return assigned?.isFetchable() == true
    }

    fun preferredReadReplica(
        tp: TopicPartition,
        expireTimeMs: Long,
    ) {
    }

    fun clearPreferredReadReplica(tp: TopicPartition): Int? {
        val topicPartitionState = assignment.stateValue(tp)
        return topicPartitionState?.clearPreferredReadReplica()
    }

    fun hasValidPosition(tp: TopicPartition): Boolean {
        return true
    }

    fun movePartitionToEnd(tp: TopicPartition) {
    }

    class FetchPosition(
        val offset: Long,
        val offsetEpoch: Long,
        val currentLeader: Metadata.LeaderAndEpoch,
    )

    class TopicPartitionState(
        val position: FetchPosition,
        var preferredReadReplica: Int?,
        var preferredReadReplicaExpireTimeMs: Long?,

        private val paused: Boolean,
    ) {
        fun preferredReadReplica(timeMs: Long): Int? {
            val preferredReadReplicaExpireTimeMs = preferredReadReplicaExpireTimeMs
            if (preferredReadReplicaExpireTimeMs != null && preferredReadReplicaExpireTimeMs < timeMs) {
                preferredReadReplica = null
                return null
            } else {
                return preferredReadReplica
            }
        }

        fun clearPreferredReadReplica(): Int? {
            val removedReplicaId = preferredReadReplica
            preferredReadReplica = null
            preferredReadReplicaExpireTimeMs = null

            return removedReplicaId
        }

        fun isFetchable(): Boolean {
            return paused.not()
        }
    }
}
