package jraft.common

class Node(
    val id: Int,
    val host: String,
    val port: Int,
) {
    fun isEmpty(): Boolean {
        return host.isEmpty() || port < 0
    }
}
