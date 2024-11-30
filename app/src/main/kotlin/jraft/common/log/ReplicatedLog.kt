package jraft.common.log

import jraft.common.TopicPartition

/**
 * raft metadata log
 */
class ReplicatedLog(
    private val log: UnifiedLog,
    private val topicPartition: TopicPartition,
)
