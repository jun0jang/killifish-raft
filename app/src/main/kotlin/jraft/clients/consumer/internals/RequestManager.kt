package jraft.clients.consumer.internals

import jraft.clients.consumer.internals.NetworkClientDelegate.PollResult

interface RequestManager {
    fun poll(currentTimeMs: Long): PollResult
}
