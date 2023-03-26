package jraft.clients.consumer.internals.metrics

import jraft.common.metrics.Metrics
import jraft.common.metrics.Sensor
import jraft.common.metrics.stats.Avg

class KafkaConsumerMetrics(
    private val metrics: Metrics,
    private val pollIdleSensor: Sensor,
) {
    private var pollStartMs: Long = 0

    // poll 시작 시간 - poll 종료 시간 (ex: 1분)
    private var timeSinceLastPollMs: Long = 0

    private var lastPollMs: Long = 0

    fun recordPollStart(pollStartMs: Long) {
        this.pollStartMs = pollStartMs
        this.timeSinceLastPollMs = if (lastPollMs == 0L) 0 else (pollStartMs - lastPollMs)
        this.lastPollMs = pollStartMs
    }

    fun recordPollEnd(pollEndMs: Long) {
        val pollTimeMs = pollEndMs - pollStartMs
        val pollIdleRatio: Double = pollTimeMs * 1.0 / (pollTimeMs + timeSinceLastPollMs)
        this.pollIdleSensor.record(pollIdleRatio)
    }

    companion object {
        fun create(metrics: Metrics, metricGroupPrefix: String): KafkaConsumerMetrics {
            val metricGroupName = "$metricGroupPrefix-metrics"

            val pollIdleSensor = metrics.sensor("poll-idle-ratio-avg")
            pollIdleSensor.add(
                metricName = metrics.metricName(
                    "poll-idle-ratio-avg",
                    metricGroupName,
                ),
                stat = Avg(),
            )

            return KafkaConsumerMetrics(metrics, pollIdleSensor)
        }
    }
}
