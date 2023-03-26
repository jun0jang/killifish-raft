package jraft.common.metrics

import jraft.common.utils.Time

class KafkaMetric(
    override val metricName: MetricName,
    private val time: Time,
    private val lock: Any,
    private val metricValueProvider: MetricValueProvider<*>,
    private val metricConfig: MetricConfig,

) : Metric {

    override fun metricValue(): Any? {
        val now = time.milliseconds()
        synchronized(lock) {
            return when (metricValueProvider) {
                is Measurable -> {
                    metricValueProvider.measure(metricConfig, now)
                }

                is Gauge<*> -> {
                    metricValueProvider.value(metricConfig, now)
                }

                else -> {
                    throw java.lang.IllegalStateException("Not a valid metric: " + metricValueProvider::class)
                }
            }
        }
    }
}
