package jraft.clients.consumer.internals

import jraft.clients.consumer.ConsumerRecord
import jraft.common.TopicPartition

class Fetch<K, V>(
    val records: MutableMap<TopicPartition, List<ConsumerRecord<K, V>>>,
    var positionAdvanced: Boolean,
    var numRecords: Int,
) {
    companion object {
        fun <K, V> empty(): Fetch<K, V> {
            return Fetch(
                records = mutableMapOf(),
                positionAdvanced = false,
                numRecords = 0,
            )
        }

        fun <K, V> forPartition(
            partition: TopicPartition,
            records: List<ConsumerRecord<K, V>>,
        ): Fetch<K, V> {
            return Fetch(
                records = mutableMapOf(partition to records),
                positionAdvanced = false,
                numRecords = 0,
            )
        }
    }

    fun add(fetch: Fetch<K, V>) {
        addRecords(fetch.records)
        this.positionAdvanced = this.positionAdvanced || fetch.positionAdvanced
    }

    private fun addRecords(records: Map<TopicPartition, List<ConsumerRecord<K, V>>>) {
        for ((partition, partRecords) in records) {
            this.numRecords += partRecords.size
            val currentRecords = this.records[partition]

            if (currentRecords == null) {
                this.records[partition] = partRecords
            } else {
                val newRecords = currentRecords + partRecords
                this.records[partition] = newRecords
            }
        }
    }
}
