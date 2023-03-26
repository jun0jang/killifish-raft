package jraft.common.network

class ReceiveBuilder(
    // http 통신이라면 true
    private val http: Boolean = false,
    // 내부 노드간 통신이라면 true
    private val node: Boolean = false,
) {
    fun build(): Receive {
        if (http) return HttpReceive()

        if (node) return NetworkReceive()

        throw IllegalArgumentException("Invalid ReceiveBuilder")
    }
}
