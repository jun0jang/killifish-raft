package jraft.common.metrics

interface Metric {
    val metricName: MetricName

    fun metricValue(): Any?
}
