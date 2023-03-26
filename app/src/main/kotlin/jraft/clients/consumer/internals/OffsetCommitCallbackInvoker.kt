package jraft.clients.consumer.internals

import jraft.clients.consumer.OffsetAndMetadata
import jraft.clients.consumer.OffsetCommitCallback
import jraft.common.TopicPartition
import jraft.common.errors.FencedInstanceIdException
import java.lang.Exception
import java.util.concurrent.LinkedBlockingQueue

class OffsetCommitCallbackInvoker(
    private val interceptor: ConsumerInterceptors<*, *>,
) {
    private val callbackQueue = LinkedBlockingQueue<OffsetCommitCallbackTask>()

    private var hasFencedException = false

    fun executeCallbacks() {
        while (callbackQueue.isNotEmpty()) {
            val task = callbackQueue.poll() ?: break
            if (task.exception is FencedInstanceIdException) {
                hasFencedException = true
            }

            task.callback.onComplete(task.offsets, task.exception)
        }
    }

    class OffsetCommitCallbackTask(
        val offsets: Map<TopicPartition, OffsetAndMetadata>,
        val callback: OffsetCommitCallback,
        val exception: Exception,
    )
}
