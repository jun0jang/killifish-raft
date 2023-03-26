package jraft.common.network

import java.nio.channels.GatheringByteChannel

interface TransferableChannel : GatheringByteChannel {
    fun hasPendingWrites(): Boolean
}
