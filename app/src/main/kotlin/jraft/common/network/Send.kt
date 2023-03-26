package jraft.common.network

interface Send {
    fun completed(): Boolean

    fun writeTo(transportLayer: TransportLayer): Long

    fun size(): Long
}
