package jraft.clients.consumer.internals

import jraft.common.TopicPartition

class FetchMetricsAggregator(
    private val metricsManager: FetchMetricsManager,
    private val unrecordedPartitions: Set<TopicPartition>,
) {
    private val fetchMetrics: FetchMetrics = FetchMetrics()

    fun record(tp: TopicPartition, bytes: Int, records: Int) {
        fetchMetrics.increment(bytes = bytes, records = records)
    }

    private class FetchMetrics(
        var bytes: Int = 0,
        var records: Int = 0,
    ) {
        fun increment(
            bytes: Int,
            records: Int,
        ) {
            this.bytes += bytes
            this.records += records
        }
    }
}
