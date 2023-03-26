package jraft.common.metrics

import jraft.clients.consumer.internals.metrics.MetricsReporter
import jraft.common.logger
import jraft.common.utils.Time
import java.util.concurrent.ConcurrentHashMap

class Metrics(
    private val time: Time = Time.SYSTEM,
    private val config: MetricConfig = MetricConfig(),
    private val metricsContext: MetricsContext = KafkaMetricsContext(),
) {
    private val log = logger<Metrics>()

    val metrics = ConcurrentHashMap<MetricName, Metric>()

    private val sensors = ConcurrentHashMap<String, Sensor>()

    private val childrenSensors = ConcurrentHashMap<Sensor, MutableList<Sensor>>()

    private val reporters = mutableListOf<MetricsReporter>()

    init {
        for (reporter in reporters) {
            reporter
        }
    }

    fun register(metric: Metric): Metric? {
        val existingMetric = metrics.putIfAbsent(metric.metricName, metric)
        if (existingMetric != null) return existingMetric

        for (reporter in reporters) {
            try {
                reporter.metricChange(metric)
            } catch (e: Exception) {
                log.error("Error in reporter ${reporter.javaClass.name}", e)
            }
        }

        return null
    }

    fun metricName(
        name: String,
        group: String,
        documentation: String = "",
        tags: Map<String, String> = emptyMap(),
    ): MetricName {
        return MetricName(
            name = name,
            group = group,
            description = documentation,
            tags = tags,
        )
    }

    fun sensor(
        name: String,
        recordingLevel: Sensor.RecordingLevel = Sensor.RecordingLevel.INFO,
        parents: List<Sensor> = emptyList(),
        config: MetricConfig? = null,
    ): Sensor {
        val s = sensors[name]
        if (s != null) return s

        val newSensor = Sensor(
            registry = this,
            parents = parents,
            name = name,
            config = config ?: this.config,
            time = time,
            recordingLevel = recordingLevel,
        )
        sensors[name] = newSensor

        for (parent in parents) {
            val children = childrenSensors.computeIfAbsent(parent) { mutableListOf() }
            children.add(newSensor)
        }
        return newSensor
    }
}
