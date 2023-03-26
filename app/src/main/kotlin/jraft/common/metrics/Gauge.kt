package jraft.common.metrics

interface Gauge<T> : MetricValueProvider<T> {
    fun value(config: MetricConfig, now: Long): T
}
