package jraft.clients.consumer

import jraft.common.TopicPartition

interface ConsumerRebalanceListener {
    /**
     * 제거된 파티션
     */
    fun onPartitionsRevoked(partitions: Collection<TopicPartition>)
}
