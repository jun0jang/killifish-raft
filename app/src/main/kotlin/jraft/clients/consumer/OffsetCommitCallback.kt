package jraft.clients.consumer

import jraft.common.TopicPartition

interface OffsetCommitCallback {
    fun onComplete(offsets: Map<TopicPartition, OffsetAndMetadata>, exception: Exception?)
}
