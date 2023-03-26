package jraft.common.metrics

import jraft.common.utils.Time
import java.util.function.Supplier

class Sensor(
    val registry: Metrics,
    val name: String,
    val metrics: MutableMap<MetricName, KafkaMetric> = LinkedHashMap(),
    val parents: List<Sensor> = emptyList(),
    val config: MetricConfig,
    val time: Time,
    var lastRecordTime: Long = time.milliseconds(),
    val stats: MutableList<StatAndConfig> = mutableListOf(),
    val recordingLevel: RecordingLevel = RecordingLevel.INFO,
) {
    private val metricLock = Any()

    enum class RecordingLevel {
        INFO,
        DEBUG,
        TRACE,
    }

    @Synchronized
    fun add(
        metricName: MetricName,
        stat: MeasurableStat,
        config: MetricConfig? = null,
    ) {
        if (metrics.containsKey(metricName)) return

        val metric = KafkaMetric(
            metricName = metricName,
            time = time,
            lock = metricLock,
            metricValueProvider = stat,
            metricConfig = config ?: this.config,
        )
        val existingMetric = registry.register(metric)
        if (existingMetric != null) {
            throw IllegalArgumentException("A metric named $metricName already exists")
        }
        metrics[metricName] = metric
        stats.add(
            StatAndConfig(
                stat = stat,
                configSupplier = { this.config },
            ),
        )
    }

    fun record(value: Double) {
        recordInternal(value, time.milliseconds(), checkQuotas = true)
    }

    private fun recordInternal(
        value: Double,
        timeMs: Long,
        checkQuotas: Boolean,
    ) {
        this.lastRecordTime = time.milliseconds()

        synchronized(this) {
            synchronized(metricLock) {
                for (statAndConfig in this.stats) {
                    statAndConfig.stat.record(statAndConfig.config(), value, timeMs)
                }
            }
        }
        for (parent in parents) {
            parent.recordInternal(value, timeMs, checkQuotas)
        }
    }

    class StatAndConfig(
        val stat: Stat,
        private val configSupplier: Supplier<MetricConfig>,
    ) {
        fun config(): MetricConfig {
            return configSupplier.get()
        }
    }
}
