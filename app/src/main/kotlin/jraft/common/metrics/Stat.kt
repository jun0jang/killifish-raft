package jraft.common.metrics

interface Stat {

    fun record(config: MetricConfig, value: Double, timeMs: Long)
}
