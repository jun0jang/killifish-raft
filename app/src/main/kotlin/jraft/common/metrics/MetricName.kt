package jraft.common.metrics

data class MetricName(
    private val name: String,
    private val group: String,
    private val description: String,
    private val tags: Map<String, String>,
)
