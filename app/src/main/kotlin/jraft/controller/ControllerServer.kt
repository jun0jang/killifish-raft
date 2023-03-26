package jraft.controller

import jraft.raft.QuorumState
import java.io.File

class ControllerServer(
    private val socketServer: SocketServer,
    private val raftManager: RaftManager,
) {
    fun start() {
        socketServer.start()
        raftManager.start()
    }
}

fun main() {
    val socketServer = SocketServer(port = 9094)
    val raftManager =
        RaftManager(
            quorumState =
            QuorumState(
                nodeId = 1,
                File("./state"),
            ),
        )
    val controllerServer = ControllerServer(socketServer, raftManager)
    controllerServer.start()
}
