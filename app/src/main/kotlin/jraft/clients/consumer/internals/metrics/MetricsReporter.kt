package jraft.clients.consumer.internals.metrics

import jraft.common.metrics.Metric

interface MetricsReporter {

    fun init(metrics: List<Metric>)

    fun metricChange(metric: Metric)
}
