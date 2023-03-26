package jraft.common.metrics

import java.time.Duration

class MetricConfig(
    val samples: Int = 2,
    val eventWindow: Long = Long.MAX_VALUE,
    val timeWindowMs: Long = Duration.ofSeconds(30).toMillis(),
    val recordingLevel: Sensor.RecordingLevel = Sensor.RecordingLevel.INFO,
)
