package jraft.common.metrics

interface MetricsContext {
    fun contextLabels(): Map<String, String>

    companion object {
        const val NAMESPACE = "_namesapce"
    }
}
