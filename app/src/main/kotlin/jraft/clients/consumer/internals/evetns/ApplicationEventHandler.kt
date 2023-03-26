package jraft.clients.consumer.internals.evetns

import jraft.clients.consumer.internals.ConsumerNetworkThread
import jraft.common.utils.Time
import java.time.Duration
import java.util.concurrent.BlockingQueue
import java.util.function.Supplier

class ApplicationEventHandler(
    time: Time,
    applicationEventProcessorSupplier: Supplier<ApplicationEventProcessor>,
    private val applicationEventQueue: BlockingQueue<ApplicationEvent>,
) {
    private val networkThread = ConsumerNetworkThread(
        time = time,
        applicationEventProcessorSupplier = applicationEventProcessorSupplier,
        networkClientDelegateSupplier = Supplier { TODO() },
        requestManagers = TODO(),
    )

    init {
        networkThread.start()
    }

    fun add(event: ApplicationEvent) {
        applicationEventQueue.add(event)
        networkThread.wakeup()
    }

    fun maximumTimeToWait(): Long {
        return networkThread.maximumTimeToWait()
    }

    fun wakeupNetworkThread() {
        networkThread.wakeup()
    }

    fun close(timeout: Duration) {
    }
}
