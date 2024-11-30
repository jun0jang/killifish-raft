package jraft.metadata.placement

import kotlin.random.Random

/**
 * Kafka's StripedReplicaPlacer 와는 다르게 rack 간의 균등 분배를 고려하지 않음.
 *
 * random broker 순으로 라운드로빈.
 */
class SimpleReplicaPlacer(
    private val random: Random,
) : ReplicaPlacer {

    override fun place(placement: PlacementSpec, cluster: ClusterDescriber): TopicAssignment {
        val brokers = BrokerListFactory.new(
            random = random,
            iterator = cluster.usableBrokers(),
        )

        val placements = mutableListOf<List<Int>>()
        for (partition in 0 until placement.numPartitions) {
            placements.add(
                brokers.place(placement.numReplicas),
            )
        }

        return TopicAssignment(
            assignments = placements.map {
                PartitionAssignment(
                    replicas = it,
                )
            },
        )
    }
}

class BrokerList(
    private val brokers: List<Int>,
) {
    private var offset = 0

    /**
     * return: 선택된 broker id's
     */
    fun place(replicationFactor: Short): List<Int> {
        val result = mutableListOf<Int>()

        for (i in 0 until replicationFactor) {
            result.add(
                brokers[(offset + i) % brokers.size],
            )
        }

        offset += 1

        return result
    }
}

object BrokerListFactory {
    fun new(
        random: Random,
        iterator: Iterator<Int>,
    ): BrokerList {
        val brokerList = iterator.asSequence().toList()

        return BrokerList(
            brokers = brokerList.shuffled(random),
        )
    }
}
