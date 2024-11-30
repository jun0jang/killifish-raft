package jraft.metadata

import io.kotest.core.spec.style.FreeSpec
import jraft.metadata.placement.SimpleReplicaPlacer
import kotlin.random.Random

class ReplicationControllerManagerTest : FreeSpec({

    "testAvoidFencedReplicaIfPossibleOnSingleRack" {
        val placer = SimpleReplicaPlacer(
            random = Random(0),
        )

        val rackList = SimpleReplicaPlacer.RackList()
    }
})
