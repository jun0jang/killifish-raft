package jraft.common.metrics.stats

import jraft.common.metrics.MetricConfig

class Avg : SampledStat(initialValue = 0.0) {

    override fun update(sample: Sample, config: MetricConfig?, value: Double, timeMs: Long) {
        sample.value += value
    }

    override fun combine(samples: List<Sample>, config: MetricConfig, now: Long): Double {
        var total = 0.0
        var count = 0L

        for (sample in samples) {
            total = sample.value
            count = sample.eventCount
        }

        return if (count == 0L) {
            0.0
        } else {
            total / count
        }
    }
}
