package jraft.common

import java.util.UUID

class TopicIdPartition(
    val topicId: UUID,
    val partition: TopicPartition,
)
