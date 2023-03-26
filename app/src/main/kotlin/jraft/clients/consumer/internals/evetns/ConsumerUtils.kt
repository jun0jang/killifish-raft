package jraft.clients.consumer.internals.evetns

import jraft.clients.consumer.ConsumerConfig
import jraft.common.metrics.KafkaMetricsContext
import jraft.common.metrics.Metrics
import jraft.common.utils.Time

class ConsumerUtils {

    fun createMetrics(config: ConsumerConfig, time: Time): Metrics {
        val clientId = config.get<String>(ConsumerConfig.CLIENT_ID_CONFIG)
        val metricsTags = mapOf(CONSUMER_CLIENT_ID_METRIC_TAG to clientId)

        val metricsContext = KafkaMetricsContext.create(CONSUMER_JMX_PREFIX, metricsTags)

        return Metrics(metricsContext = metricsContext, time = time)
    }

    companion object {
        const val CONSUMER_JMX_PREFIX = "kafka.consumer"

        const val CONSUMER_CLIENT_ID_METRIC_TAG = "client-id"
    }
}
