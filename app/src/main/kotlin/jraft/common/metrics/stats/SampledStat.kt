package jraft.common.metrics.stats

import jraft.common.metrics.MeasurableStat
import jraft.common.metrics.MetricConfig

abstract class SampledStat(
    private val initialValue: Double,
) : MeasurableStat {
    private val samples: MutableList<Sample?> = mutableListOf()

    private var current: Int = 0

    override fun record(config: MetricConfig, value: Double, timeMs: Long) {
        var sample = current(timeMs)
        if (sample.isComplete(timeMs, config)) {
            sample = advance(config, timeMs)
        }
        update(sample, config, value, timeMs)
        sample.eventCount += 1
    }

    override fun measure(config: MetricConfig, now: Long): Double {
        purgeObsoleteSamples(config, now)
        return combine(this.samples.filterNotNull(), config, now)
    }

    private fun current(timeMs: Long): Sample {
        if (samples.size == 0) {
            this.samples.add(Sample(initialValue, 0, timeMs, initialValue))
        }
        return samples[current]!!
    }

    private fun advance(config: MetricConfig, timeMs: Long): Sample {
        current = (current + 1) % config.samples

        if (current >= samples.size) {
            val sample = Sample(initialValue, 0, timeMs, initialValue)
            samples.add(sample)
            return sample
        } else {
            val sample = current(timeMs)
            sample.reset(timeMs)
            return sample
        }
    }

    protected abstract fun update(sample: Sample, config: MetricConfig?, value: Double, timeMs: Long)

    abstract fun combine(samples: List<Sample>, config: MetricConfig, now: Long): Double

    protected fun purgeObsoleteSamples(config: MetricConfig, now: Long) {
        val expireAge: Long = config.samples * config.timeWindowMs
        for (sample in samples.filterNotNull()) {
            if (now - sample.lastWindowMs >= expireAge) sample.reset(now)
        }
    }

    class Sample(
        val initialValue: Double,
        var eventCount: Long,
        var lastWindowMs: Long,
        var value: Double,
    ) {
        fun isComplete(timeMs: Long, config: MetricConfig): Boolean {
            return timeMs - lastWindowMs >= config.timeWindowMs
        }

        fun reset(timeMs: Long) {
            eventCount = 0
            lastWindowMs = timeMs
            value = initialValue
        }
    }
}
