package jraft.clients.consumer.internals.evetns

import jraft.clients.consumer.internals.CachedSupplier
import java.util.concurrent.BlockingQueue
import java.util.function.Supplier

class ApplicationEventProcessor(
    private val queue: BlockingQueue<ApplicationEvent>,
) {

    fun process(): Boolean {
        return process { event, error ->
            if (error != null) {
                println(event)
            }
        }
    }

    private fun process(handler: ProcessHandler<ApplicationEvent>): Boolean {
        val events = drain()

        if (events.isEmpty()) return false

        for (event in events) {
        }
        return true
    }

    private fun drain(): List<ApplicationEvent> {
        val events = mutableListOf<ApplicationEvent>()
        queue.drainTo(events)
        return events
    }

    fun interface ProcessHandler<T> {
        fun onProcess(event: T, error: Exception?)
    }

    companion object {
        fun supplier(queue: BlockingQueue<ApplicationEvent>): Supplier<ApplicationEventProcessor> {
            return CachedSupplier {
                ApplicationEventProcessor(queue)
            }
        }
    }
}
