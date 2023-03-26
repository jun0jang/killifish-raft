package jraft.clients

import jraft.common.logger

class ClusterConnectionStates(
    private val connectingNodes: Set<String>,
) {
    private val nodeState: MutableMap<String, NodeConnectionState> = mutableMapOf()

    private val log = logger<ClusterConnectionStates>()

    fun connecting(
        nodeId: String,
        currentTimeMs: Long,
        host: String,
    ) {
        val connectionState = nodeState.get(nodeId)

        if (connectionState != null && connectionState.host == host) {
        } else if (connectionState != null) {
            log.info("Hostname for node {} changed from {} to {}.", nodeId, connectionState.host, host)
        }

        nodeState[nodeId] = NodeConnectionState(
            state = ConnectionState.CONNECTING,
            host = host,
        )
    }

    class NodeConnectionState(
        val state: ConnectionState,
        val host: String,
    )

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CHECKING_API_VERSIONS, READY, AUTHENTICATION_FAILED
    }
}
