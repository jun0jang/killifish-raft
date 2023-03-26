package jraft.clients.consumer.internals

data class FetchConfig(
    val maxPollRecords: Int,
)
