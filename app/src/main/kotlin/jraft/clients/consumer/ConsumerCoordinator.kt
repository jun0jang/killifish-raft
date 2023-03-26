package jraft.clients.consumer

class ConsumerCoordinator(
    // Reblanace config
    val sessionTimeoutMs: Long = 1000,
    val rebalanceTimeoutMs: Long = 1000,
    val heartbeatIntervalMs: Long = 1000,
    val groupId: String,
)
