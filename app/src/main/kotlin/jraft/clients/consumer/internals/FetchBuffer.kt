package jraft.clients.consumer.internals

import jraft.common.execute
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

/**
 * nextInLineFetch vs completedFetches
 * -
 */
class FetchBuffer(
    private val completedFetches: ConcurrentLinkedQueue<CompletedFetch> = ConcurrentLinkedQueue(),
) : AutoCloseable {
    private var nextInLineFetch: CompletedFetch? = null

    private val lock = ReentrantLock()

    private val notEmptyCondition: Condition = lock.newCondition()

    override fun close() {
        TODO("Not yet implemented")
    }

    // get top of the queue without removing it
    fun peek(): CompletedFetch? {
        return lock.execute {
            completedFetches.peek()
        }
    }

    // get top of the queue and remove it
    fun poll(): CompletedFetch? {
        return lock.execute {
            completedFetches.poll()
        }
    }

    fun isEmpty(): Boolean {
        return lock.execute {
            completedFetches.isEmpty()
        }
    }

    fun add(completedFetch: CompletedFetch) {
        lock.execute {
            completedFetches.add(completedFetch)
            notEmptyCondition.signal()
        }
    }

    fun nextInLineFetch(): CompletedFetch? {
        return lock.execute {
            nextInLineFetch
        }
    }

    fun setNextInLineFetch(completedFetch: CompletedFetch?) {
        lock.execute {
            nextInLineFetch = completedFetch
        }
    }
}
