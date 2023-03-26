package jraft.clients.consumer

import jraft.common.TopicPartition

class ConsumerRecords<K, V>(
    private val records: Map<TopicPartition, List<ConsumerRecord<K, V>>>,
) : Iterable<ConsumerRecord<K, V>> {

    override fun iterator(): Iterator<ConsumerRecord<K, V>> {
        return records.values.flatten().iterator()
    }
}
