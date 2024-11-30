package jraft.metadata

import jraft.metadata.placement.ReplicaPlacer

class ClusterControllerManager(
    private val brokers: List<Int>,
    val replicaPlacer: ReplicaPlacer,
) {
    fun isActive(brokerId: Int): Boolean {
        return true
    }

    fun usableBrokers(): Iterator<Int> {
        return brokers.iterator()
    }
}
