package jraft.clients.consumer.internals

import jraft.clients.consumer.ConsumerRecord
import jraft.clients.consumer.SubscriptionState
import jraft.common.TopicPartition
import jraft.common.log.record.Record
import jraft.common.log.record.RecordBatch
import jraft.common.requests.FetchResponseData

class CompletedFetch(
    val partition: TopicPartition,

    private val subscription: SubscriptionState,

    private var recordsRead: Int,

    private val bytesRead: Int,

    private var lastRecord: Record,

    val partitionData: FetchResponseData.PartitionData,

    var nextFetchOffset: Long,

    private val records: Iterator<Record>?,

    private val batches: Iterator<RecordBatch>,
) {

    var isInitialize = false

    var isConsumed: Boolean = false

    private var corruptLastRecord: Boolean = false

    private var cachedRecordException: Exception? = null

    private var currentBatch: RecordBatch? = null

    fun drain() {
        if (isConsumed) return

        isConsumed = true
        if (bytesRead > 0) {
            subscription.movePartitionToEnd(partition)
        }
    }

    private fun maybeCloseRecordStream() {
        // close records
    }

    fun recordAggregatedMetrics(bytes: Int, records: Int) {
    }

    fun <K, V> fetchRecords(
        fetchConfig: FetchConfig,
        deserializers: Deserializers<K, V>,
        maxRecords: Int,
    ): List<ConsumerRecord<K, V>> {
        if (isConsumed) return emptyList()

        val records = mutableListOf<ConsumerRecord<K, V>>()
        for (i in 1..maxRecords) {
            if (cachedRecordException == null) {
                corruptLastRecord = true
                nextFetchedRecord(fetchConfig)?.let {
                    lastRecord = it
                }
                corruptLastRecord = false
            }

            recordsRead++
        }

        return records
    }

    private fun nextFetchedRecord(fetchConfig: FetchConfig): Record? {
        while (true) {
            if (records == null || records.hasNext().not()) {
                maybeCloseRecordStream()

                if (batches.hasNext().not()) {
                    if (currentBatch != null) {
                        nextFetchOffset = currentBatch!!.nextOffset()
                    }
                    drain()
                    return null
                }

                currentBatch = batches.next()
            } else {
                val record = records.next()

                record.offset()
            }
        }
    }
}
