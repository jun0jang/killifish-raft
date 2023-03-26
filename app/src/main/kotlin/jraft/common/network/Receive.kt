package jraft.common.network

import java.nio.ByteBuffer

interface Receive {
    fun completed(): Boolean

    fun readFrom(transportLayer: TransportLayer): Long

    fun payload(): ByteBuffer
}
