package jraft.common.metrics

class KafkaMetricsContext : MetricsContext {
    private val contextLabels = mutableMapOf<String, String>()

    override fun contextLabels(): Map<String, String> {
        return contextLabels
    }

    companion object {
        fun create(namespace: String, labels: Map<String, String> = emptyMap()): KafkaMetricsContext {
            val result = KafkaMetricsContext()
            result.contextLabels[MetricsContext.NAMESPACE] = namespace
            result.contextLabels.putAll(labels)

            return result
        }
    }
}
