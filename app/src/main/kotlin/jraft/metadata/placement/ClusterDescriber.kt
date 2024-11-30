package jraft.metadata.placement

import jraft.metadata.ClusterControllerManager

/**
 * 현재 클러스터의 상태를 ReplicaPlacer에게 설명할 수 있다.
 */
interface ClusterDescriber {
    fun usableBrokers(): Iterator<Int>
}

class KRaftClusterDescriber(
    private val clusterControl: ClusterControllerManager,
) : ClusterDescriber {
    override fun usableBrokers(): Iterator<Int> {
        return clusterControl.usableBrokers()
    }
}
