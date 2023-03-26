package jraft.clients.consumer.internals.evetns

class PollEvent(
    val pollTimeMs: Long,
) : ApplicationEvent(type = Type.POLL)
