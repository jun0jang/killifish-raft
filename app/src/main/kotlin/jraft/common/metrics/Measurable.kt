package jraft.common.metrics

interface Measurable : MetricValueProvider<Double> {

    fun measure(config: MetricConfig, now: Long): Double
}
