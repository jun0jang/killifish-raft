package jraft.clients.consumer.internals.evetns

import java.util.UUID

abstract class ApplicationEvent(
    val id: UUID = UUID.randomUUID(),
    val type: Type,
) {
    enum class Type {
        POLL,
    }
}
